package nl.s22k.chess.move;

import static nl.s22k.chess.ChessConstants.BISHOP;
import static nl.s22k.chess.ChessConstants.NIGHT;
import static nl.s22k.chess.ChessConstants.PAWN;
import static nl.s22k.chess.ChessConstants.QUEEN;
import static nl.s22k.chess.ChessConstants.ROOK;

import nl.s22k.chess.Bitboard;
import nl.s22k.chess.CastlingUtil;
import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.ChessConstants;
import nl.s22k.chess.Statistics;
import nl.s22k.chess.Util;

public final class MoveGenerator {

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
				// move king
				addKingMoves(cb);
				break;
			default:
				generateOutOfSlidingCheckMoves(cb);
			}
			break;
		default:
			// double check
			// only the king can move
			addKingMoves(cb);
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
			generateOutOfCheckAttacks(cb);
			break;
		default:
			// double check: only the king can attack
			addKingAttacks(cb);
		}

		if (Statistics.ENABLED) {
			Statistics.movesGenerated += MoveList.movesLeft();
		}
	}

	private static void generateNotInCheckMoves(final ChessBoard cb) {

		int fromIndex;

		// non pinned pieces
		addNotPinnedNightMoves(cb, cb.emptySpaces);
		addNotPinnedRookAndQueenMoves(cb, cb.emptySpaces);
		addNotPinnedBishopAndQueenMoves(cb, cb.emptySpaces);
		addNotPinnedPawnMoves(cb, cb.emptySpaces);
		addKingMoves(cb);

		// pinned pieces that could move at the line of the pinner
		if ((cb.pinnedPieces & cb.friendlyPieces[cb.colorToMove]) != 0) {
			long piece = (cb.pieces[cb.colorToMove][ROOK] | cb.pieces[cb.colorToMove][QUEEN]) & cb.pinnedPieces;
			while (piece != 0) {
				fromIndex = Long.numberOfTrailingZeros(piece);
				MoveListAdd.quietNotInCheckPinnedMoves(MagicUtil.getRookMoves(fromIndex, cb.allPieces, cb.friendlyPieces[cb.colorToMove]) & cb.emptySpaces,
						fromIndex, cb);
				piece &= piece - 1;
			}
			piece = (cb.pieces[cb.colorToMove][BISHOP] | cb.pieces[cb.colorToMove][QUEEN]) & cb.pinnedPieces;
			while (piece != 0) {
				fromIndex = Long.numberOfTrailingZeros(piece);
				MoveListAdd.quietNotInCheckPinnedMoves(MagicUtil.getBishopMoves(fromIndex, cb.allPieces, cb.friendlyPieces[cb.colorToMove]) & cb.emptySpaces,
						fromIndex, cb);
				piece &= piece - 1;
			}

			// pawns
			piece = cb.pieces[cb.colorToMove][PAWN] & cb.pinnedPieces;
			while (piece != 0) {
				fromIndex = Long.numberOfTrailingZeros(piece);

				// 1-move
				MoveListAdd.quietNotInCheckPinnedMoves(StaticMoves.PAWN_MOVES_1[cb.colorToMove][fromIndex] & cb.emptySpaces, fromIndex, cb);

				// 2-move
				final long moves = StaticMoves.PAWN_MOVES_2[cb.colorToMove][fromIndex];
				if (moves > 0 && (cb.allPieces & StaticMoves.PAWN_MOVES_1[cb.colorToMove][fromIndex]) == 0) {
					MoveListAdd.quietNotInCheckPinnedMoves(moves & cb.emptySpaces, fromIndex, cb);
				}

				piece &= piece - 1;
			}
		}

	}

	private static void generateOutOfSlidingCheckMoves(final ChessBoard cb) {

		// TODO when check is blocked -> pinned piece

		// move king or block sliding piece
		final long inBetween = ChessConstants.IN_BETWEEN[cb.kingIndex[cb.colorToMove]][Long.numberOfTrailingZeros(cb.checkingPieces)];

		addNotPinnedNightMoves(cb, inBetween);
		addNotPinnedRookAndQueenMoves(cb, inBetween);
		addNotPinnedBishopAndQueenMoves(cb, inBetween);
		addNotPinnedPawnMoves(cb, inBetween);
		addKingMoves(cb);
	}

	private static void generateNotInCheckAttacks(final ChessBoard cb) {

		int fromIndex;

		addNotPinnedNightAttacks(cb, cb.friendlyPieces[cb.colorToMoveInverse]);

		// rooks + queens
		long piece = cb.pieces[cb.colorToMove][ROOK] | cb.pieces[cb.colorToMove][QUEEN];
		while (piece != 0) {
			fromIndex = Long.numberOfTrailingZeros(piece);
			MoveListAdd.notInCheckAttacks(MagicUtil.getRookMoves(fromIndex, cb.allPieces) & cb.friendlyPieces[cb.colorToMoveInverse], fromIndex, cb);
			piece &= piece - 1;
		}

		// bishops + queens
		piece = cb.pieces[cb.colorToMove][BISHOP] | cb.pieces[cb.colorToMove][QUEEN];
		while (piece != 0) {
			fromIndex = Long.numberOfTrailingZeros(piece);
			MoveListAdd.notInCheckAttacks(MagicUtil.getBishopMoves(fromIndex, cb.allPieces) & cb.friendlyPieces[cb.colorToMoveInverse], fromIndex, cb);
			piece &= piece - 1;
		}

		// pawns (non-promoting)
		piece = cb.pieces[cb.colorToMove][PAWN];
		while (piece != 0) {
			fromIndex = Long.numberOfTrailingZeros(piece);

			// attack
			MoveListAdd.notInCheckAttacks(StaticMoves.PAWN_NON_PROMOTION_ATTACKS[cb.colorToMove][fromIndex] & cb.friendlyPieces[cb.colorToMoveInverse],
					fromIndex, cb);

			// ep-move
			addEpAttacks(cb, fromIndex);

			piece &= piece - 1;
		}

		// pawns (promoting)
		piece = cb.pieces[cb.colorToMove][PAWN] & Bitboard.RANK_PROMOTION[cb.colorToMove];
		while (piece != 0) {
			fromIndex = Long.numberOfTrailingZeros(piece);

			// promotion move
			MoveListAdd.notInCheckPromotionMove(StaticMoves.PAWN_PROMOTION_MOVES[cb.colorToMove][fromIndex] & cb.emptySpaces, fromIndex, cb);

			// promotion attack
			MoveListAdd.promotionAttacks(StaticMoves.PAWN_PROMOTION_ATTACKS[cb.colorToMove][fromIndex] & cb.friendlyPieces[cb.colorToMoveInverse], fromIndex,
					cb);

			piece &= piece - 1;
		}

		// king
		addKingAttacks(cb);
	}

	private static void generateOutOfCheckAttacks(final ChessBoard cb) {

		// attack attacker
		int fromIndex;

		addNotPinnedNightAttacks(cb, cb.checkingPieces);

		// rooks + queens
		long piece = (cb.pieces[cb.colorToMove][ROOK] | cb.pieces[cb.colorToMove][QUEEN]) & ~cb.pinnedPieces;
		while (piece != 0) {
			fromIndex = Long.numberOfTrailingZeros(piece);
			MoveListAdd.inCheckAttacks(MagicUtil.getRookMoves(fromIndex, cb.allPieces) & cb.checkingPieces, fromIndex, cb);
			piece &= piece - 1;
		}

		// bishops + queens
		piece = (cb.pieces[cb.colorToMove][BISHOP] | cb.pieces[cb.colorToMove][QUEEN]) & ~cb.pinnedPieces;
		while (piece != 0) {
			fromIndex = Long.numberOfTrailingZeros(piece);
			MoveListAdd.inCheckAttacks(MagicUtil.getBishopMoves(fromIndex, cb.allPieces) & cb.checkingPieces, fromIndex, cb);
			piece &= piece - 1;
		}

		// pawns (non-promoting)
		piece = cb.pieces[cb.colorToMove][PAWN] & ~cb.pinnedPieces;
		while (piece != 0) {
			fromIndex = Long.numberOfTrailingZeros(piece);

			// attack
			MoveListAdd.inCheckAttacks(StaticMoves.PAWN_NON_PROMOTION_ATTACKS[cb.colorToMove][fromIndex] & cb.checkingPieces, fromIndex, cb);

			// ep-move
			addEpAttacks(cb, fromIndex);

			piece &= piece - 1;
		}

		// pawns (promoting)
		piece = cb.pieces[cb.colorToMove][PAWN] & Bitboard.RANK_PROMOTION[cb.colorToMove] & ~cb.pinnedPieces;
		while (piece != 0) {
			fromIndex = Long.numberOfTrailingZeros(piece);

			// promotion move
			MoveListAdd.inCheckPromotionMove(StaticMoves.PAWN_PROMOTION_MOVES[cb.colorToMove][fromIndex] & cb.emptySpaces, fromIndex, cb);

			// promotion attack
			MoveListAdd.promotionAttacks(StaticMoves.PAWN_PROMOTION_ATTACKS[cb.colorToMove][fromIndex] & cb.checkingPieces, fromIndex, cb);

			piece &= piece - 1;
		}

		// king
		addKingAttacks(cb);
	}

	private static void addNotPinnedBishopAndQueenMoves(final ChessBoard cb, final long possiblePositions) {
		// bishops + queens
		long piece = (cb.pieces[cb.colorToMove][BISHOP] | cb.pieces[cb.colorToMove][QUEEN]) & ~cb.pinnedPieces;
		while (piece != 0) {
			final int fromIndex = Long.numberOfTrailingZeros(piece);
			MoveListAdd.quietNotPinnedMoves(MagicUtil.getBishopMoves(fromIndex, cb.allPieces, cb.friendlyPieces[cb.colorToMove]) & possiblePositions, fromIndex,
					cb.pieceIndexes[fromIndex]);
			piece &= piece - 1;
		}
	}

	private static void addNotPinnedRookAndQueenMoves(final ChessBoard cb, final long possiblePositions) {
		// rooks + queens
		long piece = (cb.pieces[cb.colorToMove][ROOK] | cb.pieces[cb.colorToMove][QUEEN]) & ~cb.pinnedPieces;
		while (piece != 0) {
			final int fromIndex = Long.numberOfTrailingZeros(piece);
			MoveListAdd.quietNotPinnedMoves(MagicUtil.getRookMoves(fromIndex, cb.allPieces, cb.friendlyPieces[cb.colorToMove]) & possiblePositions, fromIndex,
					cb.pieceIndexes[fromIndex]);
			piece &= piece - 1;
		}
	}

	private static void addNotPinnedNightMoves(final ChessBoard cb, final long possiblePositions) {
		long piece = cb.pieces[cb.colorToMove][NIGHT] & ~cb.pinnedPieces;
		while (piece != 0) {
			final int fromIndex = Long.numberOfTrailingZeros(piece);
			MoveListAdd.quietNotPinnedMoves(StaticMoves.KNIGHT_MOVES[fromIndex] & possiblePositions, fromIndex, NIGHT);
			piece &= piece - 1;
		}
	}

	private static void addNotPinnedPawnMoves(final ChessBoard cb, final long possiblePositions) {
		// pawns
		long piece = cb.pieces[cb.colorToMove][PAWN] & ~cb.pinnedPieces;
		while (piece != 0) {
			final int fromIndex = Long.numberOfTrailingZeros(piece);

			// 1-move
			MoveListAdd.quietNotPinnedMoves(StaticMoves.PAWN_MOVES_1[cb.colorToMove][fromIndex] & possiblePositions, fromIndex, PAWN);

			// 2-move
			final long moves = StaticMoves.PAWN_MOVES_2[cb.colorToMove][fromIndex];
			if (moves > 0 && (cb.allPieces & StaticMoves.PAWN_MOVES_1[cb.colorToMove][fromIndex]) == 0) {
				MoveListAdd.quietNotPinnedMoves(moves & possiblePositions, fromIndex, PAWN);
			}

			piece &= piece - 1;
		}
	}

	private static void addKingMoves(final ChessBoard cb) {
		final int fromIndex = cb.kingIndex[cb.colorToMove];
		MoveListAdd.kingQuietMoves(StaticMoves.KING_MOVES[fromIndex] & cb.emptySpaces, fromIndex, cb);

		// castling
		if (cb.checkingPieces == 0) {
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
	}

	private static void addKingAttacks(final ChessBoard cb) {
		final int fromIndex = cb.kingIndex[cb.colorToMove];
		MoveListAdd.kingAttackMoves(StaticMoves.KING_MOVES[fromIndex] & cb.friendlyPieces[cb.colorToMoveInverse], fromIndex, cb);
	}

	private static void addNotPinnedNightAttacks(final ChessBoard cb, final long possiblePositions) {
		long piece = cb.pieces[cb.colorToMove][NIGHT] & ~cb.pinnedPieces;
		while (piece != 0) {
			final int fromIndex = Long.numberOfTrailingZeros(piece);
			MoveListAdd.inCheckAttacks(StaticMoves.KNIGHT_MOVES[fromIndex] & possiblePositions, fromIndex, cb);
			piece &= piece - 1;
		}
	}

	private static void addEpAttacks(final ChessBoard cb, final int fromIndex) {
		if (cb.epIndex != 0 && (Util.POWER_LOOKUP[cb.epIndex] & StaticMoves.PAWN_NON_PROMOTION_ATTACKS[cb.colorToMove][fromIndex]) != 0) {
			MoveListAdd.EPAttackMove(fromIndex, cb);
		}
	}
}
