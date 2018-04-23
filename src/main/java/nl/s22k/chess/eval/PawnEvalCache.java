package nl.s22k.chess.eval;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import nl.s22k.chess.Statistics;
import nl.s22k.chess.Util;
import nl.s22k.chess.engine.EngineConstants;

public class PawnEvalCache {

	private static final int POWER_2_TABLE_SHIFTS = 64 - EngineConstants.POWER_2_PAWN_EVAL_ENTRIES;
	public static final int MAX_TABLE_ENTRIES = 1 << EngineConstants.POWER_2_PAWN_EVAL_ENTRIES;

	private static final long[] keys = new long[MAX_TABLE_ENTRIES];
	private static final long[] passedPawns = new long[MAX_TABLE_ENTRIES];
	private static final long[] whiteOutposts = new long[MAX_TABLE_ENTRIES];
	private static final long[] blackOutposts = new long[MAX_TABLE_ENTRIES];
	private static final int[] scores = new int[MAX_TABLE_ENTRIES];
	public static int usageCounter;

	public static void clearValues() {
		Arrays.fill(keys, 0);
		Arrays.fill(passedPawns, 0);
		Arrays.fill(whiteOutposts, 0);
		Arrays.fill(blackOutposts, 0);
		Arrays.fill(scores, 0);
		usageCounter = 0;
	}

	public static boolean hasScore(final long key) {

		if (!EngineConstants.ENABLE_PAWN_EVAL_CACHE) {
			return false;
		}

		if (!Statistics.ENABLED) {
			return keys[getIndex(key)] == key;
		}

		if (keys[getIndex(key)] == key) {
			Statistics.pawnEvalCacheHits++;
			return true;
		}

		Statistics.pawnEvalCacheMisses++;
		return false;
	}

	public static int getScore(final long key) {
		return scores[getIndex(key)];
	}

	public static long getPassedPawns(final long key) {
		return passedPawns[getIndex(key)];
	}

	public static long getWhiteOutposts(final long key) {
		return whiteOutposts[getIndex(key)];
	}

	public static long getBlackOutposts(final long key) {
		return blackOutposts[getIndex(key)];
	}

	public static void addValue(final long key, final int score, final long passedPawnsValue, final long whiteOutpostsValue, final long blackOutpostsValue) {

		if (EngineConstants.ASSERT) {
			assertTrue(score <= Util.SHORT_MAX);
			assertTrue(score >= Util.SHORT_MIN);
		}

		final int ttIndex = getIndex(key);

		keys[ttIndex] = key;
		scores[ttIndex] = score;
		passedPawns[ttIndex] = passedPawnsValue;
		whiteOutposts[ttIndex] = whiteOutpostsValue;
		blackOutposts[ttIndex] = blackOutpostsValue;

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
