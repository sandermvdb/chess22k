package nl.s22k.chess.move;

import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.ChessConstants;
import nl.s22k.chess.Util;

public class MoveListAdd {

	public static void quietNotInCheckMoves(long moves, final int fromIndex, final ChessBoard cb) {
		if ((cb.pinnedPieces[cb.colorToMove] & Util.POWER_LOOKUP[fromIndex]) == 0) {
			while (moves != 0) {
				MoveList.addMove(MoveUtil.createMove(fromIndex, Long.numberOfTrailingZeros(moves), cb.pieceIndexes[fromIndex]));
				moves &= moves - 1;
			}
		} else {
			// piece is pinned
			int toIndex;
			while (moves != 0) {
				toIndex = Long.numberOfTrailingZeros(moves);
				if (cb.isLegalMove(fromIndex, toIndex)) {
					MoveList.addMove(MoveUtil.createMove(fromIndex, toIndex, cb.pieceIndexes[fromIndex]));
				}
				moves &= moves - 1;
			}
		}
	}

	public static void quietInCheckMoves(long moves, final int fromIndex, final ChessBoard cb) {
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
		while (moves != 0) {
			if ((cb.pinnedPieces[cb.colorToMove] & Util.POWER_LOOKUP[fromIndex]) == 0) {
				final int toIndex = Long.numberOfTrailingZeros(moves);
				MoveList.addMove(MoveUtil.createAttackMove(fromIndex, toIndex, cb.pieceIndexes[fromIndex], cb.pieceIndexes[toIndex]));
				moves &= moves - 1;
			} else {
				// piece is pinned
				while (moves != 0) {
					final int toIndex = Long.numberOfTrailingZeros(moves);
					if (cb.isLegalAttackMove(fromIndex, toIndex)) {
						MoveList.addMove(MoveUtil.createAttackMove(fromIndex, toIndex, cb.pieceIndexes[fromIndex], cb.pieceIndexes[toIndex]));
					}
					moves &= moves - 1;
				}
			}
		}
	}

	public static void inCheckAttacks(long moves, final int fromIndex, final ChessBoard cb) {
		while (moves != 0) {
			final int toIndex = Long.numberOfTrailingZeros(moves);
			if (cb.isLegalAttackMove(fromIndex, toIndex)) {
				MoveList.addMove(MoveUtil.createAttackMove(fromIndex, toIndex, cb.pieceIndexes[fromIndex], cb.pieceIndexes[toIndex]));
			}
			moves &= moves - 1;
		}
	}

	public static void notInCheckPromotionMove(long move, final int fromIndex, final ChessBoard cb) {
		if (move == 0) {
			return;
		}
		if ((cb.pinnedPieces[cb.colorToMove] & Util.POWER_LOOKUP[fromIndex]) == 0) {
			MoveList.addMove(MoveUtil.createPromotionMove(fromIndex, Long.numberOfTrailingZeros(move)));
			MoveList.addMove(MoveUtil.createNightPromotionMove(fromIndex, Long.numberOfTrailingZeros(move)));
		} else {
			final int toIndex = Long.numberOfTrailingZeros(move);
			if (cb.isLegalMove(fromIndex, toIndex)) {
				MoveList.addMove(MoveUtil.createPromotionMove(fromIndex, toIndex));
				MoveList.addMove(MoveUtil.createNightPromotionMove(fromIndex, toIndex));
			}
		}
	}

	public static void inCheckPromotionMove(long move, final int fromIndex, final ChessBoard cb) {
		if (move == 0) {
			return;
		}
		final int toIndex = Long.numberOfTrailingZeros(move);
		if (cb.isLegalMove(fromIndex, toIndex)) {
			MoveList.addMove(MoveUtil.createPromotionMove(fromIndex, toIndex));
			MoveList.addMove(MoveUtil.createNightPromotionMove(fromIndex, toIndex));
		}
	}

	public static void promotionNotInCheckAttacks(long moves, final int fromIndex, final ChessBoard cb) {
		while (moves != 0) {
			if ((cb.pinnedPieces[cb.colorToMove] & Util.POWER_LOOKUP[fromIndex]) == 0) {
				final int toIndex = Long.numberOfTrailingZeros(moves);
				MoveList.addMove(MoveUtil.createPromotionAttack(fromIndex, toIndex, cb.pieceIndexes[toIndex]));
				MoveList.addMove(MoveUtil.createNightPromotionAttack(fromIndex, toIndex, cb.pieceIndexes[toIndex]));
				moves &= moves - 1;
			} else {
				while (moves != 0) {
					final int toIndex = Long.numberOfTrailingZeros(moves);
					if (cb.isLegalAttackMove(fromIndex, toIndex)) {
						MoveList.addMove(MoveUtil.createPromotionAttack(fromIndex, toIndex, cb.pieceIndexes[toIndex]));
						MoveList.addMove(MoveUtil.createNightPromotionAttack(fromIndex, toIndex, cb.pieceIndexes[toIndex]));
					}
					moves &= moves - 1;
				}
			}
		}
	}

	public static void promotionInCheckAttacks(long moves, final int fromIndex, final ChessBoard cb) {
		while (moves != 0) {
			final int toIndex = Long.numberOfTrailingZeros(moves);
			if (cb.isLegalAttackMove(fromIndex, toIndex)) {
				MoveList.addMove(MoveUtil.createPromotionAttack(fromIndex, toIndex, cb.pieceIndexes[toIndex]));
				MoveList.addMove(MoveUtil.createNightPromotionAttack(fromIndex, toIndex, cb.pieceIndexes[toIndex]));
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
