package nl.s22k.chess.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nl.s22k.chess.Assert;
import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.ChessConstants.ScoreType;
import nl.s22k.chess.Statistics;
import nl.s22k.chess.Util;
import nl.s22k.chess.engine.EngineConstants;
import nl.s22k.chess.eval.EvalConstants;
import nl.s22k.chess.move.MoveUtil;
import nl.s22k.chess.move.MoveWrapper;
import nl.s22k.chess.move.TreeMove;

public class TTUtil {

	private static int keyShifts;
	public static int maxEntries;

	private static long[] alwaysReplaceKeys;
	private static long[] alwaysReplaceValues;
	private static long[] depthReplaceKeys;
	private static long[] depthReplaceValues;

	public static long usageCounter;

	public static final int FLAG_EXACT = 0;
	public static final int FLAG_UPPER = 1;
	public static final int FLAG_LOWER = 2;

	public static long halfMoveCounter = 0;

	// ///////////////////// DEPTH //8 bits
	private static final int FLAG = 8; // 2
	private static final int MOVE = 10; // 22
	private static final int HALF_MOVE_COUNTER = 32; // 16
	private static final int SCORE = 48; // 16

	public static boolean isInitialized = false;

	public static void init(final boolean force) {
		if (force || !isInitialized) {
			keyShifts = 64 - EngineConstants.POWER_2_TT_ENTRIES + 1;
			maxEntries = (int) Util.POWER_LOOKUP[EngineConstants.POWER_2_TT_ENTRIES - 1];

			alwaysReplaceKeys = new long[maxEntries];
			alwaysReplaceValues = new long[maxEntries];
			depthReplaceKeys = new long[maxEntries];
			depthReplaceValues = new long[maxEntries];
			usageCounter = 0;

			isInitialized = true;
		}
	}

	public static void clearValues() {
		if (!isInitialized) {
			return;
		}
		Arrays.fill(alwaysReplaceKeys, 0);
		Arrays.fill(alwaysReplaceValues, 0);
		Arrays.fill(depthReplaceKeys, 0);
		Arrays.fill(depthReplaceValues, 0);
		usageCounter = 0;
	}

	public static long getTTValue(final long key) {

		final int index = getIndex(key);

		final long alwaysValue = alwaysReplaceValues[index];
		final long depthValue = depthReplaceValues[index];

		if ((alwaysReplaceKeys[index] ^ alwaysValue) == key) {
			if (Statistics.ENABLED) {
				Statistics.ttHits++;
			}

			if ((depthReplaceKeys[index] ^ depthValue) == key && getDepth(depthValue) > getDepth(alwaysValue)) {
				return depthValue;
			}

			return alwaysValue;
		}

		if ((depthReplaceKeys[index] ^ depthValue) == key) {
			if (Statistics.ENABLED) {
				Statistics.ttHits++;
			}
			return depthValue;
		}

		if (Statistics.ENABLED) {
			Statistics.ttMisses++;
		}

		return 0;
	}

	private static int getIndex(final long key) {
		return (int) (key >>> keyShifts);
	}

	public static void setScoreInStatistics(ChessBoard cb) {
		if (NegamaxUtil.mode.get() == Mode.STOP) {
			return;
		}

		final long key = alwaysReplaceKeys[getIndex(cb.zobristKey)];
		final long value = alwaysReplaceValues[getIndex(cb.zobristKey)];

		if ((key ^ value) != cb.zobristKey) {
			// throw new RuntimeException("No best-move found");
			System.out.println("No bestmove found. SMP race condition?!");
			return;
		}
		Statistics.bestMove.score = getScore(value, 0);
		Statistics.bestMove.scoreType = ScoreType.ALPHA;
	}

	public static void setBestMoveInStatistics(ChessBoard cb, ScoreType scoreType) {
		if (NegamaxUtil.mode.get() == Mode.STOP) {
			return;
		}

		final long key = alwaysReplaceKeys[getIndex(cb.zobristKey)];
		long value = alwaysReplaceValues[getIndex(cb.zobristKey)];

		if ((key ^ value) != cb.zobristKey) {
			// throw new RuntimeException("No best-move found");
			System.out.println("No bestmove found. SMP race condition?!");
			return;
		}

		int move = getMove(value);
		List<Integer> moves = new ArrayList<Integer>(12);
		moves.add(move);
		TreeMove bestMove = new TreeMove(move, getScore(value, 0), scoreType);
		cb.doMove(move);

		for (int i = 0; i <= 10; i++) {
			value = getTTValue(cb.zobristKey);
			if (value == 0) {
				break;
			}
			move = getMove(value);
			moves.add(move);
			bestMove.appendMove(new TreeMove(move));
			cb.doMove(move);
		}
		for (int i = moves.size() - 1; i >= 0; i--) {
			cb.undoMove(moves.get(i));
		}

		Statistics.bestMove = bestMove;
	}

	public static void addValue(final long key, int score, final int ply, final int depth, final int flag, final int cleanMove) {

		if (NegamaxUtil.mode.get() != Mode.START) {
			return;
		}

		if (EngineConstants.ASSERT) {
			Assert.isTrue(depth >= 1);
			Assert.isTrue(cleanMove != 0);
			Assert.isTrue(score >= Util.SHORT_MIN && score <= Util.SHORT_MAX);
			Assert.isTrue(MoveUtil.getCleanMove(cleanMove) == cleanMove);
			Assert.isTrue(MoveUtil.getSourcePieceIndex(cleanMove) != 0);
		}

		// correct mate-score
		if (score > EvalConstants.SCORE_MATE_BOUND) {
			// Math.min because of qsearch
			score = Math.min(score + ply, Util.SHORT_MAX);
		} else if (score < -EvalConstants.SCORE_MATE_BOUND) {
			// Math.max because of qsearch
			score = Math.max(score - ply, Util.SHORT_MIN);
		}

		if (EngineConstants.ASSERT) {
			Assert.isTrue(score >= Util.SHORT_MIN && score <= Util.SHORT_MAX);
		}

		final int index = getIndex(key);
		final long value = createValue(score, cleanMove, flag, depth);

		if (Statistics.ENABLED) {
			if (alwaysReplaceKeys[index] == 0) {
				usageCounter++;
			}
		}

		if (depth > getDepth(depthReplaceValues[index]) || halfMoveCounter != getHalfMoveCounter(depthReplaceValues[index])) {
			if (Statistics.ENABLED) {
				if (depthReplaceKeys[index] == 0) {
					usageCounter++;
				}
			}
			depthReplaceKeys[index] = key ^ value;
			depthReplaceValues[index] = value;
		}

		if (NegamaxUtil.mode.get() != Mode.START) {
			return;
		}

		// TODO do not store if already stored in depth-TT?
		alwaysReplaceKeys[index] = key ^ value;
		alwaysReplaceValues[index] = value;
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

	public static int getHalfMoveCounter(final long value) {
		return (int) (value >>> HALF_MOVE_COUNTER & 0xffff);
	}

	public static int getDepth(final long value) {
		return (int) (value & 0xff);
	}

	public static int getFlag(final long value) {
		return (int) (value >>> FLAG & 3);
	}

	public static int getMove(final long value) {
		return (int) (value >>> MOVE & 0x3fffff);
	}

	// SCORE,HALF_MOVE_COUNTER,MOVE,FLAG,DEPTH
	public static long createValue(final long score, final long cleanMove, final long flag, final long depth) {
		if (EngineConstants.ASSERT) {
			Assert.isTrue(cleanMove == MoveUtil.getCleanMove((int) cleanMove));
			Assert.isTrue(score >= Util.SHORT_MIN && score <= Util.SHORT_MAX);
			Assert.isTrue(depth <= 255);
		}
		return score << SCORE | halfMoveCounter << HALF_MOVE_COUNTER | cleanMove << MOVE | flag << FLAG | depth;
	}

	public static String toString(long ttValue) {
		return "score=" + TTUtil.getScore(ttValue, 0) + " " + new MoveWrapper(getMove(ttValue)) + " depth=" + TTUtil.getDepth(ttValue) + " flag="
				+ TTUtil.getFlag(ttValue);
	}

}
