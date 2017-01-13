package nl.s22k.chess.move;

import static nl.s22k.chess.ChessConstants.BISHOP;
import static nl.s22k.chess.ChessConstants.NIGHT;
import static nl.s22k.chess.ChessConstants.PAWN;
import static nl.s22k.chess.ChessConstants.PAWN_2_MOVE_DEFAULT_COLUMN;
import static nl.s22k.chess.ChessConstants.QUEEN;
import static nl.s22k.chess.ChessConstants.ROOK;

import nl.s22k.chess.CastlingUtil;
import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.Statistics;
import nl.s22k.chess.Util;

public final class MoveGenerator {

	public static final int[] PAWN_2_MOVE_IN_BETWEEN = new int[] { 8, -8 };
	public static final int[] PAWN_2_MOVE = new int[] { 16, -16 };

	private static final long[] EP_FROM_ROW = new long[] { 0xff00000000L, 0xff000000L };
	private static final int[][] EP_TO_INDEX = new int[][] { { 7, 9 }, { -9, -7 } };

	public static void generateMoves(final ChessBoard cb) {

		switch (Long.bitCount(cb.checkingPieces)) {
		case 0:
			// not in-check
			generateNotInCheckMoves(cb);
			break;
		case 1:
			// in-check
			switch (cb.pieceIndexes[Long.numberOfTrailingZeros(cb.checkingPieces)]) {
			case PAWN:
				// fall-through
			case NIGHT:
				generateOutOfNonSlidingCheckMoves(cb);
				break;
			default:
				generateOutOfSlidingCheckMoves(cb);
			}
			break;
		default:
			// double check
			generateOutOfNonSlidingCheckMoves(cb);
		}

		if (Statistics.ENABLED) {
			Statistics.movesGenerated += MoveList.movesLeft();
		}
	}

	public static void generateAttacks(final ChessBoard cb) {

		switch (Long.bitCount(cb.checkingPieces)) {
		case 0:
			// not in-check
			generateNotInCheckAttacks(cb);
			break;
		case 1:
			// in-check
			switch (cb.pieceIndexes[Long.numberOfTrailingZeros(cb.checkingPieces)]) {
			case PAWN:
				// fall-through
			case NIGHT:
				generateOutOfNonSlidingCheckAttacks(cb);
				break;
			default:
				generateOutOfSlidingCheckAttacks(cb);
			}
			break;
		default:
			// double check
			generateOutOfNonSlidingCheckAttacks(cb);
		}

		if (Statistics.ENABLED) {
			Statistics.movesGenerated += MoveList.movesLeft();
		}
	}

	private static void generateNotInCheckMoves(final ChessBoard cb) {

		int fromIndex;
		long moves;

		// knights
		long piece = cb.pieces[cb.colorToMove][NIGHT];
		while (piece != 0) {
			fromIndex = Long.numberOfTrailingZeros(piece);
			moves = StaticMoves.KNIGHT_MOVES[fromIndex];
			MoveListAdd.quietNotInCheckMoves(moves & cb.emptySpaces, fromIndex, cb);
			piece &= piece - 1;
		}

		// rooks + queens
		piece = cb.pieces[cb.colorToMove][ROOK] | cb.pieces[cb.colorToMove][QUEEN];
		while (piece != 0) {
			fromIndex = Long.numberOfTrailingZeros(piece);
			moves = MagicUtil.getRookMoves(fromIndex, cb.allPieces, cb.friendlyPieces[cb.colorToMove]);
			MoveListAdd.quietNotInCheckMoves(moves & cb.emptySpaces, fromIndex, cb);
			piece &= piece - 1;
		}

		// bishops + queens
		piece = cb.pieces[cb.colorToMove][BISHOP] | cb.pieces[cb.colorToMove][QUEEN];
		while (piece != 0) {
			fromIndex = Long.numberOfTrailingZeros(piece);
			moves = MagicUtil.getBishopMoves(fromIndex, cb.allPieces, cb.friendlyPieces[cb.colorToMove]);
			MoveListAdd.quietNotInCheckMoves(moves & cb.emptySpaces, fromIndex, cb);
			piece &= piece - 1;
		}

		// pawns
		piece = cb.pieces[cb.colorToMove][PAWN];
		while (piece != 0) {
			fromIndex = Long.numberOfTrailingZeros(piece);

			// 1-move
			MoveListAdd.quietNotInCheckMoves(StaticMoves.PAWN_MOVES_1[cb.colorToMove][fromIndex] & cb.emptySpaces, fromIndex, cb);

			// 2-move
			moves = StaticMoves.PAWN_MOVES_2[cb.colorToMove][fromIndex];
			if (moves > 0 && (cb.allPieces & Util.POWER_LOOKUP[fromIndex + PAWN_2_MOVE_IN_BETWEEN[cb.colorToMove]]) == 0) {
				MoveListAdd.quietNotInCheckMoves(moves & cb.emptySpaces, fromIndex, cb);
			}

			piece &= piece - 1;
		}

		// king
		fromIndex = cb.kingIndex[cb.colorToMove];
		moves = StaticMoves.KING_MOVES[fromIndex];
		MoveListAdd.kingQuietMoves(moves & cb.emptySpaces, fromIndex, cb);

		// castling
		long castlingIndexes = CastlingUtil.getCastlingIndexes(cb);
		while (castlingIndexes != 0) {
			final int castlingIndex = Long.numberOfTrailingZeros(castlingIndexes);
			// no piece in between?
			if (CastlingUtil.isValidCastlingMove(cb, fromIndex, castlingIndex)) {
				MoveListAdd.castlingMove(fromIndex, castlingIndex);
			}
			castlingIndexes &= castlingIndexes - 1;
		}
	}

	private static void generateNotInCheckAttacks(final ChessBoard cb) {

		int fromIndex;
		long moves;

		// knights
		long piece = cb.pieces[cb.colorToMove][NIGHT];
		while (piece != 0) {
			fromIndex = Long.numberOfTrailingZeros(piece);
			moves = StaticMoves.KNIGHT_MOVES[fromIndex];
			MoveListAdd.notInCheckAttacks(moves & cb.friendlyPieces[cb.colorToMoveInverse], fromIndex, cb);
			piece &= piece - 1;
		}

		// rooks + queens
		piece = cb.pieces[cb.colorToMove][ROOK] | cb.pieces[cb.colorToMove][QUEEN];
		while (piece != 0) {
			fromIndex = Long.numberOfTrailingZeros(piece);
			moves = MagicUtil.getRookMoves(fromIndex, cb.allPieces, cb.friendlyPieces[cb.colorToMove]);
			MoveListAdd.notInCheckAttacks(moves & cb.friendlyPieces[cb.colorToMoveInverse], fromIndex, cb);
			piece &= piece - 1;
		}

		// bishops + queens
		piece = cb.pieces[cb.colorToMove][BISHOP] | cb.pieces[cb.colorToMove][QUEEN];
		while (piece != 0) {
			fromIndex = Long.numberOfTrailingZeros(piece);
			moves = MagicUtil.getBishopMoves(fromIndex, cb.allPieces, cb.friendlyPieces[cb.colorToMove]);
			MoveListAdd.notInCheckAttacks(moves & cb.friendlyPieces[cb.colorToMoveInverse], fromIndex, cb);
			piece &= piece - 1;
		}

		// pawns
		piece = cb.pieces[cb.colorToMove][PAWN];
		while (piece != 0) {
			fromIndex = Long.numberOfTrailingZeros(piece);

			// promotion move
			MoveListAdd.notInCheckPromotionMove(StaticMoves.PAWN_PROMOTION_MOVES[cb.colorToMove][fromIndex] & cb.emptySpaces, fromIndex, cb);

			// attack and promotion attack
			MoveListAdd.notInCheckAttacks(StaticMoves.PAWN_NON_PROMOTION_ATTACKS[cb.colorToMove][fromIndex] & cb.friendlyPieces[cb.colorToMoveInverse],
					fromIndex, cb);
			MoveListAdd.promotionNotInCheckAttacks(StaticMoves.PAWN_PROMOTION_ATTACKS[cb.colorToMove][fromIndex] & cb.friendlyPieces[cb.colorToMoveInverse],
					fromIndex, cb);

			// ep-move
			if (cb.pawn2MoveFromColumn < PAWN_2_MOVE_DEFAULT_COLUMN && (Util.POWER_LOOKUP[fromIndex] & EP_FROM_ROW[cb.colorToMove]) != 0) {
				if ((fromIndex & 7) - cb.pawn2MoveFromColumn == 1) {
					MoveListAdd.EPAttackMove(fromIndex, fromIndex + EP_TO_INDEX[cb.colorToMove][0], cb);
				} else if ((fromIndex & 7) - cb.pawn2MoveFromColumn == -1) {
					MoveListAdd.EPAttackMove(fromIndex, fromIndex + EP_TO_INDEX[cb.colorToMove][1], cb);
				}
			}

			piece &= piece - 1;
		}

		// king
		fromIndex = cb.kingIndex[cb.colorToMove];
		moves = StaticMoves.KING_MOVES[fromIndex];
		MoveListAdd.kingAttackMoves(moves & cb.friendlyPieces[cb.colorToMoveInverse], fromIndex, cb);

	}

	private static void generateOutOfSlidingCheckMoves(final ChessBoard cb) {

		// TODO use rays for determining blocking positions between checkingpiece and king?
		// TODO when check is blocked -> pinned piece
		// move king or block sliding piece

		long moves;
		int fromIndex;

		// knights
		long piece = cb.pieces[cb.colorToMove][NIGHT];
		while (piece != 0) {
			fromIndex = Long.numberOfTrailingZeros(piece);
			moves = StaticMoves.KNIGHT_MOVES[fromIndex];
			MoveListAdd.quietInCheckMoves(moves & cb.emptySpaces, fromIndex, cb);
			piece &= piece - 1;
		}

		// rooks + queens
		piece = cb.pieces[cb.colorToMove][ROOK] | cb.pieces[cb.colorToMove][QUEEN];
		while (piece != 0) {
			fromIndex = Long.numberOfTrailingZeros(piece);
			moves = MagicUtil.getRookMoves(fromIndex, cb.allPieces, cb.friendlyPieces[cb.colorToMove]);
			MoveListAdd.quietInCheckMoves(moves & cb.emptySpaces, fromIndex, cb);
			piece &= piece - 1;
		}

		// bishops + queens
		piece = cb.pieces[cb.colorToMove][BISHOP] | cb.pieces[cb.colorToMove][QUEEN];
		while (piece != 0) {
			fromIndex = Long.numberOfTrailingZeros(piece);
			moves = MagicUtil.getBishopMoves(fromIndex, cb.allPieces, cb.friendlyPieces[cb.colorToMove]);
			MoveListAdd.quietInCheckMoves(moves & cb.emptySpaces, fromIndex, cb);
			piece &= piece - 1;
		}

		// pawns
		piece = cb.pieces[cb.colorToMove][PAWN];
		while (piece != 0) {
			fromIndex = Long.numberOfTrailingZeros(piece);

			// 1-move
			MoveListAdd.quietInCheckMoves(StaticMoves.PAWN_MOVES_1[cb.colorToMove][fromIndex] & cb.emptySpaces, fromIndex, cb);

			// 2-move
			moves = StaticMoves.PAWN_MOVES_2[cb.colorToMove][fromIndex];
			if (moves > 0 && (cb.allPieces & Util.POWER_LOOKUP[fromIndex + PAWN_2_MOVE_IN_BETWEEN[cb.colorToMove]]) == 0) {
				MoveListAdd.quietInCheckMoves(moves & cb.emptySpaces, fromIndex, cb);
			}

			piece &= piece - 1;
		}

		// king
		fromIndex = cb.kingIndex[cb.colorToMove];
		moves = StaticMoves.KING_MOVES[fromIndex];
		MoveListAdd.kingQuietMoves(moves & cb.emptySpaces, fromIndex, cb);

	}

	private static void generateOutOfSlidingCheckAttacks(final ChessBoard cb) {

		// attack attacker

		long moves;
		int fromIndex;

		// knights
		long piece = cb.pieces[cb.colorToMove][NIGHT];
		while (piece != 0) {
			fromIndex = Long.numberOfTrailingZeros(piece);
			moves = StaticMoves.KNIGHT_MOVES[fromIndex];
			MoveListAdd.inCheckAttacks(cb.checkingPieces & moves, fromIndex, cb);
			piece &= piece - 1;
		}

		// rooks + queens
		piece = cb.pieces[cb.colorToMove][ROOK] | cb.pieces[cb.colorToMove][QUEEN];
		while (piece != 0) {
			fromIndex = Long.numberOfTrailingZeros(piece);
			moves = MagicUtil.getRookMoves(fromIndex, cb.allPieces, cb.friendlyPieces[cb.colorToMove]);
			MoveListAdd.inCheckAttacks(moves & cb.checkingPieces, fromIndex, cb);
			piece &= piece - 1;
		}

		// bishops + queens
		piece = cb.pieces[cb.colorToMove][BISHOP] | cb.pieces[cb.colorToMove][QUEEN];
		while (piece != 0) {
			fromIndex = Long.numberOfTrailingZeros(piece);
			moves = MagicUtil.getBishopMoves(fromIndex, cb.allPieces, cb.friendlyPieces[cb.colorToMove]);
			MoveListAdd.inCheckAttacks(moves & cb.checkingPieces, fromIndex, cb);
			piece &= piece - 1;
		}

		// pawns
		piece = cb.pieces[cb.colorToMove][PAWN];
		while (piece != 0) {
			fromIndex = Long.numberOfTrailingZeros(piece);

			// promotion move
			MoveListAdd.inCheckPromotionMove(StaticMoves.PAWN_PROMOTION_MOVES[cb.colorToMove][fromIndex] & cb.emptySpaces, fromIndex, cb);

			// attack and promotion attack
			MoveListAdd.inCheckAttacks(StaticMoves.PAWN_NON_PROMOTION_ATTACKS[cb.colorToMove][fromIndex] & cb.checkingPieces, fromIndex, cb);
			MoveListAdd.promotionInCheckAttacks(StaticMoves.PAWN_PROMOTION_ATTACKS[cb.colorToMove][fromIndex] & cb.checkingPieces, fromIndex, cb);

			// ep-move
			if (cb.pawn2MoveFromColumn < PAWN_2_MOVE_DEFAULT_COLUMN && (Util.POWER_LOOKUP[fromIndex] & EP_FROM_ROW[cb.colorToMove]) != 0) {
				if ((fromIndex & 7) - cb.pawn2MoveFromColumn == 1) {
					MoveListAdd.EPAttackMove(fromIndex, fromIndex + EP_TO_INDEX[cb.colorToMove][0], cb);
				} else if ((fromIndex & 7) - cb.pawn2MoveFromColumn == -1) {
					MoveListAdd.EPAttackMove(fromIndex, fromIndex + EP_TO_INDEX[cb.colorToMove][1], cb);
				}
			}

			piece &= piece - 1;
		}

		// king
		fromIndex = cb.kingIndex[cb.colorToMove];
		moves = StaticMoves.KING_MOVES[fromIndex];
		MoveListAdd.kingAttackMoves(moves & cb.friendlyPieces[cb.colorToMoveInverse], fromIndex, cb);

	}

	private static void generateOutOfNonSlidingCheckMoves(final ChessBoard cb) {

		// move king
		MoveListAdd.kingQuietMoves(StaticMoves.KING_MOVES[cb.kingIndex[cb.colorToMove]] & cb.emptySpaces, cb.kingIndex[cb.colorToMove], cb);
	}

	private static void generateOutOfNonSlidingCheckAttacks(final ChessBoard cb) {

		// attack attacker
		int fromIndex;

		// knights
		long piece = cb.pieces[cb.colorToMove][NIGHT];
		while (piece != 0) {
			fromIndex = Long.numberOfTrailingZeros(piece);
			MoveListAdd.inCheckAttacks(cb.checkingPieces & StaticMoves.KNIGHT_MOVES[fromIndex], fromIndex, cb);
			piece &= piece - 1;
		}

		// rooks + queens
		piece = cb.pieces[cb.colorToMove][ROOK] | cb.pieces[cb.colorToMove][QUEEN];
		while (piece != 0) {
			fromIndex = Long.numberOfTrailingZeros(piece);
			MoveListAdd.inCheckAttacks(MagicUtil.getRookMoves(fromIndex, cb.allPieces, cb.friendlyPieces[cb.colorToMove]) & cb.checkingPieces, fromIndex, cb);
			piece &= piece - 1;
		}

		// bishops + queens
		piece = cb.pieces[cb.colorToMove][BISHOP] | cb.pieces[cb.colorToMove][QUEEN];
		while (piece != 0) {
			fromIndex = Long.numberOfTrailingZeros(piece);
			MoveListAdd.inCheckAttacks(MagicUtil.getBishopMoves(fromIndex, cb.allPieces, cb.friendlyPieces[cb.colorToMove]) & cb.checkingPieces, fromIndex, cb);
			piece &= piece - 1;
		}

		// pawns
		piece = cb.pieces[cb.colorToMove][PAWN];
		while (piece != 0) {
			fromIndex = Long.numberOfTrailingZeros(piece);

			// attack and promotion attack
			MoveListAdd.inCheckAttacks(StaticMoves.PAWN_NON_PROMOTION_ATTACKS[cb.colorToMove][fromIndex] & cb.checkingPieces, fromIndex, cb);
			MoveListAdd.promotionInCheckAttacks(StaticMoves.PAWN_PROMOTION_ATTACKS[cb.colorToMove][fromIndex] & cb.checkingPieces, fromIndex, cb);

			// ep-move
			if (cb.pawn2MoveFromColumn < PAWN_2_MOVE_DEFAULT_COLUMN && (Util.POWER_LOOKUP[fromIndex] & EP_FROM_ROW[cb.colorToMove]) != 0) {
				if ((fromIndex & 7) - cb.pawn2MoveFromColumn == 1) {
					MoveListAdd.EPAttackMove(fromIndex, fromIndex + EP_TO_INDEX[cb.colorToMove][0], cb);
				} else if ((fromIndex & 7) - cb.pawn2MoveFromColumn == -1) {
					MoveListAdd.EPAttackMove(fromIndex, fromIndex + EP_TO_INDEX[cb.colorToMove][1], cb);
				}
			}

			piece &= piece - 1;
		}

		// king
		MoveListAdd.kingAttackMoves(StaticMoves.KING_MOVES[cb.kingIndex[cb.colorToMove]] & cb.friendlyPieces[cb.colorToMoveInverse],
				cb.kingIndex[cb.colorToMove], cb);
	}

}
