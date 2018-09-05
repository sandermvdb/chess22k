package nl.s22k.chess.search;

import java.util.concurrent.atomic.AtomicInteger;

import nl.s22k.chess.Assert;
import nl.s22k.chess.CheckUtil;
import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.ChessBoardUtil;
import nl.s22k.chess.ChessConstants;
import nl.s22k.chess.ChessConstants.ScoreType;
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
import nl.s22k.chess.move.MoveWrapper;

public final class NegamaxUtil {

	private static final int PHASE_TT = 0;
	private static final int PHASE_ATTACKING = 1;
	private static final int PHASE_KILLER_1 = 2;
	private static final int PHASE_KILLER_2 = 3;
	private static final int PHASE_QUIET = 4;

	// Margins shamelessly stolen from Laser
	private static final int[] STATIC_NULLMOVE_MARGIN = { 0, 60, 130, 210, 300, 400, 510 };
	private static final int[] RAZORING_MARGIN = { 0, 240, 280, 300 };
	private static final int[] FUTILITY_MARGIN = { 0, 80, 170, 270, 380, 500, 630 };

	public static AtomicInteger nrOfActiveSlaveThreads = new AtomicInteger(0);
	public static AtomicInteger mode = new AtomicInteger(Mode.STOP);

	private static MoveGenerator[] moveGens = new MoveGenerator[EngineConstants.MAX_THREADS];
	static {
		for (int i = 0; i < moveGens.length; i++) {
			moveGens[i] = new MoveGenerator();
		}
	}

	public static int calculateBestMove(final ChessBoard cb, final MoveGenerator moveGen, final int ply, int depth, int alpha, int beta,
			final int nullMoveCounter) {

		if (mode.get() != Mode.START) {
			return 0;
		}

		if (EngineConstants.ASSERT) {
			Assert.isTrue(depth >= 0);
			Assert.isTrue(alpha >= Util.SHORT_MIN && alpha <= Util.SHORT_MAX);
			Assert.isTrue(beta >= Util.SHORT_MIN && beta <= Util.SHORT_MAX);
		}

		// get extensions
		depth += extensions(cb, moveGen, ply);

		// TODO JITWatch unpredictable branch
		if (depth == 0) {
			return QuiescenceUtil.calculateBestMove(cb, moveGen, alpha, beta);
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

		/* transposition-table */
		long ttValue = TTUtil.getTTValue(cb.zobristKey);
		int score = TTUtil.getScore(ttValue, ply);
		if (ttValue != 0 && ply != 0) {
			if (!EngineConstants.TEST_TT_VALUES) {

				if (TTUtil.getDepth(ttValue) >= (depth)) {
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

			eval = EvalUtil.getScore(cb);

			/* use tt value as eval */
			if (ttValue != 0) {
				if (TTUtil.getFlag(ttValue) == TTUtil.FLAG_EXACT || TTUtil.getFlag(ttValue) == TTUtil.FLAG_UPPER && score < eval
						|| TTUtil.getFlag(ttValue) == TTUtil.FLAG_LOWER && score > eval) {
					eval = score;
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
				if (depth < RAZORING_MARGIN.length && Math.abs(alpha) < EvalConstants.SCORE_MATE_BOUND) {
					if (eval + RAZORING_MARGIN[depth] < alpha) {
						final int q = QuiescenceUtil.calculateBestMove(cb, moveGen, alpha - RAZORING_MARGIN[depth], alpha - RAZORING_MARGIN[depth] + 1);
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
				if (nullMoveCounter < 2 && MaterialUtil.hasNonPawnPieces(cb.materialKey, cb.colorToMove)) {
					cb.doNullMove();
					// TODO less reduction if stm (other side) has only 1 major piece
					final int reduction = 2 + depth / 3;
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

		final int alphaOrig = alpha;
		final boolean wasInCheck = cb.checkingPieces != 0;

		int bestMove = 0;
		int bestScore = Util.SHORT_MIN;
		int ttMove = 0;
		int killer1Move = 0;
		int killer2Move = 0;
		int movesPerformed = 0;

		moveGen.startPly();
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
							calculateBestMove(cb, moveGen, ply, depth - EngineConstants.IID_REDUCTION - 1, alpha, beta, 0);
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
				if (killer1Move != 0 && killer1Move != ttMove && cb.isValidQuietMove(killer1Move) && cb.isLegal(killer1Move)) {
					moveGen.addMove(killer1Move);
				}
				break;
			case PHASE_KILLER_2:
				killer2Move = moveGen.getKiller2(ply);
				if (killer2Move != 0 && killer2Move != ttMove && cb.isValidQuietMove(killer2Move) && cb.isLegal(killer2Move)) {
					moveGen.addMove(killer2Move);
				}
				break;
			case PHASE_QUIET:
				moveGen.generateMoves(cb);
				moveGen.setHHScores(cb.colorToMove);
				moveGen.sort();
			}

			while (moveGen.hasNext()) {

				final int moveScore = moveGen.getNextScore();
				final int move = MoveUtil.getCleanMove(moveGen.next());

				if (phase == PHASE_QUIET) {
					if (move == ttMove || move == killer1Move || move == killer2Move || !cb.isLegal(move)) {
						continue;
					}
				} else if (phase == PHASE_ATTACKING) {
					if (move == ttMove || !cb.isLegal(move)) {
						continue;
					}
				}

				// pruning allowed?
				if (!isPv && cb.checkingPieces == 0 && movesPerformed > 0 && moveScore < 100 && !cb.isDiscoveredMove(MoveUtil.getFromIndex(move))) {

					if (MoveUtil.isQuiet(move)) {

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
							if (!MoveUtil.isPawnPush78(move)) {
								if (depth < FUTILITY_MARGIN.length) {
									if (eval == Util.SHORT_MIN) {
										eval = EvalUtil.getScore(cb);
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

					/* SEE Pruning */
					else if (depth <= 6 && phase == PHASE_ATTACKING && SEEUtil.getSeeCaptureScore(cb, move) < -20 * depth * depth) {
						continue;
					}
				}

				cb.doMove(move);
				movesPerformed++;

				/* draw check */
				if (cb.isRepetition() || cb.isDrawByMaterial()) {
					score = EvalConstants.SCORE_DRAW;
				} else {
					score = alpha + 1; // initial is above alpha

					if (EngineConstants.ASSERT) {
						cb.changeSideToMove();
						Assert.isTrue(0 == CheckUtil.getCheckingPieces(cb));
						cb.changeSideToMove();
					}

					if (EngineConstants.ENABLE_LMR && moveScore < 40 && movesPerformed > 2 && depth > 3 && MoveUtil.isQuiet(move) && cb.checkingPieces == 0
							&& !wasInCheck && !MoveUtil.isPawnPush678(move, cb.colorToMoveInverse)) {
						/* LMR */
						final int reduction = move != killer1Move && moveScore < 20 && movesPerformed > 6 ? Math.min(depth - 1, 2 + depth / 6) : 2;
						score = -calculateBestMove(cb, moveGen, ply + 1, depth - reduction, -alpha - 1, -alpha, 0);
						if (score > alpha) {
							score = -calculateBestMove(cb, moveGen, ply + 1, depth - 1, -alpha - 1, -alpha, 0);
							if (Statistics.ENABLED) {
								Statistics.lmrMoveMiss++;
							}
						} else if (Statistics.ENABLED) {
							Statistics.lmrMoveHit++;
						}
					} else if (EngineConstants.ENABLE_PVS && movesPerformed > 1) {
						/* PVS */
						score = -calculateBestMove(cb, moveGen, ply + 1, depth - 1, -alpha - 1, -alpha, 0);
						if (Statistics.ENABLED) {
							if (score > alpha) {
								Statistics.pvsMoveMiss++;
							} else {
								Statistics.pvsMoveHit++;
							}
						}
					}
					if (score > alpha) {
						score = -calculateBestMove(cb, moveGen, ply + 1, depth - 1, -beta, -alpha, 0);
					}

				}
				cb.undoMove(move);

				if (mode.get() != Mode.START) {
					moveGen.endPly();
					return 0;
				}

				if (score > bestScore) {
					bestScore = score;
					bestMove = move;
					alpha = Math.max(alpha, score);
				}
				if (alpha >= beta) {

					if (Statistics.ENABLED) {
						Statistics.failHigh[Math.min(movesPerformed - 1, Statistics.failHigh.length - 1)]++;
					}

					/* killer and history */
					if (MoveUtil.isQuiet(move)) {
						moveGen.addKillerMove(move, ply);
						moveGen.addHHValue(cb.colorToMove, move, depth);
					}

					phase += 10;
					break;
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
				if (SEEUtil.getSeeCaptureScore(cb, bestMove) < 0) {
					Statistics.bestMoveLosingCapture++;
				} else {
					Statistics.bestMoveWinningCapture++;
				}
			} else if (bestMove == killer1Move && cb.checkingPieces == 0) {
				Statistics.bestMoveKiller1++;
			} else if (bestMove == killer2Move && cb.checkingPieces == 0) {
				Statistics.bestMoveKiller2++;
			} else if (bestMove == killer1Move && cb.checkingPieces != 0) {
				Statistics.bestMoveKillerEvasive1++;
			} else if (bestMove == killer2Move && cb.checkingPieces != 0) {
				Statistics.bestMoveKillerEvasive2++;
			} else {
				Statistics.bestMoveOther++;
			}
		}

		if (EngineConstants.TEST_TT_VALUES) {
			if (ttValue != 0 && TTUtil.getDepth(ttValue) == depth) {
				if (TTUtil.getFlag(ttValue) == TTUtil.FLAG_EXACT && flag == TTUtil.FLAG_EXACT) {
					score = TTUtil.getScore(ttValue, ply);
					if (score != bestScore) {
						throw new RuntimeException(String.format("Error: TT-score %s, bestScore %s", score, bestScore));
					}
				}
			}
		}

		return bestScore;
	}

	private static int extensions(final ChessBoard cb, final MoveGenerator moveGen, final int ply) {
		/* extension when the pawn endgame starts */
		if (EngineConstants.ENABLE_ENDGAME_EXTENSION && ply > 0 && MoveUtil.getAttackedPieceIndex(moveGen.previous()) > ChessConstants.PAWN
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

	public static void start(ChessBoard cb) {
		start(cb, 1);
	}

	public static void start(final ChessBoard chessBoard, final int nrOfThreads) {

		try {

			mode.set(Mode.START);
			chessBoard.moveCount = 0;
			moveGens[0].clearHeuristicTables();
			if (nrOfThreads > 1) {
				ChessBoard.initInstances(nrOfThreads - 1);
				for (int i = 0; i < nrOfThreads - 1; i++) {
					ChessBoardUtil.copy(chessBoard, ChessBoard.getInstance(i));
					moveGens[i + 1].clearHeuristicTables();
				}
			}

			TTUtil.init(false);

			int depth = 0;
			int alpha = Util.SHORT_MIN;
			int beta = Util.SHORT_MAX;
			int score = Util.SHORT_MIN;
			boolean panic = false;

			while (depth != MainEngine.maxDepth + 1 && mode.get() != Mode.STOP) {

				depth++;
				Statistics.depth = depth;

				int delta = EngineConstants.ASPIRATION_WINDOW_DELTA;

				while (mode.get() != Mode.STOP) {

					// stop if depth!=1 and no time is left
					if (depth != 1 && !TimeUtil.isTimeLeft()) {
						if (panic) {
							panic = false;
						} else {
							break;
						}
					}

					final int previousScore = score;
					if (nrOfThreads == 1 || depth < 8) {
						score = calculateBestMove(chessBoard, moveGens[0], 0, depth, alpha, beta, 0);
					} else {
						// start slave threads
						for (int i = 1; i < nrOfThreads; i++) {
							if (mode.get() == Mode.ANY_SLAVE_READY) {
								break;
							}
							NegamaxUtil.nrOfActiveSlaveThreads.incrementAndGet();
							new SearchThread(ChessBoard.getInstance(i - 1), moveGens[i], depth + (i % 2), alpha, beta).start();
						}
						// long now = System.currentTimeMillis();
						Thread.sleep(1);
						while (nrOfActiveSlaveThreads.get() != nrOfThreads - 1 && mode.get() != Mode.ANY_SLAVE_READY && mode.get() != Mode.STOP) {
							// System.out.println("Wait till all slave threads have started or any slave is ready.
							// Mode=" + mode.get());
							Thread.yield();
						}
						// System.out.println((System.currentTimeMillis() - now) + " starting slaves time");

						// start main thread
						if (mode.get() == Mode.START) {
							score = calculateBestMove(chessBoard, moveGens[0], 0, depth, alpha, beta, 0);
							mode.compareAndSet(Mode.START, Mode.STOP_SLAVES);
						}

						// wait for all slave threads to be ready
						// now = System.currentTimeMillis();
						while (nrOfActiveSlaveThreads.get() != 0) {
							// System.out.println("Wait till all slave threads have stopped");
							Thread.yield();
						}
						// System.out.println((System.currentTimeMillis() - now) + " stopping slaves time");
						mode.compareAndSet(Mode.STOP_SLAVES, Mode.START);

						// use slave score if finished sooner
						if (mode.compareAndSet(Mode.ANY_SLAVE_READY, Mode.START)) {
							// System.out.println("Slave threads already finished");
							score = TTUtil.getScore(TTUtil.getTTValue(chessBoard.zobristKey), 0);
						}
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
							beta = Util.SHORT_MAX;
						} else {
							alpha = Math.max(alpha - delta, Util.SHORT_MIN);
						}
						delta *= 2;
						TTUtil.setScoreInStatistics(chessBoard);
						if (!TimeUtil.isTimeLeft()) {
							if (Statistics.ENABLED) {
								Statistics.panic = true;
							}
							panic = true;
						}
						MainEngine.sendPlyInfo();
					} else if (score >= beta) {
						if (score > EvalConstants.SCORE_MATE_BOUND) {
							alpha = Util.SHORT_MIN;
							beta = Util.SHORT_MAX;
						} else {
							beta = Math.min(beta + delta, Util.SHORT_MAX);
						}
						delta *= 2;
						TTUtil.setBestMoveInStatistics(chessBoard, ScoreType.BETA);
						MainEngine.sendPlyInfo();
					} else {
						if (EngineConstants.ENABLE_ASPIRATION) {
							if (Math.abs(score) > EvalConstants.SCORE_MATE_BOUND) {
								alpha = Util.SHORT_MIN;
								beta = Util.SHORT_MAX;
							} else {
								alpha = Math.max(score - delta, Util.SHORT_MIN);
								beta = Math.min(score + delta, Util.SHORT_MAX);
							}
						}
						TTUtil.setBestMoveInStatistics(chessBoard, ScoreType.EXACT);
						MainEngine.sendPlyInfo();
						break;
					}
				}
			}

			mode.set(Mode.STOP);

		} catch (InterruptedException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public static long getTotalMoveCount() {
		long totalMoveCount = ChessBoard.getInstance().moveCount;
		for (int i = 0; i < MainEngine.nrOfThreads - 1; i++) {
			totalMoveCount += ChessBoard.getInstance(i).moveCount;
		}
		return totalMoveCount;
	}

}
