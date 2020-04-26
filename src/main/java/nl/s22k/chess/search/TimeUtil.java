package nl.s22k.chess.search;

import nl.s22k.chess.engine.MainEngine;

public class TimeUtil {

	public static long startTime = System.nanoTime();

	private static int movesToGo;
	private static int moveCount;
	private static int increment;
	private static long timeWindowNs;
	private static long totalTimeLeftMs;
	private static long maxTimeMs;
	private static boolean isTTHit;
	private static boolean isExactMoveTime;

	static {
		reset();
	}

	public static void reset() {
		startTime = System.nanoTime();
		isExactMoveTime = false;
		movesToGo = -1;
		totalTimeLeftMs = Integer.MAX_VALUE;
		maxTimeMs = Long.MAX_VALUE;
		timeWindowNs = Long.MAX_VALUE;
		increment = 0;
		isTTHit = false;
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
			int incrementWindow = increment < totalTimeLeftMs / 2 ? increment / 2 : 0;
			if (moveCount <= 40) {
				// first 40 moves get 50% of the total time
				timeWindowNs = 1_000_000 * (totalTimeLeftMs / (80 - moveCount) + incrementWindow);
			} else {
				// every next move gets less and less time
				timeWindowNs = 1_000_000 * (totalTimeLeftMs / 50 + incrementWindow / 2);
			}
		} else {
			// if we have more than 50% of the time left, continue with next ply
			timeWindowNs = 1_000_000 * totalTimeLeftMs / movesToGo / 2;
		}

		if (!isTTHit) {
			timeWindowNs *= 2;
		}

		switch (movesToGo) {
		case 1:
			maxTimeMs = Math.max(50, totalTimeLeftMs - 200);
			break;
		case 2:
		case 3:
		case 4:
			maxTimeMs = totalTimeLeftMs / movesToGo;
			break;
		default:
			maxTimeMs = timeWindowNs / 1_000_000 * 4;
		}

	}

	public static long getMaxTimeMs() {
		return maxTimeMs;
	}

	public static void setExactMoveTime(int moveTimeMs) {
		isExactMoveTime = true;
		maxTimeMs = moveTimeMs;
	}

	public static void setSimpleTimeWindow(final long thinkingTimeMs) {
		// if we have more than 50% of the time left, continue with next ply
		timeWindowNs = 1_000_000 * thinkingTimeMs / 2;
	}

	public static boolean isTimeLeft() {
		if (isExactMoveTime) {
			return true;
		}
		if (MainEngine.pondering) {
			return true;
		}
		return System.nanoTime() - startTime < timeWindowNs;
	}

	public static long getPassedTimeMs() {
		return (System.nanoTime() - startTime) / 1000000;
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

	public static void setTTHit() {
		TimeUtil.isTTHit = true;
	}

	public static void setIncrement(int increment) {
		TimeUtil.increment = increment;
	}

}
