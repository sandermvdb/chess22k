package nl.s22k.chess.eval;

import nl.s22k.chess.Assert;
import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.ChessConstants;
import nl.s22k.chess.Statistics;
import nl.s22k.chess.Util;
import nl.s22k.chess.engine.EngineConstants;

public class PawnCacheUtil {

	private static final int POWER_2_TABLE_SHIFTS = 64 - EngineConstants.POWER_2_PAWN_EVAL_ENTRIES;

	public static int updateBoardAndGetScore(final ChessBoard cb, final long[] pawnCache) {

		if (!EngineConstants.ENABLE_PAWN_EVAL_CACHE) {
			return ChessConstants.CACHE_MISS;
		}

		final int index = getIndex(cb.pawnZobristKey);
		if (pawnCache[index] == cb.pawnZobristKey) {
			if (Statistics.ENABLED) {
				Statistics.pawnEvalCacheHits++;
			}
			if (!EngineConstants.TEST_EVAL_CACHES) {
				cb.passedPawnsAndOutposts = pawnCache[index + 1];
			}
			return (int) pawnCache[index + 2];
		}

		if (Statistics.ENABLED) {
			Statistics.pawnEvalCacheMisses++;
		}
		return ChessConstants.CACHE_MISS;
	}

	public static void addValue(final long key, final int score, final long passedPawnsAndOutpostsValue, final long[] pawnCache) {

		if (!EngineConstants.ENABLE_PAWN_EVAL_CACHE) {
			return;
		}

		if (EngineConstants.ASSERT) {
			Assert.isTrue(score <= Util.SHORT_MAX);
			Assert.isTrue(score >= Util.SHORT_MIN);
		}

		final int index = getIndex(key);
		pawnCache[index] = key;
		pawnCache[index + 1] = passedPawnsAndOutpostsValue;
		pawnCache[index + 2] = score;
	}

	private static int getIndex(final long key) {
		return (int) (key >>> POWER_2_TABLE_SHIFTS) * 3;
	}

}
