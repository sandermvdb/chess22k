package nl.s22k.chess.engine;

public class EngineConstants {

	public static final boolean TEST_VALUES = false;
	public static final boolean TEST_EVAL_VALUES = false; // TODO mirroring, flipping, etc...
	public static final boolean TEST_PAWN_EVAL_CACHE = false;
	public static final boolean TEST_TT_VALUES = false;

	// Repetition-table
	public static final int REPETITION_TABLE_ENTRIES = 16;
	public static final boolean ENABLE_REPETITION_TABLE = true;

	// TT values
	public static int POWER_2_TT_ENTRIES = 23; // 23

	// Search improvements
	public static final boolean ENABLE_QUIESCENCE = true;
	public static final boolean ENABLE_KILLER_MOVES = true;
	public static final boolean ENABLE_HISTORY_HEURISTIC = true;
	public static final boolean ENABLE_ASPIRATION_WINDOW = true;
	public static final int ASPIRATION_WINDOW_DELTA = 20;
	public static final boolean ENABLE_IID = true;
	public static final int IID_REDUCTION = 1;
	public static final boolean ENABLE_SEE = true;
	public static final boolean ENABLE_SORT_LOSING_CAPTURES = true;
	public static final boolean ENABLE_Q_SEE = true;

	// Search extensions
	public static final boolean ENABLE_CHECK_EXTENSION = true;

	// Search reductions
	public static final boolean ENABLE_NULL_MOVE = true;
	public static final int NULL_MOVE_R = 2;
	public static final boolean ENABLE_LMR = true;
	public static final int LMR_MOVE_COUNTER = 2;
	public static final boolean ENABLE_DELTA_PRUNING = false;
	public static final int DELTA_MARGIN = 200; // not used
	public static final boolean ENABLE_Q_PRUNE_BAD_CAPTURES = true;
	public static final boolean ENABLE_PVS = true;
	public static final boolean ENABLE_MATE_DISTANCE_PRUNING = true;

	// Evaluation-function
	public static final boolean ENABLE_EVAL_CACHE = true;
	public static final int POWER_2_EVAL_ENTRIES = 14;
	public static final boolean ENABLE_PAWN_EVAL_CACHE = true;
	public static final int POWER_2_PAWN_EVAL_ENTRIES = 14; // 14
	public static final boolean ENABLE_EVAL_MOBILITY = true;
	public static final boolean ENABLE_INCREMENTAL_PSQT = true;
	public static final boolean ENABLE_EVAL_MOBILITY_KING_DEFENSE = false;
	public static final boolean ENABLE_EVAL_HANGING_PIECES = false;
}
