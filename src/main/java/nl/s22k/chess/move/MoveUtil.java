package nl.s22k.chess.move;

import nl.s22k.chess.ChessConstants;
import nl.s22k.chess.engine.EngineConstants;
import nl.s22k.chess.search.HeuristicUtil;

public class MoveUtil {

	public static final int NORMAL = 0;
	public static final int EP = 1;
	public static final int PROMOTION_N = ChessConstants.NIGHT;
	public static final int PROMOTION_B = ChessConstants.BISHOP;
	public static final int PROMOTION_R = ChessConstants.ROOK;
	public static final int PROMOTION_Q = ChessConstants.QUEEN;
	public static final int CASTLING = 6;

	// ///////////////////// FROM //6 bits
	private static final int TO = 6; // 6
	private static final int SOURCE = 12; // 3
	private static final int ATTACK = 15; // 3
	private static final int MOVE_TYPE = 18; // 3

	// TODO one extra bit is available
	private static final int SCORE = 22; // 10 or 11

	public static final int SEE_CAPTURE_DIVIDER = 6;

	public static final int SCORE_MAX = 511;

	private static final int CLEAN_MOVE_MASK = (1 << SCORE) - 1;

	public static int getFromIndex(final int move) {
		return move & 0x3f;
	}

	public static int getToIndex(final int move) {
		return move >>> TO & 0x3f;
	}

	public static int getFromToIndex(final int move) {
		return move & 0xfff;
	}

	public static int getCleanMove(final int move) {
		return move & CLEAN_MOVE_MASK;
	}

	public static int getAttackedPieceIndex(final int move) {
		return move >>> ATTACK & 7;
	}

	public static int getSourcePieceIndex(final int move) {
		return move >>> SOURCE & 7;
	}

	public static int getScore(final int move) {
		// TODO can score be negative (last bit)?
		return move >> SCORE;
	}

	public static int createMove(final int fromIndex, final int toIndex, final int sourcePieceIndex) {
		return sourcePieceIndex << SOURCE | toIndex << TO | fromIndex;
	}

	public static int createPromotionMove(final int promotionPiece, final int fromIndex, final int toIndex) {
		return promotionPiece << MOVE_TYPE | ChessConstants.PAWN << SOURCE | toIndex << TO | fromIndex;
	}

	public static int createAttackMove(final int fromIndex, final int toIndex, final int sourcePieceIndex, final int attackedPieceIndex) {
		return attackedPieceIndex << ATTACK | sourcePieceIndex << SOURCE | toIndex << TO | fromIndex;
	}

	public static int createPromotionAttack(final int promotionPiece, final int fromIndex, final int toIndex, final int attackedPieceIndex) {
		return promotionPiece << MOVE_TYPE | attackedPieceIndex << ATTACK | ChessConstants.PAWN << SOURCE | toIndex << TO | fromIndex;
	}

	public static int createEPMove(final int fromIndex, final int toIndex) {
		return EP << MOVE_TYPE | ChessConstants.PAWN << ATTACK | ChessConstants.PAWN << SOURCE | toIndex << TO | fromIndex;
	}

	public static int createCastlingMove(final int fromIndex, final int toIndex) {
		return CASTLING << MOVE_TYPE | ChessConstants.KING << SOURCE | toIndex << TO | fromIndex;
	}

	public static int getMoveType(final int move) {
		return move >> MOVE_TYPE & 7;
	}

	public static boolean isPromotion(final int move) {
		switch (getMoveType(move)) {
		case PROMOTION_N:
		case PROMOTION_B:
		case PROMOTION_R:
		case PROMOTION_Q:
			return true;
		default:
			return false;
		}
	}

	public static boolean isPawnPush78(final int move) {
		return getSourcePieceIndex(move) == ChessConstants.PAWN && (getToIndex(move) > 47 || getToIndex(move) < 16);
	}

	public static int setHHMove(final int move, final int colorToMove) {
		if (EngineConstants.ASSERT) {
			assert getCleanMove(move) == move : "Setting non-clean move as hh-move";
		}

		// TODO use constant
		return move | (Math.min(SCORE_MAX,
				100 * HeuristicUtil.HH_MOVES[colorToMove][getFromToIndex(move)] / HeuristicUtil.BF_MOVES[colorToMove][getFromToIndex(move)])) << SCORE;
	}

	public static int setSeeMove(final int move, int seeCaptureScore) {
		if (EngineConstants.ASSERT) {
			assert MoveUtil.getCleanMove(move) == move : "Setting see-score to non-clean move";
		}

		seeCaptureScore /= SEE_CAPTURE_DIVIDER;
		if (EngineConstants.ASSERT) {
			assert seeCaptureScore <= MoveUtil.SCORE_MAX && seeCaptureScore >= -1 * MoveUtil.SCORE_MAX : "See-score out of range: " + seeCaptureScore;
		}
		return move | seeCaptureScore << SCORE;
	}

}
