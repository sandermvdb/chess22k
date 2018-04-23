package nl.s22k.chess.move;

import static org.junit.Assert.assertEquals;

import nl.s22k.chess.ChessConstants;
import nl.s22k.chess.engine.EngineConstants;

public class MoveUtil {

	public static final int TYPE_NORMAL = 0;
	public static final int TYPE_EP = 1;
	public static final int TYPE_PROMOTION_N = ChessConstants.NIGHT;
	public static final int TYPE_PROMOTION_B = ChessConstants.BISHOP;
	public static final int TYPE_PROMOTION_R = ChessConstants.ROOK;
	public static final int TYPE_PROMOTION_Q = ChessConstants.QUEEN;
	public static final int TYPE_CASTLING = 6;

	// ///////////////////// FROM //6 bits
	private static final int SHIFT_TO = 6; // 6
	private static final int SHIFT_SOURCE = 12; // 3
	private static final int SHIFT_ATTACK = 15; // 3
	private static final int SHIFT_MOVE_TYPE = 18; // 3
	private static final int SHIFT_PROMOTION = 21; // 1
	private static final int SHIFT_SCORE = 22; // 10 or 11

	private static final int MASK_3_BITS = 7; // 6
	private static final int MASK_6_BITS = 0x3f; // 6
	private static final int MASK_12_BITS = 0xfff;

	private static final int MASK_FROM = 0x3f; // 6
	private static final int MASK_TO = 0x3f << 6; // 6
	private static final int MASK_SOURCE = 7 << 12; // 3
	private static final int MASK_ATTACK = 7 << 15; // 3
	private static final int MASK_TYPE = 7 << 18; // 3
	private static final int MASK_PROMOTION = 1 << 21; // 1
	private static final int MASK_SCORE = 1 << 22; // 10 or 11

	public static final int MASK_QUIET = MASK_PROMOTION | MASK_ATTACK;
	public static final int SEE_CAPTURE_DIVIDER = 6;

	public static final int SCORE_MAX = 511;

	private static final int CLEAN_MOVE_MASK = (1 << SHIFT_SCORE) - 1;

	public static int getFromIndex(final int move) {
		return move & MASK_6_BITS;
	}

	public static int getToIndex(final int move) {
		return move >>> SHIFT_TO & MASK_6_BITS;
	}

	public static int getFromToIndex(final int move) {
		return move & MASK_12_BITS;
	}

	public static int getCleanMove(final int move) {
		return move & CLEAN_MOVE_MASK;
	}

	public static int getAttackedPieceIndex(final int move) {
		return move >>> SHIFT_ATTACK & MASK_3_BITS;
	}

	public static int getSourcePieceIndex(final int move) {
		return move >>> SHIFT_SOURCE & MASK_3_BITS;
	}

	public static int getScore(final int move) {
		// arithmetic shift!
		return move >> SHIFT_SCORE;
	}

	public static int getMoveType(final int move) {
		return move >>> SHIFT_MOVE_TYPE & MASK_3_BITS;
	}

	public static int createMove(final int fromIndex, final int toIndex, final int sourcePieceIndex) {
		return sourcePieceIndex << SHIFT_SOURCE | toIndex << SHIFT_TO | fromIndex;
	}

	public static int createWhitePawnMove(final int fromIndex) {
		return ChessConstants.PAWN << SHIFT_SOURCE | (fromIndex + 8) << SHIFT_TO | fromIndex;
	}

	public static int createBlackPawnMove(final int fromIndex) {
		return ChessConstants.PAWN << SHIFT_SOURCE | (fromIndex - 8) << SHIFT_TO | fromIndex;
	}

	public static int createWhitePawn2Move(final int fromIndex) {
		return ChessConstants.PAWN << SHIFT_SOURCE | (fromIndex + 16) << SHIFT_TO | fromIndex;
	}

	public static int createBlackPawn2Move(final int fromIndex) {
		return ChessConstants.PAWN << SHIFT_SOURCE | (fromIndex - 16) << SHIFT_TO | fromIndex;
	}

	public static int createPromotionMove(final int promotionPiece, final int fromIndex, final int toIndex) {
		return 1 << SHIFT_PROMOTION | promotionPiece << SHIFT_MOVE_TYPE | ChessConstants.PAWN << SHIFT_SOURCE | toIndex << SHIFT_TO | fromIndex;
	}

	public static int createAttackMove(final int fromIndex, final int toIndex, final int sourcePieceIndex, final int attackedPieceIndex) {
		return attackedPieceIndex << SHIFT_ATTACK | sourcePieceIndex << SHIFT_SOURCE | toIndex << SHIFT_TO | fromIndex;
	}

	public static int createSeeAttackMove(final int fromIndex, final int sourcePieceIndex) {
		return sourcePieceIndex << SHIFT_SOURCE | fromIndex;
	}

	public static int createPromotionAttack(final int promotionPiece, final int fromIndex, final int toIndex, final int attackedPieceIndex) {
		return 1 << SHIFT_PROMOTION | promotionPiece << SHIFT_MOVE_TYPE | attackedPieceIndex << SHIFT_ATTACK | ChessConstants.PAWN << SHIFT_SOURCE
				| toIndex << SHIFT_TO | fromIndex;
	}

	public static int createEPMove(final int fromIndex, final int toIndex) {
		return TYPE_EP << SHIFT_MOVE_TYPE | ChessConstants.PAWN << SHIFT_ATTACK | ChessConstants.PAWN << SHIFT_SOURCE | toIndex << SHIFT_TO | fromIndex;
	}

	public static int createCastlingMove(final int fromIndex, final int toIndex) {
		return TYPE_CASTLING << SHIFT_MOVE_TYPE | ChessConstants.KING << SHIFT_SOURCE | toIndex << SHIFT_TO | fromIndex;
	}

	public static boolean isPromotion(final int move) {
		return (move & MASK_PROMOTION) != 0;
	}

	public static boolean isPawnPush78(final int move) {
		return getSourcePieceIndex(move) == ChessConstants.PAWN && (getToIndex(move) > 47 || getToIndex(move) < 16);
	}

	public static boolean isPawnPush678(final int move, final int color) {
		if (color == ChessConstants.WHITE) {
			return getSourcePieceIndex(move) == ChessConstants.PAWN && getToIndex(move) > 39;
		} else {
			return getSourcePieceIndex(move) == ChessConstants.PAWN && getToIndex(move) < 24;
		}
	}

	/**
	 * no promotion and no attack
	 */
	public static boolean isQuiet(final int move) {
		return (move & MASK_QUIET) == 0;
	}

	public static int setScoredMove(final int move, final int score) {
		if (EngineConstants.ASSERT) {
			assertEquals(move, getCleanMove(move));
			assertEquals(score, getScore(move | score << SHIFT_SCORE));
		}
		return move | score << SHIFT_SCORE;
	}

}
