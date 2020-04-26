package nl.s22k.chess.eval;

import nl.s22k.chess.Assert;
import nl.s22k.chess.ChessConstants;
import nl.s22k.chess.Statistics;
import nl.s22k.chess.Util;
import nl.s22k.chess.engine.EngineConstants;

public class MaterialCacheUtil {

	private static final int POWER_2_TABLE_SHIFTS = 64 - EngineConstants.POWER_2_MATERIAL_ENTRIES;

	public static int getScore(final int key, final int[] materialCache) {

		if (!EngineConstants.ENABLE_MATERIAL_CACHE) {
			return ChessConstants.CACHE_MISS;
		}

		final int index = getIndex(key);

		if (materialCache[index] == key) {
			if (Statistics.ENABLED) {
				Statistics.materialCacheHits++;
			}
			return materialCache[index + 1];
		}

		if (Statistics.ENABLED) {
			Statistics.materialCacheMisses++;
		}
		return ChessConstants.CACHE_MISS;
	}

	public static void addValue(final int key, final int score, final int[] materialCache) {

		if (!EngineConstants.ENABLE_MATERIAL_CACHE) {
			return;
		}

		if (EngineConstants.ASSERT) {
			Assert.isTrue(score <= Util.SHORT_MAX);
			Assert.isTrue(score >= Util.SHORT_MIN);
		}

		final int index = getIndex(key);
		materialCache[index] = key;
		materialCache[index + 1] = score;
	}

	private static int getIndex(final int materialKey) {
		return ((materialKey * 836519301) >>> POWER_2_TABLE_SHIFTS) << 1;
	}

}
