package nl.s22k.chess.eval;

import java.util.Arrays;

import nl.s22k.chess.Statistics;
import nl.s22k.chess.Util;
import nl.s22k.chess.engine.EngineConstants;

public class PawnEvalCache {

	private static final int POWER_2_TABLE_SHIFTS = 64 - EngineConstants.POWER_2_PAWN_EVAL_ENTRIES;
	public static final int MAX_TABLE_ENTRIES = (int) Util.POWER_LOOKUP[EngineConstants.POWER_2_PAWN_EVAL_ENTRIES];

	private static final int[] keys = new int[MAX_TABLE_ENTRIES];
	private static final long[] passedPawns = new long[MAX_TABLE_ENTRIES];
	private static final long[] backwardPawns = new long[MAX_TABLE_ENTRIES];
	private static final int[] scores = new int[MAX_TABLE_ENTRIES];
	public static int usageCounter;

	public static void clearValues() {
		Arrays.fill(keys, 0);
		Arrays.fill(passedPawns, 0);
		Arrays.fill(scores, 0);
		usageCounter = 0;
	}

	public static boolean hasScore(final long zkKey) {
		if (!Statistics.ENABLED) {
			return keys[getZobristIndex(zkKey)] == (int) zkKey;
		}

		if (keys[getZobristIndex(zkKey)] == (int) zkKey) {
			Statistics.pawnEvalCacheHits++;
			return true;
		}

		Statistics.pawnEvalCacheMisses++;
		return false;
	}

	public static int getScore(final long zkKey) {
		return scores[getZobristIndex(zkKey)];
	}

	public static long getPassedPawns(final long key) {
		return passedPawns[getZobristIndex(key)];
	}

	public static long getBackwardPawns(final long key) {
		return backwardPawns[getZobristIndex(key)];
	}

	public static void addValue(final long zobristKey, final int score, final long passedPawnsValue, final long backwardPawnsValue) {

		if (!EngineConstants.ENABLE_PAWN_EVAL_CACHE) {
			return;
		}
		if (EngineConstants.ASSERT) {
			assert score <= Util.SHORT_MAX : "Adding score to pawn-cache > MAX";
			assert score >= Util.SHORT_MIN : "Adding score to pawn-cache < MIN";
		}

		final int ttIndex = getZobristIndex(zobristKey);

		keys[ttIndex] = (int) zobristKey;
		scores[ttIndex] = score;
		passedPawns[ttIndex] = passedPawnsValue;
		backwardPawns[ttIndex] = backwardPawnsValue;

		if (Statistics.ENABLED) {
			if (keys[ttIndex] == 0) {
				usageCounter++;
			}
		}
	}

	private static int getZobristIndex(final long zobristKey) {
		// TODO optimal distribution??
		return (int) (zobristKey >>> POWER_2_TABLE_SHIFTS);
	}

}
