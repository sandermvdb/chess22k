package nl.s22k.chess.eval;

import java.util.Arrays;

import nl.s22k.chess.Statistics;
import nl.s22k.chess.Util;
import nl.s22k.chess.engine.EngineConstants;

public class EvalCache {

	private static final int POWER_2_TABLE_SHIFTS = 64 - EngineConstants.POWER_2_EVAL_ENTRIES;
	public static final int MAX_TABLE_ENTRIES = (int) Util.POWER_LOOKUP[EngineConstants.POWER_2_EVAL_ENTRIES];

	private static final int[] keys = new int[MAX_TABLE_ENTRIES];
	private static final short[] scores = new short[MAX_TABLE_ENTRIES];
	public static int usageCounter;

	public static void clearValues() {
		Arrays.fill(keys, 0);
		Arrays.fill(scores, (short) 0);
		usageCounter = 0;
	}

	public static boolean hasScore(final long zkKey) {
		if (!Statistics.ENABLED) {
			return keys[getZobristIndex(zkKey)] == (int) zkKey;
		}

		if (keys[getZobristIndex(zkKey)] == (int) zkKey) {
			Statistics.evalCacheHits++;
			return true;
		}

		Statistics.evalCacheMisses++;
		return false;
	}

	public static short getScore(final long zkKey) {
		return scores[getZobristIndex(zkKey)];
	}

	public static void addValue(final long zobristKey, final int score) {
		if (EngineConstants.ASSERT) {
			assert score <= Util.SHORT_MAX : "Adding score to eval-cache > MAX";
			assert score >= Util.SHORT_MIN : "Adding score to eval-cache < MIN";
		}

		final int ttIndex = getZobristIndex(zobristKey);

		if (Statistics.ENABLED) {
			if (keys[ttIndex] == 0) {
				usageCounter++;
			}
		}
		keys[ttIndex] = (int) zobristKey;
		scores[ttIndex] = (short) score;
	}

	private static int getZobristIndex(final long zobristKey) {
		// TODO optimal distribution??
		return (int) (zobristKey >>> POWER_2_TABLE_SHIFTS);
	}

}
