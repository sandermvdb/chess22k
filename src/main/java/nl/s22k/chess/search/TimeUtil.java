package nl.s22k.chess.search;

import nl.s22k.chess.Statistics;

public class TimeUtil {

	private static long timeWindowNs = Long.MAX_VALUE;

	public static void setInfiniteWindow() {
		timeWindowNs = Long.MAX_VALUE;
	}

	public static void setTimeWindow(long totalTimeLeftMs, final int moveCount, int movesToGo) {
		if (movesToGo != 0) {
			// safety margin for last move
			movesToGo += 2;

			// if we have more than 50% of the time left, continue with next ply
			timeWindowNs = 1000000 * totalTimeLeftMs / movesToGo / 2;
			return;
		}

		if (moveCount >= 80) {
			// end-game (80+)
			timeWindowNs = 1000000 * totalTimeLeftMs / 40;
		} else if (moveCount >= 40) {
			// middle-game (40-80)
			timeWindowNs = (long) (1000000 * totalTimeLeftMs / (100 - moveCount) / 1.8);
		} else {
			// start-game (0-40)
			timeWindowNs = 1000000 * totalTimeLeftMs / (100 - moveCount) / 1;
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
