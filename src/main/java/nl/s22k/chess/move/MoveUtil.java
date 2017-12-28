package nl.s22k.chess.move;

import nl.s22k.chess.ChessConstants;
import nl.s22k.chess.engine.EngineConstants;
import nl.s22k.chess.search.HeuristicUtil;

public class MoveUtil {

	public static final int TYPE_NORMAL = 0;
	public static final int TYPE_EP = 1;
	public static final int TYPE_PROMOTION_N = ChessConstants.NIGHT;
	public static final int TYPE_PROMOTION_B = ChessConstants.BISHOP;
	public static final int TYPE_PROMOTION_R = ChessConstants.ROOK;
	public static final int TYPE_PROMOTION_Q = ChessConstants.QUEEN;
	public static final int TYPE_CASTLING = 6;

	// ///////////////////// FROM //6 bits
	private static final int MASK_TO = 6; // 6
	private static final int MASK_SOURCE = 12; // 3
	private static final int MASK_ATTACK = 15; // 3
	private static final int MASK_MOVE_TYPE = 18; // 3
	private static final int MASK_PROMOTION = 21; // 1
	private static final int MASK_SCORE = 22; // 10 or 11

	public static final int SEE_CAPTURE_DIVIDER = 6;

	public static final int SCORE_MAX = 511;

	private static final int CLEAN_MOVE_MASK = (1 << MASK_SCORE) - 1;

	public static int getFromIndex(final int move) {
		return move & 0x3f;
	}

	public static int getToIndex(final int move) {
		return move >>> MASK_TO & 0x3f;
	}

	public static int getFromToIndex(final int move) {
		return move & 0xfff;
	}

	public static int getCleanMove(final int move) {
		return move & CLEAN_MOVE_MASK;
	}

	public static int getAttackedPieceIndex(final int move) {
		return move >>> MASK_ATTACK & 7;
	}

	public static int getSourcePieceIndex(final int move) {
		return move >>> MASK_SOURCE & 7;
	}

	public static int getScore(final int move) {
		// TODO can score be negative (last bit)?
		return move >> MASK_SCORE;
	}

	public static int createMove(final int fromIndex, final int toIndex, final int sourcePieceIndex) {
		return sourcePieceIndex << MASK_SOURCE | toIndex << MASK_TO | fromIndex;
	}

	public static int createPromotionMove(final int promotionPiece, final int fromIndex, final int toIndex) {
		return 1 << MASK_PROMOTION | promotionPiece << MASK_MOVE_TYPE | ChessConstants.PAWN << MASK_SOURCE | toIndex << MASK_TO | fromIndex;
	}

	public static int createAttackMove(final int fromIndex, final int toIndex, final int sourcePieceIndex, final int attackedPieceIndex) {
		return attackedPieceIndex << MASK_ATTACK | sourcePieceIndex << MASK_SOURCE | toIndex << MASK_TO | fromIndex;
	}

	public static int createSeeAttackMove(final int fromIndex, final int sourcePieceIndex) {
		return sourcePieceIndex << MASK_SOURCE | fromIndex;
	}

	public static int createPromotionAttack(final int promotionPiece, final int fromIndex, final int toIndex, final int attackedPieceIndex) {
		return 1 << MASK_PROMOTION | promotionPiece << MASK_MOVE_TYPE | attackedPieceIndex << MASK_ATTACK | ChessConstants.PAWN << MASK_SOURCE
				| toIndex << MASK_TO | fromIndex;
	}

	public static int createEPMove(final int fromIndex, final int toIndex) {
		return TYPE_EP << MASK_MOVE_TYPE | ChessConstants.PAWN << MASK_ATTACK | ChessConstants.PAWN << MASK_SOURCE | toIndex << MASK_TO | fromIndex;
	}

	public static int createCastlingMove(final int fromIndex, final int toIndex) {
		return TYPE_CASTLING << MASK_MOVE_TYPE | ChessConstants.KING << MASK_SOURCE | toIndex << MASK_TO | fromIndex;
	}

	public static int getMoveType(final int move) {
		return move >> MASK_MOVE_TYPE & 7;
	}

	public static boolean isPromotion(final int move) {
		return (move & 1 << MASK_PROMOTION) != 0;
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

	public static int setHHMove(final int move, final int colorToMove) {
		if (EngineConstants.ASSERT) {
			assert getCleanMove(move) == move : "Setting non-clean move as hh-move";
		}

		// TODO use constant
		return move | (Math.min(SCORE_MAX,
				100 * HeuristicUtil.HH_MOVES[colorToMove][getFromToIndex(move)] / HeuristicUtil.BF_MOVES[colorToMove][getFromToIndex(move)])) << MASK_SCORE;
	}

	public static int setSeeMove(final int move, int seeCaptureScore) {
		if (EngineConstants.ASSERT) {
			assert MoveUtil.getCleanMove(move) == move : "Setting see-score to non-clean move";
		}

		seeCaptureScore /= SEE_CAPTURE_DIVIDER;
		if (EngineConstants.ASSERT) {
			assert seeCaptureScore <= MoveUtil.SCORE_MAX && seeCaptureScore >= -1 * MoveUtil.SCORE_MAX : "See-score out of range: " + seeCaptureScore;
		}
		return move | seeCaptureScore << MASK_SCORE;
	}

	public static int setScoredMove(final int move, int score) {
		return move | score << MASK_SCORE;
	}

}
