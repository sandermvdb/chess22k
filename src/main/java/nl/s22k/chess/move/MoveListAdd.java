package nl.s22k.chess.move;

import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.ChessConstants;
import nl.s22k.chess.Util;
import nl.s22k.chess.engine.EngineConstants;

public class MoveListAdd {

	public static void quietNotPinnedMoves(long moves, final int fromIndex, final int sourcePieceIndex) {
		while (moves != 0) {
			MoveList.addMove(MoveUtil.createMove(fromIndex, Long.numberOfTrailingZeros(moves), sourcePieceIndex));
			moves &= moves - 1;
		}
	}

	public static void quietNotInCheckPinnedMoves(long moves, final int fromIndex, final ChessBoard cb) {
		// piece is pinned (move toward or away from pinner? could be multiple...)
		int toIndex;
		while (moves != 0) {
			toIndex = Long.numberOfTrailingZeros(moves);
			if (cb.isLegalMove(fromIndex, toIndex)) {
				MoveList.addMove(MoveUtil.createMove(fromIndex, toIndex, cb.pieceIndexes[fromIndex]));
			}
			moves &= moves - 1;
		}
	}

	public static void notInCheckAttacks(long moves, final int fromIndex, final ChessBoard cb) {
		if (moves == 0) {
			return;
		}
		if ((cb.pinnedPieces[cb.colorToMove] & Util.POWER_LOOKUP[fromIndex]) == 0) {
			while (moves != 0) {
				final int toIndex = Long.numberOfTrailingZeros(moves);
				MoveList.addMove(MoveUtil.createAttackMove(fromIndex, toIndex, cb.pieceIndexes[fromIndex], cb.pieceIndexes[toIndex]));
				moves &= moves - 1;
			}
		} else {
			// piece is pinned (attack pinner? could be multiple...)
			while (moves != 0) {
				final int toIndex = Long.numberOfTrailingZeros(moves);
				if (cb.isLegalAttackMove(fromIndex, toIndex)) {
					MoveList.addMove(MoveUtil.createAttackMove(fromIndex, toIndex, cb.pieceIndexes[fromIndex], cb.pieceIndexes[toIndex]));
				}
				moves &= moves - 1;
			}
		}
	}

	public static void inCheckAttacks(long moves, final int fromIndex, final ChessBoard cb) {
		while (moves != 0) {
			final int toIndex = Long.numberOfTrailingZeros(moves);
			MoveList.addMove(MoveUtil.createAttackMove(fromIndex, toIndex, cb.pieceIndexes[fromIndex], cb.pieceIndexes[toIndex]));
			moves &= moves - 1;
		}
	}

	public static void notInCheckPromotionMove(long move, final int fromIndex, final ChessBoard cb) {
		if (move == 0) {
			return;
		}
		if ((cb.pinnedPieces[cb.colorToMove] & Util.POWER_LOOKUP[fromIndex]) == 0) {
			MoveList.addMove(MoveUtil.createPromotionMove(MoveUtil.PROMOTION_Q, fromIndex, Long.numberOfTrailingZeros(move)));
			MoveList.addMove(MoveUtil.createPromotionMove(MoveUtil.PROMOTION_N, fromIndex, Long.numberOfTrailingZeros(move)));
			if (EngineConstants.GENERATE_BR_PROMOTIONS) {
				MoveList.addMove(MoveUtil.createPromotionMove(MoveUtil.PROMOTION_B, fromIndex, Long.numberOfTrailingZeros(move)));
				MoveList.addMove(MoveUtil.createPromotionMove(MoveUtil.PROMOTION_R, fromIndex, Long.numberOfTrailingZeros(move)));
			}
		} else {
			// piece is pinned
			final int toIndex = Long.numberOfTrailingZeros(move);
			if (cb.isLegalMove(fromIndex, toIndex)) {
				MoveList.addMove(MoveUtil.createPromotionMove(MoveUtil.PROMOTION_Q, fromIndex, toIndex));
				MoveList.addMove(MoveUtil.createPromotionMove(MoveUtil.PROMOTION_N, fromIndex, toIndex));
				if (EngineConstants.GENERATE_BR_PROMOTIONS) {
					MoveList.addMove(MoveUtil.createPromotionMove(MoveUtil.PROMOTION_B, fromIndex, toIndex));
					MoveList.addMove(MoveUtil.createPromotionMove(MoveUtil.PROMOTION_R, fromIndex, toIndex));
				}
			}
		}
	}

	public static void inCheckPromotionMove(long move, final int fromIndex, final ChessBoard cb) {
		if (move == 0) {
			return;
		}
		final int toIndex = Long.numberOfTrailingZeros(move);
		if (cb.isLegalMove(fromIndex, toIndex)) {
			MoveList.addMove(MoveUtil.createPromotionMove(MoveUtil.PROMOTION_Q, fromIndex, toIndex));
			MoveList.addMove(MoveUtil.createPromotionMove(MoveUtil.PROMOTION_N, fromIndex, toIndex));
			if (EngineConstants.GENERATE_BR_PROMOTIONS) {
				MoveList.addMove(MoveUtil.createPromotionMove(MoveUtil.PROMOTION_B, fromIndex, toIndex));
				MoveList.addMove(MoveUtil.createPromotionMove(MoveUtil.PROMOTION_R, fromIndex, toIndex));
			}
		}
	}

	public static void promotionAttacks(long moves, final int fromIndex, final ChessBoard cb) {
		while (moves != 0) {
			final int toIndex = Long.numberOfTrailingZeros(moves);
			if (cb.isLegalAttackMove(fromIndex, toIndex)) {
				MoveList.addMove(MoveUtil.createPromotionAttack(MoveUtil.PROMOTION_Q, fromIndex, toIndex, cb.pieceIndexes[toIndex]));
				MoveList.addMove(MoveUtil.createPromotionAttack(MoveUtil.PROMOTION_N, fromIndex, toIndex, cb.pieceIndexes[toIndex]));
				if (EngineConstants.GENERATE_BR_PROMOTIONS) {
					MoveList.addMove(MoveUtil.createPromotionAttack(MoveUtil.PROMOTION_B, fromIndex, toIndex, cb.pieceIndexes[toIndex]));
					MoveList.addMove(MoveUtil.createPromotionAttack(MoveUtil.PROMOTION_R, fromIndex, toIndex, cb.pieceIndexes[toIndex]));
				}
			}
			moves &= moves - 1;
		}
	}

	public static void kingQuietMoves(long moves, final int fromIndex, final ChessBoard cb) {
		while (moves != 0) {
			final int toIndex = Long.numberOfTrailingZeros(moves);
			if (cb.isLegalKingMove(fromIndex, toIndex)) {
				MoveList.addMove(MoveUtil.createMove(fromIndex, Long.numberOfTrailingZeros(moves), ChessConstants.KING));
			}
			moves &= moves - 1;
		}
	}

	public static void kingAttackMoves(long moves, final int fromIndex, final ChessBoard cb) {
		while (moves != 0) {
			final int toIndex = Long.numberOfTrailingZeros(moves);
			if (cb.isLegalKingAttackMove(fromIndex, toIndex)) {
				MoveList.addMove(MoveUtil.createAttackMove(fromIndex, toIndex, ChessConstants.KING, cb.pieceIndexes[toIndex]));
			}
			moves &= moves - 1;
		}
	}

	public static void castlingMove(final int fromIndex, final int toIndex) {
		MoveList.addMove(MoveUtil.createCastlingMove(fromIndex, toIndex));
	}

	public static void EPAttackMove(final int fromIndex, final ChessBoard cb) {
		// pinned-pieces check not possible because a pawn can be attacked that is on the same row as the king:
		// 5.P.pKpPr
		if (cb.isLegalEPMove(fromIndex)) {
			MoveList.addMove(MoveUtil.createEPMove(fromIndex, cb.epIndex));
		}
	}
}
