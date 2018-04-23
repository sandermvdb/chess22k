package nl.s22k.chess.eval;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import nl.s22k.chess.Statistics;
import nl.s22k.chess.Util;
import nl.s22k.chess.engine.EngineConstants;

public class MaterialCache {

	private static final int POWER_2_TABLE_SHIFTS = 64 - EngineConstants.POWER_2_MATERIAL_ENTRIES;
	public static final int MAX_TABLE_ENTRIES = 1 << EngineConstants.POWER_2_MATERIAL_ENTRIES;

	private static final long[] keys = new long[MAX_TABLE_ENTRIES];
	private static final int[] scores = new int[MAX_TABLE_ENTRIES];
	public static int usageCounter;

	public static void clearValues() {
		Arrays.fill(keys, 0);
		Arrays.fill(scores, 0);
		usageCounter = 0;
	}

	public static boolean hasScore(final long key) {

		if (!EngineConstants.ENABLE_MATERIAL_CACHE) {
			return false;
		}

		if (!Statistics.ENABLED) {
			return keys[getIndex(key)] == key;
		}

		if (keys[getIndex(key)] == key) {
			Statistics.materialCacheHits++;
			return true;
		}

		Statistics.materialCacheMisses++;
		return false;
	}

	public static int getScore(final long key) {
		return scores[getIndex(key)];
	}

	public static void addValue(final long key, final int score) {

		if (EngineConstants.ASSERT) {
			assertTrue(score <= Util.SHORT_MAX);
			assertTrue(score >= Util.SHORT_MIN);
		}

		final int ttIndex = getIndex(key);

		if (Statistics.ENABLED) {
			if (keys[ttIndex] == 0) {
				usageCounter++;
			}
		}
		keys[ttIndex] = key;
		scores[ttIndex] = score;
	}

	private static int getIndex(final long key) {
		return (int) ((key * 8365193016878897270L) >>> POWER_2_TABLE_SHIFTS);
	}

}
