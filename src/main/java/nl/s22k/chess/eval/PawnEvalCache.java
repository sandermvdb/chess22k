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
	public static final int MAX_TABLE_ENTRIES = 1 << EngineConstants.POWER_2_PAWN_EVAL_ENTRIES;

	private static final long[] keys = new long[MAX_TABLE_ENTRIES];
	private static final long[] passedPawnsAndOutposts = new long[MAX_TABLE_ENTRIES];
	private static final int[] scores = new int[MAX_TABLE_ENTRIES];
	public static int usageCounter;

	public static void clearValues() {
		Arrays.fill(keys, 0);
		Arrays.fill(passedPawnsAndOutposts, 0);
		Arrays.fill(scores, 0);
		usageCounter = 0;
	}

	public static int updateBoardAndGetScore(final ChessBoard cb) {

		if (!EngineConstants.ENABLE_PAWN_EVAL_CACHE) {
			return ChessConstants.CACHE_MISS;
		}

		final int index = getIndex(cb.pawnZobristKey);
		final int score = scores[index];
		final long passedPawnsAndOutpostsValue = passedPawnsAndOutposts[index];

		if ((keys[index] ^ score ^ passedPawnsAndOutpostsValue) == cb.pawnZobristKey) {
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

		final int ttIndex = getIndex(key);

		keys[ttIndex] = key ^ score ^ passedPawnsAndOutpostsValue;
		scores[ttIndex] = score;
		passedPawnsAndOutposts[ttIndex] = passedPawnsAndOutpostsValue;

		if (Statistics.ENABLED) {
			if (keys[ttIndex] == 0) {
				usageCounter++;
			}
		}
	}

	private static int getIndex(final long key) {
		return (int) (key >>> POWER_2_TABLE_SHIFTS);
	}

}
