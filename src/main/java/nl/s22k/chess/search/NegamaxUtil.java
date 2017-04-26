package nl.s22k.chess.search;

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
import nl.s22k.chess.move.MoveGenerator;
import nl.s22k.chess.move.MoveList;
import nl.s22k.chess.move.MoveUtil;
import nl.s22k.chess.move.MoveWrapper;

public final class NegamaxUtil {

	public static boolean stop = true;
	public static int maxDepth = ChessConstants.MAX_PLIES;

	public static final int MOVE_TT = 0;
	public static final int MOVE_ATTACKING = 1;
	public static final int MOVE_KILLER_1 = 2;
	public static final int MOVE_KILLER_2 = 3;
	public static final int MOVE_QUIET = 4;

	private static final int[] STATIC_NULLMOVE_MARGIN = { 0, 80, 160, 240 }; // 0 is not used
	private static final int[] RAZORING_MARGIN = { 0, 160, 240, 320 }; // 0 is not used

	// public static final MoveWrapper[] playedMoves = new MoveWrapper[4];

	public static int calculateBestMove(final ChessBoard cb, final int ply, int depth, int alpha, int beta, final boolean isNullMove) {

		if (stop) {
			return 0;
		}

		if (EngineConstants.ASSERT) {
			assert depth >= 0 : "Depth = " + depth;
			assert alpha >= Util.SHORT_MIN && alpha <= Util.SHORT_MAX : "Incorrect alpha: " + alpha;
			assert beta >= Util.SHORT_MIN && beta <= Util.SHORT_MAX : "Incorrect beta: " + beta;
		}

		Statistics.maxDepth = Math.max(Statistics.maxDepth, ply);
		if (Statistics.ENABLED) {
			Statistics.abNodes++;
		}

		// we need a bestmove
		if (ply != 0) {
			/* 3-fold repetition check */
			if (EngineConstants.ENABLE_REPETITION_TABLE && RepetitionTable.isRepetition(cb)) {
				if (Statistics.ENABLED) {
					Statistics.repetitions++;
				}
				return EvalConstants.SCORE_DRAW;
			}

			/* draw-by-material */
			if (cb.isDrawByMaterial()) {
				if (Statistics.ENABLED) {
					Statistics.drawByMaterialCount++;
				}
				return EvalConstants.SCORE_DRAW;
			}

			/* bad-bishop endgame */
			if (cb.isBadBishopEndgame()) {
				if (Statistics.ENABLED) {
					Statistics.badBishopEndgameCount++;
				}
				return EvalConstants.SCORE_DRAW;
			}
		}

		/* mate-distance pruning */
		if (EngineConstants.ENABLE_MATE_DISTANCE_PRUNING) {
			alpha = Math.max(alpha, Util.SHORT_MIN + ply);
			beta = Math.min(beta, Util.SHORT_MAX - ply - 1);
			if (alpha >= beta) {
				return alpha;
			}
		}

		/* check-extension */
		// TODO extend discovered checks?
		// TODO extend checks with SEE > 0?
		// TODO extend when mate-threat?
		if (EngineConstants.ENABLE_CHECK_EXTENSION && cb.checkingPieces != 0 && Long.bitCount(cb.allPieces) > 3) {
			depth++;
			if (Statistics.ENABLED) {
				Statistics.extensions++;
			}
		}

		/* quiescence-search */
		if (depth == 0) {
			if (EngineConstants.ENABLE_QUIESCENCE) {
				return QuiescenceUtil.calculateBestMove(cb, ply, alpha, beta);
			}
			return ChessConstants.COLOR_FACTOR[cb.colorToMove] * EvalUtil.calculateScore(cb);
		}

		int score = 0;
		int eval = Util.SHORT_MIN;
		boolean mateThreat = false;

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

		if (beta - alpha <= 1 && cb.checkingPieces == 0 && !cb.hasOnlyPawns(cb.colorToMove)) {

			eval = ChessConstants.COLOR_FACTOR[cb.colorToMove] * EvalUtil.calculateScore(cb);

			/* razoring */
			if (EngineConstants.ENABLE_RAZORING) {
				if (depth < RAZORING_MARGIN.length && Math.abs(beta) < EvalConstants.SCORE_MATE_BOUND) {
					if (eval + RAZORING_MARGIN[depth] < alpha) {
						eval = QuiescenceUtil.calculateBestMove(cb, ply, alpha - RAZORING_MARGIN[depth], beta - RAZORING_MARGIN[depth]);
						if (eval + RAZORING_MARGIN[depth] <= alpha) {
							if (Statistics.ENABLED) {
								Statistics.razoringHit++;
							}
							return eval + RAZORING_MARGIN[depth];
						}
					}
				}
			}

			/* static null move pruning */
			if (EngineConstants.ENABLE_STATIC_NULL_MOVE) {
				if (depth < STATIC_NULLMOVE_MARGIN.length && Math.abs(beta) < EvalConstants.SCORE_MATE_BOUND) {
					if (eval - STATIC_NULLMOVE_MARGIN[depth] >= beta) {
						if (Statistics.ENABLED) {
							Statistics.staticNullMovePruningHit++;
						}
						return eval - STATIC_NULLMOVE_MARGIN[depth];
					}
				}
			}

			/* null-move */
			if (EngineConstants.ENABLE_NULL_MOVE) {
				if (!isNullMove && depth > EngineConstants.NULL_MOVE_R && Long.bitCount(cb.allPieces) > 3) {
					cb.doNullMove();
					score = -calculateBestMove(cb, ply + 1, depth - EngineConstants.NULL_MOVE_R - 1, -beta, -beta + 1, true);
					cb.undoNullMove();
					if (score >= beta) {
						if (Statistics.ENABLED) {
							Statistics.nullMoveHit++;
						}
						return score;
					} else {
						// Detect mate threat
						if (score <= -EvalConstants.SCORE_MATE_BOUND) {
							if (Statistics.ENABLED) {
								Statistics.mateThreat++;
							}
							mateThreat = true;
						}
					}
					if (Statistics.ENABLED) {
						Statistics.nullMoveFail++;
					}
				}
			}
		}

		final int alphaOrig = alpha;
		final boolean wasInCheck = cb.checkingPieces != 0;

		int bestMove = 0;
		int bestScore = Util.SHORT_MIN;
		int cleanTTMove = 0;
		int killerFromToIndex1 = 0;
		int killerFromToIndex2 = 0;
		int move = 0;
		int movesPerformed = 0;
		int losingCapturesIndex = -1;

		MoveList.startPly();
		int phase = MOVE_TT;
		while (phase <= MOVE_QUIET) {
			switch (phase) {
			case MOVE_TT:
				if (ttValue == 0) {
					/* IID */
					if (EngineConstants.ENABLE_IID && depth > 5 && beta - alpha > 1) {
						if (Statistics.ENABLED) {
							Statistics.iidCount++;
						}
						calculateBestMove(cb, ply, depth - EngineConstants.IID_REDUCTION - 1, alpha, beta, false);
						ttValue = TTUtil.getTTValue(cb.zobristKey);
					}
				}
				if (ttValue != 0) {
					move = TTUtil.getCleanMove(ttValue);
					cleanTTMove = move;

					// verify TT-move?
					if (EngineConstants.VERIFY_TT_MOVE) {
						if (!cb.isValidMove(move)) {
							throw new RuntimeException("Invalid tt-move found! " + new MoveWrapper(move) + " - " + cb.toString());
						}
					}

					MoveList.addMove(move);
				}
				break;
			case MOVE_ATTACKING:
				MoveGenerator.generateAttacks(cb);
				if (EngineConstants.ENABLE_SEE) {
					MoveList.setSeeScores(cb);
				}
				MoveList.sort();
				break;
			case MOVE_KILLER_1:
				// TODO skip killer when in-check?
				move = HeuristicUtil.getCleanKiller1(ply);
				if (move != 0 && move != cleanTTMove && cb.isValidQuietMove(move)) {
					killerFromToIndex1 = MoveUtil.getFromToIndex(move);
					MoveList.addMove(move);
				}
				break;
			case MOVE_KILLER_2:
				move = HeuristicUtil.getCleanKiller2(ply);
				if (move != 0 && move != cleanTTMove && cb.isValidQuietMove(move)) {
					killerFromToIndex2 = MoveUtil.getFromToIndex(move);
					MoveList.addMove(move);
				}
				break;
			case MOVE_QUIET:
				MoveGenerator.generateMoves(cb);
				MoveList.setHHScores(cb);

				// check for negative-SEE moves
				if (losingCapturesIndex != -1) {
					MoveList.setIndex(losingCapturesIndex);
				}
				MoveList.sort();
			}

			while (MoveList.hasNext()) {

				move = MoveList.next();

				if (phase == MOVE_ATTACKING) {

					// skip tt-moves
					if (MoveUtil.getCleanMove(move) == cleanTTMove) {
						continue;
					}

					// skip bad captures?
					// if (depth > 4 && movesPerformed > 1 && MoveUtil.getScore(move) < -10) {
					// MoveList.skipMoves();
					// continue;
					// }

					// move bad captures to the end of the list
					if (EngineConstants.ENABLE_SORT_LOSING_CAPTURES && MoveUtil.getScore(move) < -10) {
						losingCapturesIndex = MoveList.getIndex() - 1;
						MoveList.skipMoves();
						continue;
					}
				} else if (phase == MOVE_QUIET) {

					// skip tt- and killer-moves
					if (MoveUtil.getCleanMove(move) == cleanTTMove || MoveUtil.getFromToIndex(move) == killerFromToIndex1
							|| MoveUtil.getFromToIndex(move) == killerFromToIndex2) {
						continue;
					}
				}

				// // record nodecount
				// final long nodeCount = Statistics.moveCount;
				// if (ply < 4) {
				// playedMoves[ply] = new MoveWrapper(move);
				// }

				cb.doMove(move);
				movesPerformed++;
				RepetitionTable.addValue(cb.zobristKey);

				if (EngineConstants.ASSERT) {
					cb.changeSideToMove();
					assert !CheckUtil.isInCheck(cb) : "NM: Just did an illegal move...";
					cb.changeSideToMove();
				}

				score = Util.SHORT_MAX;
				// TODO create constant for LMR_DEPTH
				if (EngineConstants.ENABLE_LMR && depth > 3 && movesPerformed > EngineConstants.LMR_MOVE_COUNTER && MoveUtil.getAttackedPieceIndex(move) == 0
						&& cb.checkingPieces == 0 && !MoveUtil.isPawnPush78(move) && !wasInCheck && !mateThreat) {
					/* LMR */
					score = movesPerformed > 6 ? -calculateBestMove(cb, ply + 1, Math.max(1, depth - 2 - depth / 6), -alpha - 1, -alpha, false)
							: -calculateBestMove(cb, ply + 1, depth - 2, -alpha - 1, -alpha, false);
					if (Statistics.ENABLED) {
						if (score > alpha) {
							Statistics.lmrMoveFail++;
						} else {
							Statistics.lmrMoveHit++;
						}
					}
				} else if (EngineConstants.ENABLE_PVS && movesPerformed > 1) {
					/* PVS */
					// TODO when depth = 1?
					score = -calculateBestMove(cb, ply + 1, depth - 1, -alpha - 1, -alpha, false);
					if (Statistics.ENABLED) {
						if (score > alpha) {
							Statistics.pvsMoveFail++;
						} else {
							Statistics.pvsMoveHit++;
						}
					}
				}
				if (score > alpha) {
					score = -calculateBestMove(cb, ply + 1, depth - 1, -beta, -alpha, false);
				}

				RepetitionTable.removeValue(cb.zobristKey);
				cb.undoMove(move);

				// print nodecount
				// if (ply == 0) {
				// System.out.println(playedMoves[0] + " " + (Statistics.moveCount - nodeCount));
				// } else if (ply == 1) {
				// System.out.println(playedMoves[0] + " " + playedMoves[1] + " " + (Statistics.moveCount - nodeCount));
				// } else if (ply == 2) {
				// System.out.println(playedMoves[0] + " " + playedMoves[1] + " " + playedMoves[2] + " " +
				// (Statistics.moveCount - nodeCount));
				// }
				// else if (ply == 3) {
				// System.out.println(
				// playedMoves[0] + " " + playedMoves[1] + ", " + playedMoves[2] + ", " + playedMoves[3] + " " +
				// (Statistics.moveCount - nodeCount));
				// }

				if (score > bestScore) {
					bestScore = score;
					bestMove = move;
				}
				alpha = Math.max(alpha, score);
				if (alpha >= beta) {

					/* add heuristics if not attack move and not promotion */
					if (MoveUtil.getAttackedPieceIndex(move) == ChessConstants.EMPTY && !MoveUtil.isPromotion(move)) {
						// killer-move
						HeuristicUtil.addKillerMove(MoveUtil.getCleanMove(move), ply);

						// history heuristic
						HeuristicUtil.addHHValue(cb.colorToMove, MoveUtil.getFromToIndex(move), depth);
					}

					phase += 10;
					break;
				}

				if (MoveUtil.getAttackedPieceIndex(move) == ChessConstants.EMPTY && !MoveUtil.isPromotion(move)) {
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
			assert bestMove != 0 : "No bestmove found! (null-move problem?!)";
		}

		// set tt-flag
		int flag = TTUtil.FLAG_EXACT;
		if (bestScore >= beta) {
			flag = TTUtil.FLAG_LOWER;
		} else if (bestScore <= alphaOrig) {
			flag = TTUtil.FLAG_UPPER;
		}

		TTUtil.addValue(cb.zobristKey, bestScore, ply, depth, flag, MoveUtil.getCleanMove(bestMove));

		/* update statistics */
		if (Statistics.ENABLED) {
			if (MoveUtil.getCleanMove(bestMove) == cleanTTMove) {
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
				if (MoveUtil.getScore(bestMove) < 0) {
					Statistics.bestMoveLosingCapture++;
				} else {
					Statistics.bestMoveWinningCapture++;
				}
			} else if (MoveUtil.getFromToIndex(bestMove) == killerFromToIndex1) {
				Statistics.bestMoveKiller1++;
			} else if (MoveUtil.getFromToIndex(bestMove) == killerFromToIndex2) {
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

	public static void start(ChessBoard chessBoard) {
		stop = false;

		if (!TTUtil.isInitialized) {
			TTUtil.init();
		}

		int depth = 1;
		int alpha = Util.SHORT_MIN;
		int beta = Util.SHORT_MAX;

		int previousScore;
		int score = Util.SHORT_MIN;
		boolean panic = false;

		// continue calculating while stop signal has not been given and max depth has not been reached
		while (!stop && depth != maxDepth + 1) {

			Statistics.depth = depth;

			int delta = EngineConstants.ASPIRATION_WINDOW_DELTA;

			while (true) {

				// stop if depth!=1 and no time is left
				if (depth != 1 && !TimeUtil.isTimeLeft()) {
					if (panic) {
						panic = false;
					} else {
						break;
					}
				}

				previousScore = score;
				score = calculateBestMove(chessBoard, 0, depth, alpha, beta, true);

				if (stop) {
					break;
				}

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
					TTUtil.setBestMoveInStatistics(chessBoard, depth, ScoreType.ALPHA);
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
					TTUtil.setBestMoveInStatistics(chessBoard, depth, ScoreType.BETA);
					MainEngine.sendPlyInfo();
				} else {
					if (EngineConstants.ENABLE_ASPIRATION_WINDOW) {
						alpha = Math.max(score - delta, Util.SHORT_MIN);
						beta = Math.min(score + delta, Util.SHORT_MAX);
					}
					TTUtil.setBestMoveInStatistics(chessBoard, depth, ScoreType.EXACT);
					MainEngine.sendPlyInfo();
					break;
				}
			}

			depth++;
		}
		stop = true;
		MainEngine.sendBestMove();
	}

}
