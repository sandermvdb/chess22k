package nl.s22k.chess.search;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import nl.s22k.chess.ChessConstants;
import nl.s22k.chess.engine.EngineConstants;
import nl.s22k.chess.move.MoveUtil;

public class HeuristicUtil {

	private static final int[] KILLER_MOVE_1 = new int[EngineConstants.MAX_PLIES * 2];
	private static final int[] KILLER_MOVE_2 = new int[EngineConstants.MAX_PLIES * 2];

	private static final int[][] HH_MOVES = new int[2][64 * 64];
	private static final int[][] BF_MOVES = new int[2][64 * 64];
	static {
		clearTables();
	}

	public static void clearTables() {

		Arrays.fill(KILLER_MOVE_1, 0);
		Arrays.fill(KILLER_MOVE_2, 0);

		Arrays.fill(HH_MOVES[ChessConstants.WHITE], 0);
		Arrays.fill(HH_MOVES[ChessConstants.BLACK], 0);
		Arrays.fill(BF_MOVES[ChessConstants.WHITE], 1);
		Arrays.fill(BF_MOVES[ChessConstants.BLACK], 1);
	}

	public static void addHHValue(final int color, final int fromToIndex, final int depth) {
		HH_MOVES[color][fromToIndex] += depth * depth;
		if (EngineConstants.ASSERT) {
			assertTrue(HH_MOVES[color][fromToIndex] >= 0);
		}
	}

	public static void addBFValue(final int color, final int fromToIndex, final int depth) {
		BF_MOVES[color][fromToIndex] += depth * depth;
		if (EngineConstants.ASSERT) {
			assertTrue(BF_MOVES[color][fromToIndex] >= 0);
		}
	}

	public static int getHHScore(final int color, final int fromToIndex) {
		return Math.min(MoveUtil.SCORE_MAX, 100 * HeuristicUtil.HH_MOVES[color][fromToIndex] / HeuristicUtil.BF_MOVES[color][fromToIndex]);
	}

	public static void addKillerMove(final int move, final int ply) {
		if (EngineConstants.ASSERT) {
			assertEquals(move, MoveUtil.getCleanMove(move));
		}
		if (EngineConstants.ENABLE_KILLER_MOVES) {
			if (KILLER_MOVE_1[ply] != move) {
				KILLER_MOVE_2[ply] = KILLER_MOVE_1[ply];
				KILLER_MOVE_1[ply] = move;
			}
		}
	}

	public static int getKiller1(final int ply) {
		return KILLER_MOVE_1[ply];
	}

	public static int getKiller2(final int ply) {
		return KILLER_MOVE_2[ply];
	}

}
