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
import nl.s22k.chess.engine.EngineConstants;

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
			// double check, only the king can move
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
			// double check, only the king can attack
			addKingAttacks(cb, cb.friendlyPieces[cb.colorToMoveInverse]);
		}

		if (Statistics.ENABLED) {
			Statistics.movesGenerated += MoveList.movesLeft();
		}
	}

	private static void generateNotInCheckMoves(final ChessBoard cb) {

		// non pinned pieces
		addKingMoves(cb);
		addQueenMoves(cb.pieces[cb.colorToMove][QUEEN] & ~cb.pinnedPieces, cb, cb.emptySpaces);
		addRookMoves(cb.pieces[cb.colorToMove][ROOK] & ~cb.pinnedPieces, cb, cb.emptySpaces);
		addBishopMoves(cb.pieces[cb.colorToMove][BISHOP] & ~cb.pinnedPieces, cb, cb.emptySpaces);
		addNightMoves(cb.pieces[cb.colorToMove][NIGHT] & ~cb.pinnedPieces, cb.emptySpaces);
		addPawnMoves(cb.pieces[cb.colorToMove][PAWN] & ~cb.pinnedPieces, cb, cb.emptySpaces);

		// pinned pieces
		long piece = cb.friendlyPieces[cb.colorToMove] & cb.pinnedPieces;
		while (piece != 0) {
			switch (cb.pieceIndexes[Long.numberOfTrailingZeros(piece)]) {
			case PAWN:
				addPawnMoves(Long.lowestOneBit(piece), cb,
						cb.emptySpaces & ChessConstants.PINNED_MOVEMENT[Long.numberOfTrailingZeros(piece)][cb.kingIndex[cb.colorToMove]]);
				break;
			case BISHOP:
				addBishopMoves(Long.lowestOneBit(piece), cb,
						cb.emptySpaces & ChessConstants.PINNED_MOVEMENT[Long.numberOfTrailingZeros(piece)][cb.kingIndex[cb.colorToMove]]);
				break;
			case ROOK:
				addRookMoves(Long.lowestOneBit(piece), cb,
						cb.emptySpaces & ChessConstants.PINNED_MOVEMENT[Long.numberOfTrailingZeros(piece)][cb.kingIndex[cb.colorToMove]]);
				break;
			case QUEEN:
				addQueenMoves(Long.lowestOneBit(piece), cb,
						cb.emptySpaces & ChessConstants.PINNED_MOVEMENT[Long.numberOfTrailingZeros(piece)][cb.kingIndex[cb.colorToMove]]);
			}
			piece &= piece - 1;
		}

	}

	private static void generateOutOfSlidingCheckMoves(final ChessBoard cb) {

		// TODO when check is blocked -> pinned piece

		// move king or block sliding piece
		final long inBetween = ChessConstants.ROOK_IN_BETWEEN[cb.kingIndex[cb.colorToMove]][Long.numberOfTrailingZeros(cb.checkingPieces)]
				| ChessConstants.BISHOP_IN_BETWEEN[cb.kingIndex[cb.colorToMove]][Long.numberOfTrailingZeros(cb.checkingPieces)];

		addNightMoves(cb.pieces[cb.colorToMove][NIGHT] & ~cb.pinnedPieces, inBetween);
		addBishopMoves(cb.pieces[cb.colorToMove][BISHOP] & ~cb.pinnedPieces, cb, inBetween);
		addRookMoves(cb.pieces[cb.colorToMove][ROOK] & ~cb.pinnedPieces, cb, inBetween);
		addQueenMoves(cb.pieces[cb.colorToMove][QUEEN] & ~cb.pinnedPieces, cb, inBetween);
		addPawnMoves(cb.pieces[cb.colorToMove][PAWN] & ~cb.pinnedPieces, cb, inBetween);
		addKingMoves(cb);
	}

	private static void generateNotInCheckAttacks(final ChessBoard cb) {

		final long enemies = cb.friendlyPieces[cb.colorToMoveInverse];

		// non pinned pieces
		addEpAttacks(cb);
		addPawnAttacks(cb.pieces[cb.colorToMove][PAWN] & ~cb.pinnedPieces, cb, enemies, cb.emptySpaces);
		addNightAttacks(cb.pieces[cb.colorToMove][NIGHT] & ~cb.pinnedPieces, cb, enemies);
		addRookAttacks(cb.pieces[cb.colorToMove][ROOK] & ~cb.pinnedPieces, cb, enemies);
		addBishopAttacks(cb.pieces[cb.colorToMove][BISHOP] & ~cb.pinnedPieces, cb, enemies);
		addQueenAttacks(cb.pieces[cb.colorToMove][QUEEN] & ~cb.pinnedPieces, cb, enemies);
		addKingAttacks(cb, enemies);

		// pinned pieces
		long piece = cb.friendlyPieces[cb.colorToMove] & cb.pinnedPieces;
		while (piece != 0) {
			switch (cb.pieceIndexes[Long.numberOfTrailingZeros(piece)]) {
			case PAWN:
				addPawnAttacks(Long.lowestOneBit(piece), cb,
						enemies & ChessConstants.PINNED_MOVEMENT[Long.numberOfTrailingZeros(piece)][cb.kingIndex[cb.colorToMove]],
						cb.emptySpaces & ChessConstants.PINNED_MOVEMENT[Long.numberOfTrailingZeros(piece)][cb.kingIndex[cb.colorToMove]]);
				break;
			case BISHOP:
				addBishopAttacks(Long.lowestOneBit(piece), cb,
						enemies & ChessConstants.PINNED_MOVEMENT[Long.numberOfTrailingZeros(piece)][cb.kingIndex[cb.colorToMove]]);
				break;
			case ROOK:
				addRookAttacks(Long.lowestOneBit(piece), cb,
						enemies & ChessConstants.PINNED_MOVEMENT[Long.numberOfTrailingZeros(piece)][cb.kingIndex[cb.colorToMove]]);
				break;
			case QUEEN:
				addQueenAttacks(Long.lowestOneBit(piece), cb,
						enemies & ChessConstants.PINNED_MOVEMENT[Long.numberOfTrailingZeros(piece)][cb.kingIndex[cb.colorToMove]]);
			}
			piece &= piece - 1;
		}

	}

	private static void generateOutOfCheckAttacks(final ChessBoard cb) {
		// attack attacker
		addEpAttacks(cb);
		addPawnAttacks(cb.pieces[cb.colorToMove][PAWN] & ~cb.pinnedPieces, cb, cb.checkingPieces, cb.emptySpaces);
		addNightAttacks(cb.pieces[cb.colorToMove][NIGHT] & ~cb.pinnedPieces, cb, cb.checkingPieces);
		addBishopAttacks(cb.pieces[cb.colorToMove][BISHOP] & ~cb.pinnedPieces, cb, cb.checkingPieces);
		addRookAttacks(cb.pieces[cb.colorToMove][ROOK] & ~cb.pinnedPieces, cb, cb.checkingPieces);
		addQueenAttacks(cb.pieces[cb.colorToMove][QUEEN] & ~cb.pinnedPieces, cb, cb.checkingPieces);
		addKingAttacks(cb, cb.friendlyPieces[cb.colorToMoveInverse]);
	}

	private static void addPawnAttacks(final long pawns, final ChessBoard cb, final long enemies, final long emptySpaces) {
		// pawns (non-promoting)
		long piece = pawns & Bitboard.RANK_NON_PROMOTION[cb.colorToMove];
		while (piece != 0) {
			final int fromIndex = Long.numberOfTrailingZeros(piece);
			long moves = StaticMoves.PAWN_ATTACKS[cb.colorToMove][fromIndex] & enemies;
			while (moves != 0) {
				final int toIndex = Long.numberOfTrailingZeros(moves);
				MoveList.addMove(MoveUtil.createAttackMove(fromIndex, toIndex, PAWN, cb.pieceIndexes[toIndex]));
				moves &= moves - 1;
			}
			piece &= piece - 1;
		}

		// pawns (promoting)
		if (cb.colorToMove == ChessConstants.WHITE) {
			piece = pawns & Bitboard.RANK_7;
			while (piece != 0) {
				final int fromIndex = Long.numberOfTrailingZeros(piece);

				// promotion move
				if ((Long.lowestOneBit(piece) << 8 & emptySpaces) != 0) {
					addPromotionMove(fromIndex, fromIndex + 8);
				}

				// promotion attacks
				addPromotionAttacks(StaticMoves.PAWN_ATTACKS[ChessConstants.WHITE][fromIndex] & enemies, fromIndex, cb);

				piece &= piece - 1;
			}
		} else {
			piece = pawns & Bitboard.RANK_2;
			while (piece != 0) {
				final int fromIndex = Long.numberOfTrailingZeros(piece);

				// promotion move
				if ((Long.lowestOneBit(piece) >>> 8 & emptySpaces) != 0) {
					addPromotionMove(fromIndex, fromIndex - 8);
				}

				// promotion attacks
				addPromotionAttacks(StaticMoves.PAWN_ATTACKS[ChessConstants.BLACK][fromIndex] & enemies, fromIndex, cb);

				piece &= piece - 1;
			}
		}
	}

	private static void addBishopAttacks(long piece, final ChessBoard cb, final long possiblePositions) {
		while (piece != 0) {
			final int fromIndex = Long.numberOfTrailingZeros(piece);
			long moves = MagicUtil.getBishopMoves(fromIndex, cb.allPieces) & possiblePositions;
			while (moves != 0) {
				final int toIndex = Long.numberOfTrailingZeros(moves);
				MoveList.addMove(MoveUtil.createAttackMove(fromIndex, toIndex, BISHOP, cb.pieceIndexes[toIndex]));
				moves &= moves - 1;
			}
			piece &= piece - 1;
		}
	}

	private static void addRookAttacks(long piece, final ChessBoard cb, final long possiblePositions) {
		while (piece != 0) {
			final int fromIndex = Long.numberOfTrailingZeros(piece);
			long moves = MagicUtil.getRookMoves(fromIndex, cb.allPieces) & possiblePositions;
			while (moves != 0) {
				final int toIndex = Long.numberOfTrailingZeros(moves);
				MoveList.addMove(MoveUtil.createAttackMove(fromIndex, toIndex, ROOK, cb.pieceIndexes[toIndex]));
				moves &= moves - 1;
			}
			piece &= piece - 1;
		}
	}

	private static void addQueenAttacks(long piece, final ChessBoard cb, final long possiblePositions) {
		while (piece != 0) {
			final int fromIndex = Long.numberOfTrailingZeros(piece);
			long moves = (MagicUtil.getBishopMoves(fromIndex, cb.allPieces) | MagicUtil.getRookMoves(fromIndex, cb.allPieces)) & possiblePositions;
			while (moves != 0) {
				final int toIndex = Long.numberOfTrailingZeros(moves);
				MoveList.addMove(MoveUtil.createAttackMove(fromIndex, toIndex, QUEEN, cb.pieceIndexes[toIndex]));
				moves &= moves - 1;
			}
			piece &= piece - 1;
		}
	}

	private static void addBishopMoves(long piece, final ChessBoard cb, final long possiblePositions) {
		while (piece != 0) {
			final int fromIndex = Long.numberOfTrailingZeros(piece);
			long moves = MagicUtil.getBishopMoves(fromIndex, cb.allPieces, cb.friendlyPieces[cb.colorToMove]) & possiblePositions;
			while (moves != 0) {
				MoveList.addMove(MoveUtil.createMove(fromIndex, Long.numberOfTrailingZeros(moves), BISHOP));
				moves &= moves - 1;
			}

			piece &= piece - 1;
		}
	}

	private static void addQueenMoves(long piece, final ChessBoard cb, final long possiblePositions) {
		while (piece != 0) {
			final int fromIndex = Long.numberOfTrailingZeros(piece);
			long moves = (MagicUtil.getBishopMoves(fromIndex, cb.allPieces, cb.friendlyPieces[cb.colorToMove])
					| MagicUtil.getRookMoves(fromIndex, cb.allPieces, cb.friendlyPieces[cb.colorToMove])) & possiblePositions;
			while (moves != 0) {
				MoveList.addMove(MoveUtil.createMove(fromIndex, Long.numberOfTrailingZeros(moves), QUEEN));
				moves &= moves - 1;
			}

			piece &= piece - 1;
		}
	}

	private static void addRookMoves(long piece, final ChessBoard cb, final long possiblePositions) {
		while (piece != 0) {
			final int fromIndex = Long.numberOfTrailingZeros(piece);
			long moves = MagicUtil.getRookMoves(fromIndex, cb.allPieces, cb.friendlyPieces[cb.colorToMove]) & possiblePositions;
			while (moves != 0) {
				MoveList.addMove(MoveUtil.createMove(fromIndex, Long.numberOfTrailingZeros(moves), ROOK));
				moves &= moves - 1;
			}
			piece &= piece - 1;
		}
	}

	private static void addNightMoves(long piece, final long possiblePositions) {
		while (piece != 0) {
			final int fromIndex = Long.numberOfTrailingZeros(piece);
			long moves = StaticMoves.KNIGHT_MOVES[fromIndex] & possiblePositions;
			while (moves != 0) {
				MoveList.addMove(MoveUtil.createMove(fromIndex, Long.numberOfTrailingZeros(moves), NIGHT));
				moves &= moves - 1;
			}
			piece &= piece - 1;
		}
	}

	private static void addPawnMoves(final long pawns, final ChessBoard cb, final long possiblePositions) {
		if (cb.colorToMove == ChessConstants.WHITE) {
			// 1-move
			long piece = pawns & (possiblePositions >>> 8) & Bitboard.RANK_23456;
			while (piece != 0) {
				MoveList.addMove(MoveUtil.createWhitePawnMove(Long.numberOfTrailingZeros(piece)));
				piece &= piece - 1;
			}
			// 2-move
			piece = pawns & (possiblePositions >>> 16) & Bitboard.RANK_2;
			while (piece != 0) {
				if ((cb.emptySpaces & (Long.lowestOneBit(piece) << 8)) != 0) {
					MoveList.addMove(MoveUtil.createWhitePawn2Move(Long.numberOfTrailingZeros(piece)));
				}
				piece &= piece - 1;
			}
		} else {
			// 1-move
			long piece = pawns & (possiblePositions << 8) & Bitboard.RANK_34567;
			while (piece != 0) {
				MoveList.addMove(MoveUtil.createBlackPawnMove(Long.numberOfTrailingZeros(piece)));
				piece &= piece - 1;
			}
			// 2-move
			piece = pawns & (possiblePositions << 16) & Bitboard.RANK_7;
			while (piece != 0) {
				if ((cb.emptySpaces & (Long.lowestOneBit(piece) >>> 8)) != 0) {
					MoveList.addMove(MoveUtil.createBlackPawn2Move(Long.numberOfTrailingZeros(piece)));
				}
				piece &= piece - 1;
			}
		}
	}

	private static void addKingMoves(final ChessBoard cb) {
		final int fromIndex = cb.kingIndex[cb.colorToMove];
		long moves = StaticMoves.KING_MOVES[fromIndex] & cb.emptySpaces;
		while (moves != 0) {
			MoveList.addMove(MoveUtil.createMove(fromIndex, Long.numberOfTrailingZeros(moves), ChessConstants.KING));
			moves &= moves - 1;
		}

		// castling
		if (cb.checkingPieces == 0) {
			long castlingIndexes = CastlingUtil.getCastlingIndexes(cb);
			while (castlingIndexes != 0) {
				final int castlingIndex = Long.numberOfTrailingZeros(castlingIndexes);
				// no piece in between?
				if (CastlingUtil.isValidCastlingMove(cb, fromIndex, castlingIndex)) {
					MoveList.addMove(MoveUtil.createCastlingMove(fromIndex, castlingIndex));
				}
				castlingIndexes &= castlingIndexes - 1;
			}
		}
	}

	private static void addKingAttacks(final ChessBoard cb, final long possiblePositions) {
		final int fromIndex = cb.kingIndex[cb.colorToMove];
		long moves = StaticMoves.KING_MOVES[fromIndex] & possiblePositions;
		while (moves != 0) {
			final int toIndex = Long.numberOfTrailingZeros(moves);
			MoveList.addMove(MoveUtil.createAttackMove(fromIndex, toIndex, ChessConstants.KING, cb.pieceIndexes[toIndex]));
			moves &= moves - 1;
		}
	}

	private static void addNightAttacks(long piece, final ChessBoard cb, final long possiblePositions) {
		while (piece != 0) {
			final int fromIndex = Long.numberOfTrailingZeros(piece);
			long moves = StaticMoves.KNIGHT_MOVES[fromIndex] & possiblePositions;
			while (moves != 0) {
				final int toIndex = Long.numberOfTrailingZeros(moves);
				MoveList.addMove(MoveUtil.createAttackMove(fromIndex, toIndex, NIGHT, cb.pieceIndexes[toIndex]));
				moves &= moves - 1;
			}
			piece &= piece - 1;
		}
	}

	private static void addEpAttacks(final ChessBoard cb) {
		if (cb.epIndex == 0) {
			return;
		}
		long piece = cb.pieces[cb.colorToMove][PAWN] & StaticMoves.PAWN_ATTACKS[cb.colorToMoveInverse][cb.epIndex];
		while (piece != 0) {
			MoveList.addMove(MoveUtil.createEPMove(Long.numberOfTrailingZeros(piece), cb.epIndex));
			piece &= piece - 1;
		}
	}

	private static void addPromotionMove(final int fromIndex, final int toIndex) {
		MoveList.addMove(MoveUtil.createPromotionMove(MoveUtil.TYPE_PROMOTION_Q, fromIndex, toIndex));
		MoveList.addMove(MoveUtil.createPromotionMove(MoveUtil.TYPE_PROMOTION_N, fromIndex, toIndex));
		if (EngineConstants.GENERATE_BR_PROMOTIONS) {
			MoveList.addMove(MoveUtil.createPromotionMove(MoveUtil.TYPE_PROMOTION_B, fromIndex, toIndex));
			MoveList.addMove(MoveUtil.createPromotionMove(MoveUtil.TYPE_PROMOTION_R, fromIndex, toIndex));
		}
	}

	private static void addPromotionAttacks(long moves, final int fromIndex, final ChessBoard cb) {
		while (moves != 0) {
			final int toIndex = Long.numberOfTrailingZeros(moves);
			MoveList.addMove(MoveUtil.createPromotionAttack(MoveUtil.TYPE_PROMOTION_Q, fromIndex, toIndex, cb.pieceIndexes[toIndex]));
			MoveList.addMove(MoveUtil.createPromotionAttack(MoveUtil.TYPE_PROMOTION_N, fromIndex, toIndex, cb.pieceIndexes[toIndex]));
			if (EngineConstants.GENERATE_BR_PROMOTIONS) {
				MoveList.addMove(MoveUtil.createPromotionAttack(MoveUtil.TYPE_PROMOTION_B, fromIndex, toIndex, cb.pieceIndexes[toIndex]));
				MoveList.addMove(MoveUtil.createPromotionAttack(MoveUtil.TYPE_PROMOTION_R, fromIndex, toIndex, cb.pieceIndexes[toIndex]));
			}
			moves &= moves - 1;
		}
	}
}
