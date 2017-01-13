package nl.s22k.chess.move;

import nl.s22k.chess.ChessConstants;
import nl.s22k.chess.engine.EngineConstants;
import nl.s22k.chess.search.HeuristicUtil;

public class MoveUtil {

	// ///////////////////// FROM //6 bits
	private static final int TO = 6; // 6
	private static final int SOURCE = 12; // 3
	private static final int ATTACK = 15; // 3
	private static final int IS_EP = 18; // 1
	private static final int IS_CASTLING = 19; // 1
	private static final int PROMOTION = 20; // 1
	private static final int PROMOTION_NIGHT = 21; // 1
	private static final int SCORE = 22; // 10 or 11

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

	public static int getZKAttackedPieceIndex(final int move) {
		return move >>> ATTACK & 7;
	}

	public static int getZKSourcePieceIndex(final int move) {
		return move >>> SOURCE & 7;
	}

	public static int getScore(final int move) {
		// TODO can score be negative (last bit)?
		return move >> SCORE;
	}

	public static int createMove(final int fromIndex, final int toIndex, final int sourcePieceIndex) {
		return sourcePieceIndex << SOURCE | toIndex << TO | fromIndex;
	}

	public static int createPromotionMove(final int fromIndex, final int toIndex) {
		return 1 << PROMOTION | ChessConstants.PAWN << SOURCE | toIndex << TO | fromIndex;
	}

	public static int createNightPromotionMove(final int fromIndex, final int toIndex) {
		return 1 << PROMOTION | 1 << PROMOTION_NIGHT | ChessConstants.PAWN << SOURCE | toIndex << TO | fromIndex;
	}

	public static int createAttackMove(final int fromIndex, final int toIndex, final int sourcePieceIndex, final int attackedPieceIndex) {
		return attackedPieceIndex << ATTACK | sourcePieceIndex << SOURCE | toIndex << TO | fromIndex;
	}

	public static int createPromotionAttack(final int fromIndex, final int toIndex, final int attackedPieceIndex) {
		return 1 << PROMOTION | attackedPieceIndex << ATTACK | ChessConstants.PAWN << SOURCE | toIndex << TO | fromIndex;
	}

	public static int createNightPromotionAttack(final int fromIndex, final int toIndex, final int attackedPieceIndex) {
		return 1 << PROMOTION | 1 << PROMOTION_NIGHT | attackedPieceIndex << ATTACK | ChessConstants.PAWN << SOURCE | toIndex << TO | fromIndex;
	}

	public static int createEPMove(final int fromIndex, final int toIndex) {
		return ChessConstants.PAWN << ATTACK | ChessConstants.PAWN << SOURCE | 1 << IS_EP | toIndex << TO | fromIndex;
	}

	public static int createCastlingMove(final int fromIndex, final int toIndex) {
		return 1 << IS_CASTLING | ChessConstants.KING << SOURCE | toIndex << TO | fromIndex;
	}

	/**
	 * queen or night promotion
	 */
	public static boolean isPromotion(final int move) {
		return (move & 1 << PROMOTION) != 0;
	}

	public static boolean isNightPromotion(final int move) {
		return (move & 1 << PROMOTION_NIGHT) != 0;
	}

	public static boolean isEP(final int move) {
		return (move & 1 << IS_EP) != 0;
	}

	public static boolean isCastling(final int move) {
		return (move & 1 << IS_CASTLING) != 0;
	}

	public static int setHHMove(final int move, final int colorToMove) {
		if (EngineConstants.TEST_VALUES) {
			if (getCleanMove(move) != move) {
				System.out.println("Setting non-clean move as hh-move");
			}
		}

		return move | (Math.min(SCORE_MAX,
				100 * HeuristicUtil.HH_MOVES[colorToMove][getFromToIndex(move)] / HeuristicUtil.BF_MOVES[colorToMove][getFromToIndex(move)])) << SCORE;
	}

	public static int setSeeMove(final int move, int seeCaptureScore) {
		if (EngineConstants.TEST_VALUES) {
			if (MoveUtil.getCleanMove(move) != move) {
				System.out.println("Setting see-score to non-clean move");
			}
		}
		seeCaptureScore /= 6;
		if (EngineConstants.TEST_VALUES) {
			if (seeCaptureScore > MoveUtil.SCORE_MAX || seeCaptureScore < -1 * MoveUtil.SCORE_MAX) {
				System.out.println("See-score out of range: " + seeCaptureScore);
			}
		}
		return move | seeCaptureScore << SCORE;
	}

}
