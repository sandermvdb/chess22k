package nl.s22k.chess.engine;

public class EngineConstants {

	public static final int MAX_PLIES = 64;
	public static final int PLIES_EXTENDED = 24;
	public static final int MAX_MOVES = 768;

	public static final boolean ASSERT = false;
	public static final boolean TEST_EVAL_VALUES = false;
	public static final boolean TEST_PAWN_EVAL_CACHE = false;
	public static final boolean TEST_TT_VALUES = false;
	public static final boolean GENERATE_BR_PROMOTIONS = false;
	public static boolean isTuningSession = false;

	// Repetition-table
	public static final int REPETITION_TABLE_ENTRIES = 16; // 16
	public static final boolean ENABLE_REPETITION_TABLE = true;

	// TT values
	public static int POWER_2_TT_ENTRIES = 23; // 23
	public static final boolean VERIFY_TT_MOVE = false;

	// Search improvements
	public static final boolean ENABLE_KILLER_MOVES = true;
	public static final boolean ENABLE_HISTORY_HEURISTIC = true;
	public static final boolean ENABLE_ASPIRATION_WINDOW = true;
	public static final int ASPIRATION_WINDOW_DELTA = 20; // 20
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
	public static final boolean ENABLE_Q_PRUNE_BAD_CAPTURES = true;
	public static final boolean ENABLE_PVS = true;
	public static final boolean ENABLE_MATE_DISTANCE_PRUNING = true;
	public static final boolean ENABLE_STATIC_NULL_MOVE = true;
	public static final boolean ENABLE_RAZORING = true;
	public static final boolean ENABLE_FUTILITY_PRUNING = true;

	// Evaluation-function
	public static boolean ENABLE_EVAL_CACHE = true;
	public static final int POWER_2_EVAL_ENTRIES = 14; // 14
	public static boolean ENABLE_PAWN_EVAL_CACHE = true;
	public static final int POWER_2_PAWN_EVAL_ENTRIES = 14; // 14
	public static final boolean ENABLE_EVAL_MOBILITY = true;
	public static final boolean ENABLE_EVAL_MOBILITY_KING_DEFENSE = true;
	public static final boolean ENABLE_INCREMENTAL_PSQT = true;
}
