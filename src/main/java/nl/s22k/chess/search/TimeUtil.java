package nl.s22k.chess.search;

import nl.s22k.chess.Statistics;

public class TimeUtil {

	private static long timeWindow = Long.MAX_VALUE;

	public static void setInfiniteWindow() {
		timeWindow = Long.MAX_VALUE;
	}

	public static void setTimeWindow(final long totalTimeLeft, final int moveCount, int movesToGo) {
		if (movesToGo != 0) {
			// safety margin for last move
			movesToGo += 2;

			// if we have more than 50% of the time left, continue with next ply
			timeWindow = (long) (totalTimeLeft / movesToGo * 0.5);
			return;
		}

		if (moveCount >= 80) {
			// end-game (80+)
			timeWindow = totalTimeLeft / 40;
		} else if (moveCount >= 40) {
			// middle-game (40-80)
			timeWindow = (long) (totalTimeLeft / (100 - moveCount) / 1.8);
		} else {
			// start-game (0-40)
			timeWindow = totalTimeLeft / (100 - moveCount) / 1;
		}
	}

	public static boolean isTimeLeft() {
		return System.currentTimeMillis() - Statistics.startTime < timeWindow;
	}

	public static long getMaxTime() {
		// we have a maximum of 3 times the calculated window (or Long.MAX when analyzing)
		return Math.min(Long.MAX_VALUE, timeWindow * 3);
	}

}
