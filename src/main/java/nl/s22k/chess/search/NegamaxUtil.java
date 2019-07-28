package nl.s22k.chess.search;

import java.util.concurrent.atomic.AtomicInteger;

import nl.s22k.chess.Assert;
import nl.s22k.chess.CheckUtil;
import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.ChessBoardUtil;
import nl.s22k.chess.ChessConstants;
import nl.s22k.chess.Statistics;
import nl.s22k.chess.Util;
import nl.s22k.chess.engine.EngineConstants;
import nl.s22k.chess.engine.MainEngine;
import nl.s22k.chess.eval.EvalConstants;
import nl.s22k.chess.eval.EvalUtil;
import nl.s22k.chess.eval.MaterialUtil;
import nl.s22k.chess.eval.SEEUtil;
import nl.s22k.chess.move.MoveGenerator;
import nl.s22k.chess.move.MoveUtil;
import nl.s22k.chess.move.PV;

public final class NegamaxUtil {

	private static final int PHASE_TT = 0;
	private static final int PHASE_ATTACKING = 1;
	private static final int PHASE_KILLER_1 = 2;
	private static final int PHASE_KILLER_2 = 3;
	private static final int PHASE_COUNTER = 4;
	private static final int PHASE_QUIET = 5;

	// Margins shamelessly stolen from Laser
	private static final int[] STATIC_NULLMOVE_MARGIN = { 0, 60, 130, 210, 300, 400, 510 };
	private static final int[] RAZORING_MARGIN = { 0, 240, 280, 300 };
	private static final int[] FUTILITY_MARGIN = { 0, 80, 170, 270, 380, 500, 630 };
	private static final int[][] LMR_TABLE = new int[64][64];
	static {
		// Ethereal LMR formula with depth and number of performed moves
		for (int depth = 1; depth < 64; depth++) {
			for (int moveNumber = 1; moveNumber < 64; moveNumber++) {
				LMR_TABLE[depth][moveNumber] = (int) (0.5f + Math.log(depth) * Math.log(moveNumber * 1.2f) / 2.5f);
			}
		}
	}

	public static AtomicInteger nrOfActiveThreads = new AtomicInteger(0);
	public static boolean isRunning = false;

	public static int calculateBestMove(final ChessBoard cb, final MoveGenerator moveGen, int ply, int depth, int alpha, int beta, final int nullMoveCounter) {

		if (!isRunning) {
			return ChessConstants.SCORE_NOT_RUNNING;
		}

		if (EngineConstants.ASSERT) {
			Assert.isTrue(depth >= 0);
			Assert.isTrue(alpha >= Util.SHORT_MIN && alpha <= Util.SHORT_MAX);
			Assert.isTrue(beta >= Util.SHORT_MIN && beta <= Util.SHORT_MAX);
		}

		final int alphaOrig = alpha;

		// get extensions
		depth += extensions(cb);

		/* mate-distance pruning */
		if (EngineConstants.ENABLE_MATE_DISTANCE_PRUNING) {
			alpha = Math.max(alpha, Util.SHORT_MIN + ply);
			beta = Math.min(beta, Util.SHORT_MAX - ply - 1);
			if (alpha >= beta) {
				return alpha;
			}
		}

		// TODO JITWatch unpredictable branch
		if (depth == 0) {
			return QuiescenceUtil.calculateBestMove(cb, moveGen, alpha, beta);
		}

		/* transposition-table */
		long ttValue = TTUtil.getValue(cb.zobristKey);
		int score = TTUtil.getScore(ttValue, ply);
		if (ttValue != 0) {
			if (!EngineConstants.TEST_TT_VALUES) {

				if (TTUtil.getDepth(ttValue) >= depth) {
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

		if (Statistics.ENABLED) {
			Statistics.abNodes++;
		}

		int eval = Util.SHORT_MIN;
		final boolean isPv = beta - alpha != 1;
		if (!isPv && cb.checkingPieces == 0) {

			eval = EvalUtil.getScore(cb);

			/* use tt value as eval */
			if (EngineConstants.USE_TT_SCORE_AS_EVAL) {
				if (TTUtil.canRefineEval(ttValue, eval, score)) {
					eval = score;
				}
			}

			/* static null move pruning */
			if (EngineConstants.ENABLE_STATIC_NULL_MOVE && depth < STATIC_NULLMOVE_MARGIN.length) {
				if (eval - STATIC_NULLMOVE_MARGIN[depth] >= beta) {
					if (Statistics.ENABLED) {
						Statistics.staticNullMoved[depth]++;
					}
					return eval;
				}
			}

			/* razoring */
			if (EngineConstants.ENABLE_RAZORING && depth < RAZORING_MARGIN.length && Math.abs(alpha) < EvalConstants.SCORE_MATE_BOUND) {
				if (eval + RAZORING_MARGIN[depth] < alpha) {
					score = QuiescenceUtil.calculateBestMove(cb, moveGen, alpha - RAZORING_MARGIN[depth], alpha - RAZORING_MARGIN[depth] + 1);
					if (score + RAZORING_MARGIN[depth] <= alpha) {
						if (Statistics.ENABLED) {
							Statistics.razored[depth]++;
						}
						return score;
					}
				}
			}

			/* null-move */
			if (EngineConstants.ENABLE_NULL_MOVE) {
				if (nullMoveCounter < 2 && eval >= beta && MaterialUtil.hasNonPawnPieces(cb.materialKey, cb.colorToMove)) {
					cb.doNullMove();
					// TODO less reduction if stm (other side) has only 1 major piece
					final int reduction = depth / 4 + 3 + Math.min((eval - beta) / 80, 3);
					score = depth - reduction <= 0 ? -QuiescenceUtil.calculateBestMove(cb, moveGen, -beta, -beta + 1)
							: -calculateBestMove(cb, moveGen, ply + 1, depth - reduction, -beta, -beta + 1, nullMoveCounter + 1);
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

		final int parentMove = ply == 0 ? 0 : moveGen.previous();
		int bestMove = 0;
		int bestScore = Util.SHORT_MIN - 1;
		int ttMove = 0;
		int counterMove = 0;
		int killer1Move = 0;
		int killer2Move = 0;
		int movesPerformed = 0;

		moveGen.startPly();
		int phase = PHASE_TT;
		while (phase <= PHASE_QUIET) {
			switch (phase) {
			case PHASE_TT:
				if (ttValue != 0) {
					ttMove = TTUtil.getMove(ttValue);

					// verify TT-move?
					if (EngineConstants.VERIFY_TT_MOVE) {
						throw new RuntimeException("verify tt move is not implemented");
					}

					moveGen.addMove(ttMove);
				}
				break;
			case PHASE_ATTACKING:
				// TODO no ordering at ALL-nodes?
				moveGen.generateAttacks(cb);
				moveGen.setMVVLVAScores();
				moveGen.sort();
				break;
			case PHASE_KILLER_1:
				killer1Move = moveGen.getKiller1(ply);
				if (killer1Move != 0 && killer1Move != ttMove && cb.isValidQuietMove(killer1Move)) {
					moveGen.addMove(killer1Move);
					break;
				} else {
					phase++;
				}
			case PHASE_KILLER_2:
				killer2Move = moveGen.getKiller2(ply);
				if (killer2Move != 0 && killer2Move != ttMove && cb.isValidQuietMove(killer2Move)) {
					moveGen.addMove(killer2Move);
					break;
				} else {
					phase++;
				}
			case PHASE_COUNTER:
				counterMove = moveGen.getCounter(cb.colorToMove, parentMove);
				if (counterMove != 0 && counterMove != ttMove && counterMove != killer1Move && counterMove != killer2Move && cb.isValidQuietMove(counterMove)) {
					moveGen.addMove(counterMove);
					break;
				} else {
					phase++;
				}
			case PHASE_QUIET:
				moveGen.generateMoves(cb);
				moveGen.setHHScores(cb.colorToMove);
				moveGen.sort();
			}

			while (moveGen.hasNext()) {
				final int move = moveGen.next();

				if (phase == PHASE_QUIET) {
					if (move == ttMove || move == killer1Move || move == killer2Move || move == counterMove || !cb.isLegal(move)) {
						continue;
					}
				} else if (phase == PHASE_ATTACKING) {
					if (move == ttMove || !cb.isLegal(move)) {
						continue;
					}
				}

				if (EngineConstants.ASSERT) {
					if (MoveUtil.isQuiet(move)) {
						Assert.isTrue(cb.isValidQuietMove(move));
					}
				}

				// pruning allowed?
				if (!isPv && cb.checkingPieces == 0 && movesPerformed > 0 && moveGen.getScore() < 100 && !cb.isDiscoveredMove(MoveUtil.getFromIndex(move))) {

					if (MoveUtil.isQuiet(move)) {

						/* late move pruning */
						if (EngineConstants.ENABLE_LMP && depth <= 4 && movesPerformed >= depth * 3 + 3) {
							if (Statistics.ENABLED) {
								Statistics.lmped[depth]++;
							}
							continue;
						}

						/* futility pruning */
						if (EngineConstants.ENABLE_FUTILITY_PRUNING && depth < FUTILITY_MARGIN.length) {
							if (!MoveUtil.isPawnPush78(move)) {
								if (eval == Util.SHORT_MIN) {
									eval = EvalUtil.getScore(cb);
								}
								if (eval + FUTILITY_MARGIN[depth] <= alpha) {
									if (Statistics.ENABLED) {
										Statistics.futile[depth]++;
									}
									continue;
								}
							}
						}
					}

					/* SEE Pruning */
					else if (EngineConstants.ENABLE_SEE_PRUNING && depth <= 6 && phase == PHASE_ATTACKING
							&& SEEUtil.getSeeCaptureScore(cb, move) < -20 * depth * depth) {
						continue;
					}
				}

				cb.doMove(move);
				movesPerformed++;

				/* draw check */
				if (cb.isRepetition(move) || MaterialUtil.isDrawByMaterial(cb)) {
					score = EvalConstants.SCORE_DRAW;
				} else {
					score = alpha + 1; // initial is above alpha

					if (EngineConstants.ASSERT) {
						cb.changeSideToMove();
						Assert.isTrue(0 == CheckUtil.getCheckingPieces(cb));
						cb.changeSideToMove();
					}

					int reduction = 1;
					if (depth > 2 && movesPerformed > 1 && MoveUtil.isQuiet(move) && !MoveUtil.isPawnPush78(move)) {

						reduction = LMR_TABLE[Math.min(depth, 63)][Math.min(movesPerformed, 63)];
						if (moveGen.getScore() > 40) {
							reduction -= 1;
						}
						if (move == killer1Move || move == killer2Move || move == counterMove) {
							reduction -= 1;
						}
						if (!isPv) {
							reduction += 1;
						}
						reduction = Math.min(depth - 1, Math.max(reduction, 1));
					}

					/* LMR */
					if (EngineConstants.ENABLE_LMR && reduction != 1) {
						score = -calculateBestMove(cb, moveGen, ply + 1, depth - reduction, -alpha - 1, -alpha, 0);
					}

					/* PVS */
					if (EngineConstants.ENABLE_PVS && score > alpha && movesPerformed > 1) {
						score = -calculateBestMove(cb, moveGen, ply + 1, depth - 1, -alpha - 1, -alpha, 0);
					}

					/* normal bounds */
					if (score > alpha) {
						score = -calculateBestMove(cb, moveGen, ply + 1, depth - 1, -beta, -alpha, 0);
					}
				}
				cb.undoMove(move);

				if (!isRunning) {
					moveGen.endPly();
					return ChessConstants.SCORE_NOT_RUNNING;
				}

				if (score > bestScore) {
					bestScore = score;
					bestMove = move;

					if (ply == 0 && SearchThread.getThreadNumber() == 0) {
						PV.set(bestMove, alphaOrig, beta, bestScore, cb);
					}

					alpha = Math.max(alpha, score);
					if (alpha >= beta) {

						if (Statistics.ENABLED) {
							Statistics.failHigh[Math.min(movesPerformed - 1, Statistics.failHigh.length - 1)]++;
						}

						/* killer and history */
						if (MoveUtil.isQuiet(move) && cb.checkingPieces == 0) {
							moveGen.addCounterMove(cb.colorToMove, parentMove, move);
							moveGen.addKillerMove(move, ply);
							moveGen.addHHValue(cb.colorToMove, move, depth);
						}

						phase += 10;
						break;
					}
				}

				if (MoveUtil.isQuiet(move)) {
					moveGen.addBFValue(cb.colorToMove, move, depth);
				}
			}
			phase++;
		}
		moveGen.endPly();

		/* checkmate or stalemate */
		if (movesPerformed == 0) {
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
			Assert.isTrue(bestMove != 0);
		}

		// set tt-flag
		int flag = TTUtil.FLAG_EXACT;
		if (bestScore >= beta) {
			flag = TTUtil.FLAG_LOWER;
		} else if (bestScore <= alphaOrig) {
			flag = TTUtil.FLAG_UPPER;
		}

		if (isRunning) {
			TTUtil.addValue(cb.zobristKey, bestScore, ply, depth, flag, bestMove);
		}

		if (Statistics.ENABLED) {
			Statistics.setBestMove(cb, bestMove, ttMove, ttValue, flag, counterMove, killer1Move, killer2Move);
		}

		if (EngineConstants.TEST_TT_VALUES) {
			SearchTestUtil.testTTValues(score, bestScore, depth, bestMove, flag, ttValue, ply);
		}

		return bestScore;
	}

	private static int extensions(final ChessBoard cb) {
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

	public static void start(final ChessBoard cb) {

		isRunning = true;
		cb.moveCount = 0;
		TTUtil.init(false);

		PV.init(cb);

		if (MainEngine.nrOfThreads == 1) {
			SearchThread thread = new SearchThread(0);
			NegamaxUtil.nrOfActiveThreads.incrementAndGet();
			thread.run();
			isRunning = false;
		} else {

			// start slave threads
			for (int i = 1; i < MainEngine.nrOfThreads; i++) {
				ChessBoardUtil.copy(cb, ChessBoard.getInstance(i));
				NegamaxUtil.nrOfActiveThreads.incrementAndGet();
				new SearchThread(i).start();
			}

			// start main thread
			SearchThread thread = new SearchThread(0);
			NegamaxUtil.nrOfActiveThreads.incrementAndGet();
			thread.run();
			isRunning = false;

			// wait for all slave threads to be ready
			while (nrOfActiveThreads.get() != 0) {
				Thread.yield();
			}
		}
	}

}
