package nl.s22k.chess.eval;

import java.util.Arrays;

import nl.s22k.chess.Assert;
import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.ChessConstants;
import nl.s22k.chess.Statistics;
import nl.s22k.chess.Util;
import nl.s22k.chess.engine.EngineConstants;

public class PawnEvalCache {

	private static final int POWER_2_TABLE_SHIFTS = 64 - EngineConstants.POWER_2_PAWN_EVAL_ENTRIES;

	// keys, scores, passedPawnsOutposts
	private static final long[] keys = new long[(1 << EngineConstants.POWER_2_PAWN_EVAL_ENTRIES) * 3];

	public static void clearValues() {
		Arrays.fill(keys, 0);
	}

	public static int updateBoardAndGetScore(final ChessBoard cb) {

		if (!EngineConstants.ENABLE_PAWN_EVAL_CACHE) {
			return ChessConstants.CACHE_MISS;
		}

		final int index = getIndex(cb.pawnZobristKey);
		final long xorKey = keys[index];
		final int score = (int) keys[index + 1];
		final long passedPawnsAndOutpostsValue = keys[index + 2];

		if ((xorKey ^ score ^ passedPawnsAndOutpostsValue) == cb.pawnZobristKey) {
			if (Statistics.ENABLED) {
				Statistics.pawnEvalCacheHits++;
			}
			if (!EngineConstants.TEST_EVAL_CACHES) {
				cb.passedPawnsAndOutposts = passedPawnsAndOutpostsValue;
			}
			return score;
		}

		if (Statistics.ENABLED) {
			Statistics.pawnEvalCacheMisses++;
		}
		return ChessConstants.CACHE_MISS;
	}

	public static void addValue(final long key, final int score, final long passedPawnsAndOutpostsValue) {

		if (EngineConstants.ASSERT) {
			Assert.isTrue(score <= Util.SHORT_MAX);
			Assert.isTrue(score >= Util.SHORT_MIN);
		}

		final int index = getIndex(key);
		keys[index] = key ^ score ^ passedPawnsAndOutpostsValue;
		keys[index + 1] = score;
		keys[index + 2] = passedPawnsAndOutpostsValue;
	}

	private static int getIndex(final long key) {
		return (int) (key >>> POWER_2_TABLE_SHIFTS) * 3;
	}

	public static int getUsage() {
		return Util.getUsagePercentage(keys);
	}

}
