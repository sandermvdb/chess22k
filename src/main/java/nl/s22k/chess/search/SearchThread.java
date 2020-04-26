package nl.s22k.chess.search;

import java.util.concurrent.Callable;

import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.ChessBoardInstances;
import nl.s22k.chess.Util;
import nl.s22k.chess.engine.EngineConstants;
import nl.s22k.chess.engine.ErrorLogger;
import nl.s22k.chess.engine.MainEngine;
import nl.s22k.chess.engine.UciOut;

public class SearchThread implements Callable<Void> {

	// Laser based SMP skip
	private static final int[] SMP_SKIP_DEPTHS = { 1, 1, 2, 2, 2, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4 };
	private static final int[] SMP_SKIP_AMOUNT = { 1, 2, 1, 2, 3, 1, 2, 3, 4, 5, 1, 2, 3, 4, 5, 6 };
	private static final int SMP_MAX_CYCLES = SMP_SKIP_AMOUNT.length;

	private int threadNumber;
	private ChessBoard cb;
	private ThreadData threadData;

	public SearchThread(final int threadNumber) {
		this.threadNumber = threadNumber;
		cb = ChessBoardInstances.get(threadNumber);
		threadData = ThreadData.getInstance(threadNumber);
	}

	@Override
	public Void call() {
		try {
			Thread.currentThread().setName("chess22k-search-" + threadNumber);
			if (threadNumber == 0) {
				runMain();
				NegamaxUtil.isRunning = false;
			} else {
				runHelper();
			}
		} catch (Throwable t) {
			ErrorLogger.log(ChessBoardInstances.get(threadNumber), t, false);
		}
		return null;
	}

	private void runMain() {
		threadData.clearHistoryHeuristics();
		threadData.initPV(cb);

		int depth = 0;
		int score = 0;
		int alpha;
		int beta;
		boolean failLow = false;

		while (NegamaxUtil.isRunning) {
			if (depth == MainEngine.maxDepth) {
				return;
			}

			depth++;

			int delta = EngineConstants.ENABLE_ASPIRATION && depth > 5 && Math.abs(score) < 1000 ? EngineConstants.ASPIRATION_WINDOW_DELTA : Util.SHORT_MAX * 2;
			alpha = Math.max(score - delta, Util.SHORT_MIN);
			beta = Math.min(score + delta, Util.SHORT_MAX);

			while (NegamaxUtil.isRunning) {
				if (!TimeUtil.isTimeLeft() && depth != 1 && !failLow) {
					return;
				}

				// System.out.println("start " + threadNumber + " " + depth);
				score = NegamaxUtil.calculateBestMove(cb, threadData, 0, depth, alpha, beta, 0);
				// System.out.println("done " + threadNumber + " " + depth);

				UciOut.sendPlyInfo(threadData);
				failLow = false;
				if (score <= alpha) {
					failLow = true;
					alpha = Math.max(alpha - delta, Util.SHORT_MIN);
					delta *= 2;
				} else if (score >= beta) {
					beta = Math.min(beta + delta, Util.SHORT_MAX);
					delta *= 2;
				} else {
					break;
				}
			}
		}
	}

	private void runHelper() {
		threadData.clearHistoryHeuristics();
		int cycleIndex = (threadNumber - 1) % SMP_MAX_CYCLES;

		int depth = 0;
		int score = 0;
		int alpha;
		int beta;

		while (depth < MainEngine.maxDepth && NegamaxUtil.isRunning) {

			depth++;
			if ((depth + cycleIndex) % SMP_SKIP_DEPTHS[cycleIndex] == 0) {
				depth += SMP_SKIP_AMOUNT[cycleIndex];
				if (depth > MainEngine.maxDepth) {
					return;
				}
			}

			int delta = EngineConstants.ENABLE_ASPIRATION && depth > 5 && Math.abs(score) < 1000 ? EngineConstants.ASPIRATION_WINDOW_DELTA : Util.SHORT_MAX * 2;
			alpha = Math.max(score - delta, Util.SHORT_MIN);
			beta = Math.min(score + delta, Util.SHORT_MAX);

			while (NegamaxUtil.isRunning) {

				// System.out.println("start " + threadNumber + " " + depth);
				score = NegamaxUtil.calculateBestMove(cb, threadData, 0, depth, alpha, beta, 0);
				// System.out.println("done " + threadNumber + " " + depth);

				if (score <= alpha) {
					alpha = Math.max(alpha - delta, Util.SHORT_MIN);
					delta *= 2;
				} else if (score >= beta) {
					beta = Math.min(beta + delta, Util.SHORT_MAX);
					delta *= 2;
				} else {
					break;
				}
			}
		}
	}
}
