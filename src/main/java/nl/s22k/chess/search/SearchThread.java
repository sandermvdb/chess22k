package nl.s22k.chess.search;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.Statistics;
import nl.s22k.chess.Util;
import nl.s22k.chess.engine.EngineConstants;
import nl.s22k.chess.engine.MainEngine;
import nl.s22k.chess.eval.EvalConstants;
import nl.s22k.chess.move.MoveGenerator;

public class SearchThread extends Thread {

	// Laser based SMP skip
	private static final int[] SMP_SKIP_DEPTHS = { 1, 1, 2, 2, 2, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4 };
	private static final int[] SMP_SKIP_AMOUNT = { 1, 2, 1, 2, 3, 1, 2, 3, 4, 5, 1, 2, 3, 4, 5, 6 };
	private static final int SMP_MAX_CYCLES = SMP_SKIP_AMOUNT.length;

	private static final Map<Long, Integer> THREAD_MAP = new ConcurrentHashMap<>();
	private int threadNumber;

	public SearchThread(final int threadNumber) {
		this.threadNumber = threadNumber;
	}

	public static final int getThreadNumber() {
		return THREAD_MAP.get(Thread.currentThread().getId());
	}

	@Override
	public void run() {

		THREAD_MAP.put(Thread.currentThread().getId(), threadNumber);

		int threadNumber = THREAD_MAP.get(Thread.currentThread().getId());
		Thread.currentThread().setName("chess22k-search " + threadNumber);
		int cycleIndex = (threadNumber - 1) % SMP_MAX_CYCLES;

		int depth = 0;
		int alpha = Util.SHORT_MIN;
		int beta = Util.SHORT_MAX;
		int score = Util.SHORT_MIN;
		int previousScore;
		boolean panic = false;

		MoveGenerator.getInstance(threadNumber).clearHistoryHeuristics();

		while (depth < MainEngine.maxDepth && NegamaxUtil.isRunning) {

			depth++;

			if (threadNumber == 0) {
				Statistics.depth = depth;
			} else {
				if ((depth + cycleIndex) % SMP_SKIP_DEPTHS[cycleIndex] == 0) {
					depth += SMP_SKIP_AMOUNT[cycleIndex];
					if (depth > MainEngine.maxDepth) {
						continue;
					}
				}
			}

			int delta = EngineConstants.ASPIRATION_WINDOW_DELTA;

			while (NegamaxUtil.isRunning) {

				if (threadNumber == 0 && depth != 1 && !TimeUtil.isTimeLeft()) {
					if (panic) {
						// only panic once
						panic = false;
					} else {
						break;
					}
				}

				previousScore = score;

				// System.out.println("start " + threadNumber + " " + depth);
				score = NegamaxUtil.calculateBestMove(ChessBoard.getInstance(threadNumber), MoveGenerator.getInstance(threadNumber), 0, depth, alpha, beta, 0);
				// System.out.println("done " + threadNumber + " " + depth);

				if (threadNumber == 0) {
					MainEngine.sendPlyInfo();
				}
				if (score + 100 < previousScore && Math.abs(score) < EvalConstants.SCORE_MATE_BOUND) {
					panic = true;
				}
				if (score <= alpha && alpha != Util.SHORT_MIN) {
					if (!TimeUtil.isTimeLeft()) {
						panic = true;
					}
					if (score < -1000) {
						alpha = Util.SHORT_MIN;
						beta = Util.SHORT_MAX;
					} else {
						alpha = Math.max(alpha - delta, Util.SHORT_MIN);
					}
					delta *= 2;
				} else if (score >= beta && beta != Util.SHORT_MAX) {
					if (score > 1000) {
						alpha = Util.SHORT_MIN;
						beta = Util.SHORT_MAX;
					} else {
						beta = Math.min(beta + delta, Util.SHORT_MAX);
					}
					delta *= 2;
				} else {
					if (EngineConstants.ENABLE_ASPIRATION && depth > 5) {
						if (Math.abs(score) > 1000) {
							alpha = Util.SHORT_MIN;
							beta = Util.SHORT_MAX;
						} else {
							delta = (delta + EngineConstants.ASPIRATION_WINDOW_DELTA) / 2;
							alpha = Math.max(score - delta, Util.SHORT_MIN);
							beta = Math.min(score + delta, Util.SHORT_MAX);
						}
					}
					break;
				}
			}
		}
		NegamaxUtil.nrOfActiveThreads.decrementAndGet();
	}
}
