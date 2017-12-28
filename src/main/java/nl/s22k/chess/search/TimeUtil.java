package nl.s22k.chess.search;

import nl.s22k.chess.Statistics;

public class TimeUtil {

	private static final int MAX_TIME_FACTOR = 3;

	private static final int MOVE_MARGIN = 1;

	private static boolean isExactMoveTime = false;
	private static int movesToGo = -1;
	private static int moveCount;
	private static long timeWindowNs;
	private static long totalTimeLeftMs = Long.MAX_VALUE;
	private static boolean isTTHit;
	private static boolean isLosing;
	private static long maxTimeMs;

	public static void setInfiniteWindow() {
		timeWindowNs = Long.MAX_VALUE;
	}

	public static void start() {
		if (isExactMoveTime) {
			// we depend on the max-time thread
			return;
		}
		if (totalTimeLeftMs == Long.MAX_VALUE) {
			timeWindowNs = Long.MAX_VALUE;
			return;
		}

		if (movesToGo == -1) {
			if (moveCount <= 50) {
				// first 50 moves get 50% of the total time
				timeWindowNs = 1_000_000 * totalTimeLeftMs / (100 - moveCount);
			} else {
				// every next move gets less and less time
				timeWindowNs = 1_000_000 * totalTimeLeftMs / 50;
			}
		} else {
			// safety margin for last move (sometimes we take more time than our time slot)
			// if we have more than 50% of the time left, continue with next ply
			int moveMargin = movesToGo == 1 ? 0 : MOVE_MARGIN;
			timeWindowNs = 1_000_000 * totalTimeLeftMs / (movesToGo + moveMargin) / 2;
		}

		if (movesToGo == 1) {
			// always leave at least 200msec in the last move
			maxTimeMs = Math.max(50, totalTimeLeftMs - 200);
		} else {
			// increase timewindow if we don't have a TT hit
			if (isTTHit && !isLosing) {
				// max time is 3 times the window
				maxTimeMs = timeWindowNs / 1_000_000 * 3;
			} else {
				// double timewindow but only double max time
				timeWindowNs *= 2;
				maxTimeMs = timeWindowNs / 1_000_000 * 2;
			}
		}

	}

	public static long getMaxTimeMs() {
		// we have a maximum of 3 times the calculated window
		return maxTimeMs;
	}

	public static void setExactMoveTime(int moveTimeMs) {
		isExactMoveTime = true;
		timeWindowNs = moveTimeMs * 1_000_000 / MAX_TIME_FACTOR;
	}

	public static void setSimpleTimeWindow(final long thinkingTimeMs) {
		// if we have more than 50% of the time left, continue with next ply
		timeWindowNs = 1_000_000 * thinkingTimeMs / 2;
	}

	public static boolean isTimeLeft() {
		if (isExactMoveTime) {
			return true;
		}
		return System.nanoTime() - Statistics.startTime < timeWindowNs;
	}

	public static void reset() {
		isExactMoveTime = false;
		movesToGo = -1;
		totalTimeLeftMs = Integer.MAX_VALUE;
		isLosing = false;
	}

	public static void setMovesToGo(int movesToGo) {
		TimeUtil.movesToGo = movesToGo;
	}

	public static void setTotalTimeLeft(int totalTimeLeftMs) {
		TimeUtil.totalTimeLeftMs = totalTimeLeftMs;
	}

	public static void setMoveCount(int moveCount) {
		TimeUtil.moveCount = moveCount;
	}

	public static void setTTHit(boolean isTTHit) {
		TimeUtil.isTTHit = isTTHit;
	}

	public static void setLosing(boolean isLosing) {
		TimeUtil.isLosing = isLosing;
	}

}
