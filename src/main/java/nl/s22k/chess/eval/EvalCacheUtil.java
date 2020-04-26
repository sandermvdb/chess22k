package nl.s22k.chess.eval;

import nl.s22k.chess.Assert;
import nl.s22k.chess.ChessConstants;
import nl.s22k.chess.Statistics;
import nl.s22k.chess.Util;
import nl.s22k.chess.engine.EngineConstants;

public class EvalCacheUtil {

	private static final int POWER_2_TABLE_SHIFTS = 64 - EngineConstants.POWER_2_EVAL_ENTRIES;

	public static int getScore(final long key, final int[] evalCache) {
		final int index = getIndex(key);

		if (evalCache[index] == (int) key) {
			if (Statistics.ENABLED) {
				Statistics.evalCacheHits++;
			}
			return evalCache[index + 1];
		}

		if (Statistics.ENABLED) {
			Statistics.evalCacheMisses++;
		}
		return ChessConstants.CACHE_MISS;
	}

	public static void addValue(final long key, final int score, final int[] evalCache) {
		if (!EngineConstants.ENABLE_EVAL_CACHE) {
			return;
		}
		if (EngineConstants.ASSERT) {
			Assert.isTrue(score <= Util.SHORT_MAX);
			Assert.isTrue(score >= Util.SHORT_MIN);
		}

		final int index = getIndex(key);
		evalCache[index] = (int) key;
		evalCache[index + 1] = score;
	}

	private static int getIndex(final long key) {
		return (int) (key >>> POWER_2_TABLE_SHIFTS) << 1;
	}

}
