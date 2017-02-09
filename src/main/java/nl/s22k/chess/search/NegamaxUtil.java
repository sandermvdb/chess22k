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

public final class NegamaxUtil {

	// TODO put test-methods in separate class

	public static MainEngine chessEngine;
	public static boolean stop = false;

	public static final int MOVE_TT = 0;
	public static final int MOVE_ATTACKING = 1;
	public static final int MOVE_KILLER_1 = 2;
	public static final int MOVE_KILLER_2 = 3;
	public static final int MOVE_QUIET = 4;

	public static int calculateBestMove(final ChessBoard cb, final int ply, int depth, int alpha, int beta, final boolean isNullMove) {

		if (stop) {
			return 0;
		}

		if (EngineConstants.TEST_VALUES) {
			if (depth < 0) {
				System.out.println("Depth = " + depth);
			}
			if (alpha > Util.SHORT_MAX) {
				System.out.println("Negamax: alpha > MAX: " + alpha);
			} else if (alpha < Util.SHORT_MIN) {
				System.out.println("Negamax: alpha < MIN: " + alpha);
			}
			if (beta > Util.SHORT_MAX) {
				System.out.println("Negamax: beta > MAX: " + beta);
			} else if (beta < Util.SHORT_MIN) {
				System.out.println("Negamax: beta < MIN: " + beta);
			}

		}

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

		// we need a bestmove
		if (ply != 0) {
			/* 3-fold repetition check */
			if (EngineConstants.ENABLE_REPETITION_TABLE && RepetitionTable.isRepetition(cb)) {
				if (Statistics.ENABLED) {
					Statistics.repetitions++;
				}
				return EvalConstants.SCORE_DRAW_3FOLD;
			}

			/* bad-bishop endgame */
			if (cb.isBadBishopEndgame()) {
				if (Statistics.ENABLED) {
					Statistics.badBishopEndgameCount++;
				}
				return EvalConstants.SCORE_DRAW_BAD_BISHOP_ENDGAME;
			}
		}

		/* check-extension */
		if (EngineConstants.ENABLE_CHECK_EXTENSION && cb.checkingPieces != 0) {
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
			return -1 * cb.colorFactor * EvalUtil.calculateScore(cb);
		}

		int score = 0;

		/* transposition-table */
		long ttValue = TTUtil.getTTValue(cb.zobristKey);
		if (ttValue != 0) {
			if (!EngineConstants.TEST_TT_VALUES) {

				if (TTUtil.getDepth(ttValue) >= (depth)) {
					score = TTUtil.getScore(ttValue, ply);

					if (EngineConstants.TEST_VALUES) {
						if (score > Util.SHORT_MAX) {
							System.out.println("Adding score to tt > MAX: " + score);
						} else if (score < Util.SHORT_MIN) {
							System.out.println("Adding score to tt < MIN: " + score);
						}
					}

					switch (TTUtil.getFlag(ttValue)) {
					case TTUtil.FLAG_EXACT:
						return score;
					case TTUtil.FLAG_LOWER:
						if (score >= beta) {
							return score;
						}
						// wikipedia:
						// alpha = Math.max(alpha, score);
						break;
					case TTUtil.FLAG_UPPER:
						if (score <= alpha) {
							return score;
						}
						// wikipedia:
						// beta = Math.min(beta, score);
					}
				}
			}
		}

		/* null-move */
		if (EngineConstants.ENABLE_NULL_MOVE) {
			if (beta - alpha <= 1 && !isNullMove && cb.checkingPieces == 0 && depth > EngineConstants.NULL_MOVE_R && !cb.hasOnlyPawns(cb.colorToMove)) {
				cb.doNullMove();
				score = -calculateBestMove(cb, ply + 1, depth - EngineConstants.NULL_MOVE_R - 1, -beta, -beta + 1, true);
				cb.undoNullMove();
				if (score >= beta) {
					if (Statistics.ENABLED) {
						Statistics.nullMoveHit++;
					}
					return score;
				}
				if (Statistics.ENABLED) {
					Statistics.nullMoveFail++;
				}
			}
		}

		final int alphaOrig = alpha;
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
					move = TTUtil.getMove(ttValue);
					cleanTTMove = move;
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
				move = HeuristicUtil.getCleanKiller1(ply);
				if (move != cleanTTMove && cb.isValidKillerMove(move)) {
					killerFromToIndex1 = MoveUtil.getFromToIndex(move);
					MoveList.addMove(move);
				}
				break;
			case MOVE_KILLER_2:
				move = HeuristicUtil.getCleanKiller2(ply);
				if (move != cleanTTMove && cb.isValidKillerMove(move)) {
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

					// skip ALL losing captures
					if (EngineConstants.ENABLE_SORT_LOSING_CAPTURES && MoveUtil.getScore(move) < -10) {
						losingCapturesIndex = MoveList.getIndex() - 1;
						while (MoveList.hasNext()) {
							MoveList.next();
						}
						continue;
					}
				} else if (phase == MOVE_QUIET) {

					// skip tt- and killer-moves
					if (MoveUtil.getCleanMove(move) == cleanTTMove || MoveUtil.getFromToIndex(move) == killerFromToIndex1
							|| MoveUtil.getFromToIndex(move) == killerFromToIndex2) {
						continue;
					}
				}

				cb.doMove(move);
				movesPerformed++;
				RepetitionTable.addValue(cb.zobristKey);

				if (EngineConstants.TEST_VALUES) {
					cb.changeSideToMove();
					if (CheckUtil.isInCheck(cb)) {
						System.out.println("NM: Just did an illegal move...");
					}
					cb.changeSideToMove();
				}

				score = Util.SHORT_MAX;
				// TODO if-statement order
				// TODO create constant for LMR_DEPTH
				if (EngineConstants.ENABLE_LMR && depth > 3 && movesPerformed >= EngineConstants.LMR_MOVE_COUNTER && MoveUtil.getZKAttackedPieceIndex(move) == 0
						&& cb.checkingPieces == 0) {
					/* LMR */
					if (EngineConstants.TEST_VALUES) {
						if (MoveUtil.isPromotion(move)) {
							if (MoveUtil.isNightPromotion(move)) {
								// System.out.println("Night-promotion-move found in LMR");
							} else {
								System.out.println("Queen-promotion-move found in LMR");
							}
						}
					}
					score = -calculateBestMove(cb, ply + 1, depth - 2, -alpha - 1, -alpha, false);
					if (score > alpha) {
						if (Statistics.ENABLED) {
							Statistics.lmrMoveFail++;
						}
					} else {
						if (Statistics.ENABLED) {
							Statistics.lmrMoveHit++;
						}
					}
				} else if (EngineConstants.ENABLE_PVS && MoveList.currentMoveCounter() > 1) {
					/* PVS */
					// TODO when in-check?
					// TODO when depth = 1?
					score = -calculateBestMove(cb, ply + 1, depth - 1, -alpha - 1, -alpha, false);
					if (score > alpha) {
						if (Statistics.ENABLED) {
							Statistics.pvsMoveFail++;
						}
					} else {
						if (Statistics.ENABLED) {
							Statistics.pvsMoveHit++;
						}
					}
				}
				if (score > alpha) {
					score = -calculateBestMove(cb, ply + 1, depth - 1, -beta, -alpha, false);
				}

				RepetitionTable.removeValue(cb.zobristKey);
				cb.undoMove(move);

				if (score > bestScore) {
					bestScore = score;
					bestMove = move;
				}
				alpha = Math.max(alpha, score);
				if (alpha >= beta) {

					/* add heuristics if not attack move and not promotion */
					if (MoveUtil.getZKAttackedPieceIndex(move) == ChessConstants.EMPTY && !MoveUtil.isPromotion(move)) {
						// killer-move
						HeuristicUtil.addKillerMove(MoveUtil.getCleanMove(move), ply);

						// history heuristic
						HeuristicUtil.addHHValue(cb.colorToMove, MoveUtil.getFromToIndex(move), depth);
					}

					phase += 10;
					break;
				}

				if (MoveUtil.getZKAttackedPieceIndex(move) == ChessConstants.EMPTY && !MoveUtil.isPromotion(move)) {
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
				return EvalConstants.SCORE_DRAW_STALEMATE;
			} else {
				if (Statistics.ENABLED) {
					Statistics.mateCount++;
				}
				return Util.SHORT_MIN + ply;
			}
		}

		if (EngineConstants.TEST_VALUES) {
			if (bestMove == 0) {
				System.out.println("Negamax: No bestmove found! (null-move problem?!)");
			}
		}

		// set tt-flag
		int flag = TTUtil.FLAG_EXACT;
		if (bestScore <= alphaOrig) {
			flag = TTUtil.FLAG_UPPER;
		} else if (bestScore >= beta) {
			flag = TTUtil.FLAG_LOWER;
		}

		/* add TT value, if not repetition, except at the root because we need a bestmove */
		if (score != 0 || ply == 0) {
			// do not add 3-fold-repetition scores
			TTUtil.addValue(cb.zobristKey, bestScore, ply, depth, flag, MoveUtil.getCleanMove(bestMove));
		}

		/* update statistics */
		if (Statistics.ENABLED) {
			if (MoveUtil.getCleanMove(bestMove) == cleanTTMove) {
				Statistics.bestMoveTT++;
			} else if (MoveUtil.isPromotion(bestMove)) {
				Statistics.bestMovePromotion++;
			} else if (MoveUtil.getZKAttackedPieceIndex(bestMove) != 0) {
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

	public static void start(ChessBoard chessBoard, int depth) {
		stop = false;

		Statistics.depth = depth;
		calculateBestMove(chessBoard, 0, depth, Util.SHORT_MIN, Util.SHORT_MAX, true);
		TTUtil.setBestMoveInStatistics(chessBoard, depth, ScoreType.EXACT);
		chessEngine.sendPlyInfo();
		chessEngine.sendBestMove();
	}

	public static void start(ChessBoard chessBoard, long msecLeft) {
		stop = false;

		int depth = 1;
		int alpha = Util.SHORT_MIN;
		int beta = Util.SHORT_MAX;

		int score = 0;

		// continue calculating while stop signal has not been given and max depth has not been reached
		while (!stop && depth != ChessConstants.MAX_PLIES) {

			// always continue if we are in analyzing mode or if depth=1
			if (msecLeft != Long.MAX_VALUE && depth != 1) {
				// stop if time has run out or only 1 root-move is possible
				if (!IDUtil.isTimeLeft(chessBoard, msecLeft)) {
					break;
				}
			}

			Statistics.depth = depth;

			// set minimal window if TT contains a lot of PV moves to prevent if from calculating ply 23 if 22 came from
			// the TT
			int delta;
			if (depth > 12 && Statistics.maxDepth == 0) {
				alpha = score;
				beta = score;
				delta = 2;
			} else {
				delta = EngineConstants.ASPIRATION_WINDOW_DELTA;
			}
			while (true) {
				// stop if stop command is received
				if (stop) {
					break;
				}
				// stop if depth!=1 and no time is left
				if (depth != 1 && !IDUtil.isTimeLeft(chessBoard, msecLeft)) {
					break;
				}

				score = calculateBestMove(chessBoard, 0, depth, alpha, beta, true);

				if (score <= alpha) {
					alpha = Math.max(alpha - delta, Util.SHORT_MIN);
					if (depth > 12 && Statistics.maxDepth == 0) {
						delta++;
					} else {
						delta += delta / 2;
					}
					TTUtil.setBestMoveInStatistics(chessBoard, depth, ScoreType.ALPHA);
					chessEngine.sendPlyInfo();
				} else if (score >= beta) {
					beta = Math.min(beta + delta, Util.SHORT_MAX);
					if (depth > 12 && Statistics.maxDepth == 0) {
						delta++;
					} else {
						delta += delta / 2;
					}
					TTUtil.setBestMoveInStatistics(chessBoard, depth, ScoreType.BETA);
					chessEngine.sendPlyInfo();
				} else {
					if (EngineConstants.ENABLE_ASPIRATION_WINDOW) {
						alpha = Math.max(score - delta, Util.SHORT_MIN);
						beta = Math.min(score + delta, Util.SHORT_MAX);
					}
					TTUtil.setBestMoveInStatistics(chessBoard, depth, ScoreType.EXACT);
					chessEngine.sendPlyInfo();
					break;
				}
			}

			depth++;
		}
		chessEngine.sendBestMove();
	}

}
