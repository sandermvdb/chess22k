package nl.s22k.chess.search;

import nl.s22k.chess.Statistics;

public class TimeUtil {

	private static long timeWindowNs = Long.MAX_VALUE;

	public static void setInfiniteWindow() {
		timeWindowNs = Long.MAX_VALUE;
	}

	public static void setTimeWindow(long totalTimeLeftMs, final int moveCount, int movesToGo) {
		if (totalTimeLeftMs == Long.MAX_VALUE) {
			timeWindowNs = Long.MAX_VALUE;
			return;
		}

		if (movesToGo != 0) {
			// safety margin for last move
			movesToGo += 2;

			// if we have more than 50% of the time left, continue with next ply
			timeWindowNs = 1000000 * totalTimeLeftMs / movesToGo / 2;
			return;
		}

		if (moveCount > 50) {
			// every move gets less and less time
			timeWindowNs = 1000000 * totalTimeLeftMs / 50;
		} else {
			// first 50 moves get 50% of the total time
			timeWindowNs = 1000000 * totalTimeLeftMs / (100 - moveCount);
		}
	}

	public static boolean isTimeLeft() {
		return System.nanoTime() - Statistics.startTime < timeWindowNs;
	}

	public static long getMaxTimeMs() {
		// we have a maximum of 3 times the calculated window (or Long.MAX when analyzing)
		return Math.min(Long.MAX_VALUE, timeWindowNs / 1000000 * 3);
	}

}
