package nl.s22k.chess.search;

import java.util.Arrays;

import nl.s22k.chess.Assert;
import nl.s22k.chess.ChessConstants;
import nl.s22k.chess.Statistics;
import nl.s22k.chess.Util;
import nl.s22k.chess.engine.EngineConstants;
import nl.s22k.chess.eval.EvalConstants;
import nl.s22k.chess.move.MoveUtil;
import nl.s22k.chess.move.MoveWrapper;

public class TTUtil {

	private static int keyShifts;

	// key, value
	private static long[] keys;

	public static final int FLAG_EXACT = 0;
	public static final int FLAG_UPPER = 1;
	public static final int FLAG_LOWER = 2;

	public static long halfMoveCounter = 0;

	private static final int BUCKET_SIZE = 4;

	// ///////////////////// DEPTH //10 bits
	private static final int FLAG = 10; // 2
	private static final int MOVE = 12; // 22
	private static final int SCORE = 48; // 16

	public static boolean isInitialized = false;

	public static void init(final boolean force) {
		if (force || !isInitialized) {
			keyShifts = 64 - EngineConstants.POWER_2_TT_ENTRIES;
			int maxEntries = (int) (Util.POWER_LOOKUP[EngineConstants.POWER_2_TT_ENTRIES] + BUCKET_SIZE - 1) * 2;

			keys = new long[maxEntries];

			isInitialized = true;
		}
	}

	public static void clearValues() {
		Arrays.fill(keys, 0);
	}

	public static long getValue(final long key) {

		final int index = getIndex(key);

		for (int i = index; i < index + BUCKET_SIZE * 2; i += 2) {
			long xorKey = keys[i];
			long value = keys[i + 1];
			if ((xorKey ^ value) == key) {
				if (Statistics.ENABLED) {
					Statistics.ttHits++;
				}
				return value;
			}
		}

		if (Statistics.ENABLED) {
			Statistics.ttMisses++;
		}
		return 0;
	}

	private static int getIndex(final long key) {
		return (int) (key >>> keyShifts) << 1;
	}

	public static void addValue(final long key, int score, final int ply, final int depth, final int flag, final int move) {

		if (EngineConstants.ASSERT) {
			Assert.isTrue(depth >= 1);
			Assert.isTrue(move != 0);
			Assert.isTrue(score >= Util.SHORT_MIN && score <= Util.SHORT_MAX);
			Assert.isTrue(MoveUtil.getSourcePieceIndex(move) != 0);
			Assert.isTrue(score != ChessConstants.SCORE_NOT_RUNNING);
		}

		final int index = getIndex(key);
		long replacedDepth = Integer.MAX_VALUE;
		int replaceIndex = index;
		for (int i = index; i < index + BUCKET_SIZE * 2; i += 2) {

			long xorKey = keys[i];
			if (xorKey == 0) {
				replaceIndex = i;
				break;
			}

			long currentValue = keys[i + 1];
			int currentDepth = getDepth(currentValue);
			if ((xorKey ^ currentValue) == key) {
				if (currentDepth > depth && flag != FLAG_EXACT) {
					return;
				}
				replaceIndex = i;
				break;
			}

			// replace the lowest depth
			if (currentDepth < replacedDepth) {
				replaceIndex = i;
				replacedDepth = currentDepth;
			}
		}

		// correct mate-score
		if (score > EvalConstants.SCORE_MATE_BOUND) {
			score = score + ply;
		} else if (score < -EvalConstants.SCORE_MATE_BOUND) {
			score = score - ply;
		}
		if (EngineConstants.ASSERT) {
			Assert.isTrue(score >= Util.SHORT_MIN && score <= Util.SHORT_MAX);
		}

		final long value = createValue(score, move, flag, depth);
		keys[replaceIndex] = key ^ value;
		keys[replaceIndex + 1] = value;
	}

	public static int getScore(final long value, final int ply) {
		int score = (int) (value >> SCORE);

		// correct mate-score
		if (score > EvalConstants.SCORE_MATE_BOUND) {
			score -= ply;
		} else if (score < -EvalConstants.SCORE_MATE_BOUND) {
			score += ply;
		}

		if (EngineConstants.ASSERT) {
			Assert.isTrue(score >= Util.SHORT_MIN && score <= Util.SHORT_MAX);
		}

		return score;
	}

	public static int getDepth(final long value) {
		return (int) ((value & 0x3ff) - halfMoveCounter);
	}

	public static int getFlag(final long value) {
		return (int) (value >>> FLAG & 3);
	}

	public static int getMove(final long value) {
		return (int) (value >>> MOVE & 0x3fffff);
	}

	// SCORE,HALF_MOVE_COUNTER,MOVE,FLAG,DEPTH
	public static long createValue(final long score, final long move, final long flag, final long depth) {
		if (EngineConstants.ASSERT) {
			Assert.isTrue(score >= Util.SHORT_MIN && score <= Util.SHORT_MAX);
			Assert.isTrue(depth <= 255);
		}
		return score << SCORE | move << MOVE | flag << FLAG | (depth + halfMoveCounter);
	}

	public static String toString(long ttValue) {
		return "score=" + TTUtil.getScore(ttValue, 0) + " " + new MoveWrapper(getMove(ttValue)) + " depth=" + TTUtil.getDepth(ttValue) + " flag="
				+ TTUtil.getFlag(ttValue);
	}

	public static void setSizeMB(int value) {
		switch (value) {
		case 1:
		case 2:
		case 4:
		case 8:
		case 16:
		case 32:
		case 64:
		case 128:
		case 256:
		case 512:
		case 1024:
		case 2048:
		case 4096:
		case 8192:
		case 16384:
			int power2Entries = (int) (Math.log(value) / Math.log(2) + 16);
			if (EngineConstants.POWER_2_TT_ENTRIES != power2Entries) {
				EngineConstants.POWER_2_TT_ENTRIES = power2Entries;
				init(true);
			}
			break;
		default:
			throw new RuntimeException("Hash-size must be between 1-16384 mb and a multiple of 2");
		}
	}

	public static long getUsagePercentage() {
		int usage = 0;
		for (int i = 0; i < 1000; i++) {
			if (keys[i] != 0) {
				usage++;
			}
		}
		return usage;
	}

	public static boolean canRefineEval(final long ttValue, final int eval, final int score) {
		if (ttValue != 0) {
			if (getFlag(ttValue) == FLAG_EXACT || getFlag(ttValue) == FLAG_UPPER && score < eval || getFlag(ttValue) == FLAG_LOWER && score > eval) {
				return true;
			}
		}
		return false;
	}
}
