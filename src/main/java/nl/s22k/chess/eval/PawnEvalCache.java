package nl.s22k.chess.eval;

import java.util.Arrays;

import nl.s22k.chess.ChessConstants;
import nl.s22k.chess.Statistics;
import nl.s22k.chess.Util;
import nl.s22k.chess.engine.EngineConstants;

public class PawnEvalCache {

	private static final int POWER_2_TABLE_SHIFTS = 64 - EngineConstants.POWER_2_PAWN_EVAL_ENTRIES;
	public static final int MAX_TABLE_ENTRIES = (int) Util.POWER_LOOKUP[EngineConstants.POWER_2_PAWN_EVAL_ENTRIES];

	private static final int[] keys = new int[MAX_TABLE_ENTRIES];
	private static final short[][] passerFiles = new short[2][MAX_TABLE_ENTRIES];
	private static final short[] scores = new short[MAX_TABLE_ENTRIES];
	public static int usageCounter;

	public static void clearValues() {
		Arrays.fill(keys, 0);
		Arrays.fill(passerFiles[0], (short) 0);
		Arrays.fill(passerFiles[1], (short) 0);
		Arrays.fill(scores, (short) 0);
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

	public static short getScore(final long zkKey) {
		return scores[getZobristIndex(zkKey)];
	}

	public static int getPasserFiles(final long zkKey, final int color) {
		return passerFiles[color][getZobristIndex(zkKey)] & 255;
	}

	public static int getProtectedPasserFiles(final long zkKey, final int color) {
		return passerFiles[color][getZobristIndex(zkKey)] >>> 8 & 255;
	}

	public static void addValue(final long zobristKey, final int score, final int whitePasserFiles, final int blackPasserFiles,
			final int whiteProtectedPasserFiles, final int blackProtectedPasserFiles) {

		if (EngineConstants.ASSERT) {
			assert score <= Util.SHORT_MAX : "Adding score to pawn-cache > MAX";
			assert score >= Util.SHORT_MIN : "Adding score to pawn-cache < MIN";
			assert whitePasserFiles >= 0 && whitePasserFiles <= 255 : "Adding incorrect whitePassers value to pawn-eval-cache";
			assert blackPasserFiles >= 0 && whitePasserFiles <= 255 : "Adding incorrect blackPassers value to pawn-eval-cache";
		}

		final int ttIndex = getZobristIndex(zobristKey);

		keys[ttIndex] = (int) zobristKey;
		scores[ttIndex] = (short) score;
		passerFiles[ChessConstants.WHITE][ttIndex] = (short) (whiteProtectedPasserFiles << 8 | whitePasserFiles);
		passerFiles[ChessConstants.BLACK][ttIndex] = (short) (blackProtectedPasserFiles << 8 | blackPasserFiles);

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
