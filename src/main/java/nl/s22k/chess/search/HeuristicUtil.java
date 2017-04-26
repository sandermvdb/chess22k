package nl.s22k.chess.search;

import java.util.Arrays;

import nl.s22k.chess.ChessConstants;
import nl.s22k.chess.engine.EngineConstants;
import nl.s22k.chess.move.MoveUtil;

public class HeuristicUtil {

	private static final int[] KILLER_MOVE_1 = new int[ChessConstants.MAX_PLIES + 4];
	private static final int[] KILLER_MOVE_2 = new int[ChessConstants.MAX_PLIES + 4];

	public static final int[][] HH_MOVES = new int[2][64 * 64];
	public static final int[][] BF_MOVES = new int[2][64 * 64];
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

	public static void addHHValue(final int colorToMove, final int fromToIndex, final int depth) {
		HH_MOVES[colorToMove][fromToIndex] += depth * depth;
		if (EngineConstants.ASSERT) {
			assert HH_MOVES[colorToMove][fromToIndex] >= 0 : "HH-value < 0";
		}
	}

	public static void addBFValue(final int colorToMove, final int fromToIndex, final int depth) {
		BF_MOVES[colorToMove][fromToIndex] += depth * depth;
		if (EngineConstants.ASSERT) {
			assert BF_MOVES[colorToMove][fromToIndex] >= 0 : "BF-value < 0";
		}
	}

	public static void addKillerMove(final int cleanMove, final int ply) {
		if (EngineConstants.ASSERT) {
			assert MoveUtil.getCleanMove(cleanMove) == cleanMove : "Adding non clean-move to killer-moves";
		}
		if (EngineConstants.ENABLE_KILLER_MOVES) {
			if (KILLER_MOVE_1[ply] != cleanMove) {
				KILLER_MOVE_2[ply] = KILLER_MOVE_1[ply];
				KILLER_MOVE_1[ply] = cleanMove;
			}
		}
	}

	public static int getCleanKiller1(final int ply) {
		return KILLER_MOVE_1[ply];
	}

	public static int getCleanKiller2(final int ply) {
		return KILLER_MOVE_2[ply];
	}

}
