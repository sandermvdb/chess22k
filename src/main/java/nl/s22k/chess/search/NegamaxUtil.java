package nl.s22k.chess.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import nl.s22k.chess.CheckUtil;
import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.ChessConstants;
import nl.s22k.chess.ChessConstants.ScoreType;
import nl.s22k.chess.Statistics;
import nl.s22k.chess.Util;
import nl.s22k.chess.engine.EngineConstants;
import nl.s22k.chess.engine.MainEngine;
import nl.s22k.chess.eval.EvalConstants;
import nl.s22k.chess.eval.EvalUtil;
import nl.s22k.chess.eval.MaterialUtil;
import nl.s22k.chess.move.MoveGenerator;
import nl.s22k.chess.move.MoveList;
import nl.s22k.chess.move.MoveUtil;
import nl.s22k.chess.move.MoveWrapper;

public final class NegamaxUtil {

	public static boolean stop = true;
	public static int maxDepth = EngineConstants.MAX_PLIES;

	public static final int PHASE_TT = 0;
	public static final int PHASE_ATTACKING = 1;
	public static final int PHASE_KILLER_1 = 2;
	public static final int PHASE_KILLER_2 = 3;
	public static final int PHASE_QUIET = 4;

	// Margins shamelessly stolen from Laser
	private static final int[] STATIC_NULLMOVE_MARGIN = { 0, 60, 130, 210, 300, 400, 510 };
	private static final int[] RAZORING_MARGIN = { 0, 240, 280, 300 };
	private static final int[] FUTILITY_MARGIN = { 0, 80, 170, 270, 380, 500, 630 };

	public static int calculateBestMove(final ChessBoard cb, final int ply, int depth, int alpha, int beta, final int nullMoveCounter) {

		if (stop) {
			return 0;
		}

		if (EngineConstants.ASSERT) {
			assertTrue(depth >= 0);
			assertTrue(alpha >= Util.SHORT_MIN && alpha <= Util.SHORT_MAX);
			assertTrue(beta >= Util.SHORT_MIN && beta <= Util.SHORT_MAX);
		}

		// get extensions
		depth += extensions(cb, ply);

		// TODO JITWatch unpredictable branch
		if (depth == 0) {
			return QuiescenceUtil.calculateBestMove(cb, alpha, beta);
		}

		Statistics.maxDepth = Math.max(Statistics.maxDepth, ply);
		if (Statistics.ENABLED) {
			Statistics.abNodes++;
		}

		/* mate-distance pruning */
		if (EngineConstants.ENABLE_MATE_DISTANCE_PRUNING) {
			alpha = Math.max(alpha, Util.SHORT_MIN + ply);
			beta = Math.min(beta, Util.SHORT_MAX - ply - 1);
			if (alpha >= beta) {
				return alpha;
			}
		}

		int score;

		/* transposition-table */
		long ttValue = TTUtil.getTTValue(cb.zobristKey);
		if (ttValue != 0) {
			if (!EngineConstants.TEST_TT_VALUES) {

				if (TTUtil.getDepth(ttValue) >= (depth)) {
					score = TTUtil.getScore(ttValue, ply);

					switch (TTUtil.getFlag(ttValue)) {
					case TTUtil.FLAG_EXACT:
						return score;
					case TTUtil.FLAG_LOWER:
						if (score >= beta) {
							return score;
						}
						break;
					case TTUtil.FLAG_UPPER:
						if (score <= alpha) {
							return score;
						}
					}
				}
			}
		}

		int eval = Util.SHORT_MIN;
		final boolean isPv = beta - alpha != 1;
		if (!isPv && cb.checkingPieces == 0) {

			eval = ChessConstants.COLOR_FACTOR[cb.colorToMove] * EvalUtil.getScore(cb);

			/* use tt value as eval */
			if (ttValue != 0) {
				if (TTUtil.getFlag(ttValue) == TTUtil.FLAG_EXACT || TTUtil.getFlag(ttValue) == TTUtil.FLAG_UPPER && TTUtil.getScore(ttValue, ply) < eval
						|| TTUtil.getFlag(ttValue) == TTUtil.FLAG_LOWER && TTUtil.getScore(ttValue, ply) > eval) {
					eval = TTUtil.getScore(ttValue, ply);
				}
			}

			/* static null move pruning */
			if (EngineConstants.ENABLE_STATIC_NULL_MOVE) {
				if (depth < STATIC_NULLMOVE_MARGIN.length) {
					if (eval - STATIC_NULLMOVE_MARGIN[depth] >= beta) {
						if (Statistics.ENABLED) {
							Statistics.staticNullMoved[depth]++;
						}
						return eval;
					}
				}
			}

			/* razoring */
			if (EngineConstants.ENABLE_RAZORING) {
				if (depth < RAZORING_MARGIN.length) {
					if (eval + RAZORING_MARGIN[depth] < alpha) {
						final int q = QuiescenceUtil.calculateBestMove(cb, alpha - RAZORING_MARGIN[depth], alpha - RAZORING_MARGIN[depth] + 1);
						if (q + RAZORING_MARGIN[depth] <= alpha) {
							if (Statistics.ENABLED) {
								Statistics.razored[depth]++;
							}
							return q;
						}
					}
				}
			}

			/* null-move */
			if (EngineConstants.ENABLE_NULL_MOVE) {
				if (nullMoveCounter < 2 && MaterialUtil.hasMajorPieces(cb.materialKey, cb.colorToMove) && depth > 1) {
					cb.doNullMove();
					// TODO less reduction if stm (other side) has only 1 major piece
					final int reduction = 2 + depth / 3;
					score = depth - reduction == 0 ? -QuiescenceUtil.calculateBestMove(cb, -beta, -beta + 1)
							: -calculateBestMove(cb, ply + 1, depth - reduction, -beta, -beta + 1, nullMoveCounter + 1);
					cb.undoNullMove();
					if (score >= beta) {
						if (Statistics.ENABLED) {
							Statistics.nullMoveHit++;
						}
						return score;
					}
					if (Statistics.ENABLED) {
						Statistics.nullMoveMiss++;
					}
				}
			}
		}

		final int alphaOrig = alpha;
		final boolean wasInCheck = cb.checkingPieces != 0;

		int bestMove = 0;
		int bestScore = Util.SHORT_MIN;
		int ttMove = 0;
		int killer1Move = 0;
		int killer2Move = 0;
		int movesPerformed = 0;

		MoveList.startPly();
		int phase = PHASE_TT;
		while (phase <= PHASE_QUIET) {
			switch (phase) {
			case PHASE_TT:
				if (ttValue == 0) {
					/* IID */
					if (EngineConstants.ENABLE_IID && depth > 5 && isPv) {
						// no iid in pawn-endgame because the extension could cause an endless loop
						if (MaterialUtil.containsMajorPieces(cb.materialKey)) {
							if (Statistics.ENABLED) {
								Statistics.iidCount++;
							}
							calculateBestMove(cb, ply, depth - EngineConstants.IID_REDUCTION - 1, alpha, beta, 0);
							ttValue = TTUtil.getTTValue(cb.zobristKey);
						}
					}
				}
				if (ttValue != 0) {
					ttMove = TTUtil.getMove(ttValue);

					// verify TT-move?
					if (EngineConstants.VERIFY_TT_MOVE) {
						if (!cb.isValidMove(ttMove)) {
							throw new RuntimeException("Invalid tt-move found! " + new MoveWrapper(ttMove) + " - " + cb.toString());
						}
					}

					MoveList.addMove(ttMove);
				}
				break;
			case PHASE_ATTACKING:
				// TODO no ordering at ALL-nodes?
				MoveGenerator.generateAttacks(cb);
				MoveList.setMVVLVAScores(cb);
				MoveList.sort();
				break;
			case PHASE_KILLER_1:
				// TODO skip killer when in-check?
				killer1Move = HeuristicUtil.getKiller1(ply);
				if (killer1Move != 0 && killer1Move != ttMove && cb.isValidQuietMove(killer1Move)) {
					MoveList.addMove(killer1Move);
				}
				break;
			case PHASE_KILLER_2:
				killer2Move = HeuristicUtil.getKiller2(ply);
				if (killer2Move != 0 && killer2Move != ttMove && cb.isValidQuietMove(killer2Move)) {
					MoveList.addMove(killer2Move);
				}
				break;
			case PHASE_QUIET:
				MoveGenerator.generateMoves(cb);
				MoveList.setHHScores(cb);
				MoveList.sort();
			}

			while (MoveList.hasNext()) {

				final int moveScore = MoveList.getNextScore();
				final int move = MoveUtil.getCleanMove(MoveList.next());

				if (phase != PHASE_TT && !cb.isLegal(move)) {
					continue;
				}

				if (phase == PHASE_ATTACKING) {
					// skip tt-moves
					if (move == ttMove) {
						continue;
					}
				} else if (phase == PHASE_QUIET) {
					// skip tt- and killer-moves
					if (move == ttMove || move == killer1Move || move == killer2Move) {
						continue;
					}
				}

				// pruning allowed?
				if (!isPv && cb.checkingPieces == 0 && MoveUtil.isQuiet(move) && movesPerformed > 0) {
					/* late move pruning */
					if (EngineConstants.ENABLE_LMP) {
						if (depth <= 4 && movesPerformed >= depth * 3 + 3) {
							if (Statistics.ENABLED) {
								Statistics.lmped[depth]++;
							}
							continue;
						}
					}

					/* futility pruning */
					if (EngineConstants.ENABLE_FUTILITY_PRUNING) {
						if (moveScore < 100 && !MoveUtil.isPawnPush78(move)) {
							if (depth < FUTILITY_MARGIN.length) {
								if (eval == Util.SHORT_MIN) {
									eval = ChessConstants.COLOR_FACTOR[cb.colorToMove] * EvalUtil.getScore(cb);
								}
								final int futilityValue = eval + FUTILITY_MARGIN[depth];
								if (futilityValue <= alpha) {
									if (Statistics.ENABLED) {
										Statistics.futile[depth]++;
									}
									if (futilityValue > bestScore) {
										bestScore = futilityValue;
									}
									continue;
								}
							}
						}
					}
				}

				cb.doMove(move);
				movesPerformed++;
				if (ply == 0) {
					MainEngine.sendMoveInfo(move, movesPerformed);
				}
				score = Util.SHORT_MAX; // initial is above alpha

				/* draw check */
				if (RepetitionTable.isRepetition(cb) || cb.isDrawByMaterial()) {
					score = EvalConstants.SCORE_DRAW;
				} else {

					RepetitionTable.addValue(cb.zobristKey);

					if (EngineConstants.ASSERT) {
						cb.changeSideToMove();
						assertEquals("king under attack", 0, CheckUtil.getCheckingPieces(cb));
						cb.changeSideToMove();
					}

					// TODO create constant for LMR_DEPTH
					if (EngineConstants.ENABLE_LMR && moveScore < EngineConstants.LMR_HISTORY && movesPerformed > EngineConstants.LMR_MOVE_COUNTER && depth > 3
							&& MoveUtil.isQuiet(move) && cb.checkingPieces == 0 && !wasInCheck && !MoveUtil.isPawnPush678(move, cb.colorToMoveInverse)) {
						/* LMR */
						final int reduction = move != killer1Move && moveScore < 20 && movesPerformed > 6 ? Math.min(depth - 1, 2 + depth / 6) : 2;
						score = -calculateBestMove(cb, ply + 1, depth - reduction, -alpha - 1, -alpha, 0);
						if (Statistics.ENABLED) {
							if (score > alpha) {
								Statistics.lmrMoveMiss++;
							} else {
								Statistics.lmrMoveHit++;
							}
						}
					} else if (EngineConstants.ENABLE_PVS && movesPerformed > 1) {
						/* PVS */
						score = -calculateBestMove(cb, ply + 1, depth - 1, -alpha - 1, -alpha, 0);
						if (Statistics.ENABLED) {
							if (score > alpha) {
								Statistics.pvsMoveMiss++;
							} else {
								Statistics.pvsMoveHit++;
							}
						}
					}
					if (score > alpha) {
						score = -calculateBestMove(cb, ply + 1, depth - 1, -beta, -alpha, 0);
					}

					RepetitionTable.removeValue(cb.zobristKey);

				}
				cb.undoMove(move);

				if (score > bestScore) {
					bestScore = score;
					bestMove = move;
				}
				alpha = Math.max(alpha, score);
				if (alpha >= beta) {

					/* killer and history */
					if (MoveUtil.isQuiet(move)) {
						HeuristicUtil.addKillerMove(move, ply);
						HeuristicUtil.addHHValue(cb.colorToMove, MoveUtil.getFromToIndex(move), depth);
					}

					phase += 10;
					break;
				}

				if (MoveUtil.isQuiet(move)) {
					HeuristicUtil.addBFValue(cb.colorToMove, MoveUtil.getFromToIndex(move), depth);
				}
			}
			phase++;
		}

		/* checkmate or stalemate */
		if (movesPerformed == 0) {
			MoveList.endPly();
			if (cb.checkingPieces == 0) {
				if (Statistics.ENABLED) {
					Statistics.staleMateCount++;
				}
				return EvalConstants.SCORE_DRAW;
			} else {
				if (Statistics.ENABLED) {
					Statistics.mateCount++;
				}
				return Util.SHORT_MIN + ply;
			}
		}

		if (EngineConstants.ASSERT) {
			assertTrue(bestMove != 0);
		}

		// set tt-flag
		int flag = TTUtil.FLAG_EXACT;
		if (bestScore >= beta) {
			flag = TTUtil.FLAG_LOWER;
		} else if (bestScore <= alphaOrig) {
			flag = TTUtil.FLAG_UPPER;
		}

		TTUtil.addValue(cb.zobristKey, bestScore, ply, depth, flag, bestMove);

		/* update statistics */
		if (Statistics.ENABLED) {
			if (flag == TTUtil.FLAG_LOWER) {
				Statistics.cutNodes++;
			} else if (flag == TTUtil.FLAG_UPPER) {
				Statistics.allNodes++;
			} else {
				Statistics.pvNodes++;
			}
			if (bestMove == ttMove) {
				if (TTUtil.getFlag(ttValue) == TTUtil.FLAG_LOWER) {
					Statistics.bestMoveTTLower++;
				} else if (TTUtil.getFlag(ttValue) == TTUtil.FLAG_UPPER) {
					Statistics.bestMoveTTUpper++;
				} else {
					Statistics.bestMoveTT++;
				}
			} else if (MoveUtil.isPromotion(bestMove)) {
				Statistics.bestMovePromotion++;
			} else if (MoveUtil.getAttackedPieceIndex(bestMove) != 0) {
				// slow but disabled when statistics are disabled
				// if (SEEUtil.getSeeCaptureScore(cb, bestMove) < 0) {
				// Statistics.bestMoveLosingCapture++;
				// } else {
				// Statistics.bestMoveWinningCapture++;
				// }
			} else if (bestMove == killer1Move) {
				Statistics.bestMoveKiller1++;
			} else if (bestMove == killer2Move) {
				Statistics.bestMoveKiller2++;
			} else {
				Statistics.bestMoveOther++;
			}
		}

		if (EngineConstants.TEST_TT_VALUES) {
			if (ttValue != 0 && TTUtil.getDepth(ttValue) == depth) {
				final int ttScore = TTUtil.getScore(ttValue, ply);
				if (TTUtil.getFlag(ttValue) == TTUtil.FLAG_EXACT && flag == TTUtil.FLAG_EXACT) {
					if (ttScore != bestScore) {
						System.out.println(String.format("Error: TT-score %s, bestScore %s", ttScore, alpha));
						TTUtil.getScore(ttValue, ply);
					}
				}
			}
		}

		MoveList.endPly();
		return bestScore;
	}

	private static int extensions(final ChessBoard cb, final int ply) {
		/* extension when the pawn endgame starts */
		if (EngineConstants.ENABLE_ENDGAME_EXTENSION && ply > 0 && MoveUtil.getAttackedPieceIndex(MoveList.previous()) > ChessConstants.PAWN
				&& !MaterialUtil.containsMajorPieces(cb.materialKey)) {
			if (Statistics.ENABLED) {
				Statistics.endGameExtensions++;
			}
			return EngineConstants.ENDGAME_EXTENSION_DEPTH;
		}
		/* check-extension */
		// TODO extend discovered checks?
		// TODO extend checks with SEE > 0?
		// TODO extend when mate-threat?
		if (EngineConstants.ENABLE_CHECK_EXTENSION && cb.checkingPieces != 0) {
			if (Statistics.ENABLED) {
				Statistics.checkExtensions++;
			}
			return 1;
		}
		return 0;
	}

	public static void start(ChessBoard chessBoard) {
		stop = false;

		TTUtil.init(false);

		int depth = 1;
		int alpha = Util.SHORT_MIN;
		int beta = Util.SHORT_MAX;
		int score = Util.SHORT_MIN;
		boolean panic = false;

		while (depth != maxDepth + 1) {

			Statistics.depth = depth;

			int delta = EngineConstants.ASPIRATION_WINDOW_DELTA;

			while (true) {

				// final boolean losing = score < -50;

				// stop if depth!=1 and no time is left
				if (depth != 1 && !TimeUtil.isTimeLeft()) {
					if (panic) {
						panic = false;
					} else {
						break;
					}
				}

				final int previousScore = score;
				score = calculateBestMove(chessBoard, 0, depth, alpha, beta, 0);

				if (depth > 8 && score + 100 < previousScore && Math.abs(score) < EvalConstants.SCORE_MATE_BOUND) {
					if (Statistics.ENABLED) {
						Statistics.panic = true;
					}
					panic = true;
				}

				if (score <= alpha) {
					if (score < -EvalConstants.SCORE_MATE_BOUND) {
						alpha = Util.SHORT_MIN;
					} else {
						alpha = Math.max(alpha - delta, Util.SHORT_MIN);
					}
					delta += delta / 2;
					TTUtil.setBestMoveInStatistics(chessBoard, ScoreType.ALPHA);
					if (!TimeUtil.isTimeLeft()) {
						if (Statistics.ENABLED) {
							Statistics.panic = true;
						}
						panic = true;
					}
					MainEngine.sendPlyInfo();
				} else if (score >= beta) {
					if (score > EvalConstants.SCORE_MATE_BOUND) {
						beta = Util.SHORT_MAX;
					} else {
						beta = Math.min(beta + delta, Util.SHORT_MAX);
					}
					delta += delta / 2;
					TTUtil.setBestMoveInStatistics(chessBoard, ScoreType.BETA);
					MainEngine.sendPlyInfo();
				} else {
					if (EngineConstants.ENABLE_ASPIRATION) {
						alpha = Math.max(score - delta, Util.SHORT_MIN);
						beta = Math.min(score + delta, Util.SHORT_MAX);
					}
					TTUtil.setBestMoveInStatistics(chessBoard, ScoreType.EXACT);
					MainEngine.sendPlyInfo();
					break;
				}

				if (stop) {
					break;
				}
			}
			if (stop) {
				break;
			}

			depth++;
		}
	}

}
