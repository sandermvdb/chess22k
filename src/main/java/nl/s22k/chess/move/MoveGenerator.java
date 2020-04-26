package nl.s22k.chess.move;

import static nl.s22k.chess.ChessConstants.ALL;
import static nl.s22k.chess.ChessConstants.BISHOP;
import static nl.s22k.chess.ChessConstants.BLACK;
import static nl.s22k.chess.ChessConstants.KING;
import static nl.s22k.chess.ChessConstants.NIGHT;
import static nl.s22k.chess.ChessConstants.PAWN;
import static nl.s22k.chess.ChessConstants.QUEEN;
import static nl.s22k.chess.ChessConstants.ROOK;
import static nl.s22k.chess.ChessConstants.WHITE;

import nl.s22k.chess.Bitboard;
import nl.s22k.chess.CastlingUtil;
import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.ChessConstants;
import nl.s22k.chess.engine.EngineConstants;
import nl.s22k.chess.search.ThreadData;

public class MoveGenerator {

	public static void generateMoves(final ThreadData threadData, final ChessBoard cb) {
		if (cb.checkingPieces == 0) {
			generateNotInCheckMoves(threadData, cb);
		} else if (Long.bitCount(cb.checkingPieces) == 1) {
			generateOutOfCheckMoves(threadData, cb);
		} else {
			// double check, only the king can move
			addKingMoves(threadData, cb);
		}
	}

	public static void generateAttacks(final ThreadData threadData, final ChessBoard cb) {
		if (cb.checkingPieces == 0) {
			generateNotInCheckAttacks(threadData, cb);
		} else if (Long.bitCount(cb.checkingPieces) == 1) {
			generateOutOfCheckAttacks(threadData, cb);
		} else {
			// double check, only the king can attack
			addKingAttacks(threadData, cb);
		}
	}

	private static void generateNotInCheckMoves(final ThreadData threadData, final ChessBoard cb) {

		// non pinned pieces
		final long nonPinned = ~cb.pinnedPieces;
		final long[] pieces = cb.pieces[cb.colorToMove];
		addNightMoves(threadData, pieces[NIGHT] & nonPinned, cb.emptySpaces);
		addBishopMoves(threadData, pieces[BISHOP] & nonPinned, cb.allPieces, cb.emptySpaces);
		addRookMoves(threadData, pieces[ROOK] & nonPinned, cb.allPieces, cb.emptySpaces);
		addQueenMoves(threadData, pieces[QUEEN] & nonPinned, cb.allPieces, cb.emptySpaces);
		addPawnMoves(threadData, pieces[PAWN] & nonPinned, cb, cb.emptySpaces);
		addKingMoves(threadData, cb);

		// pinned pieces
		long piece = cb.pieces[cb.colorToMove][ALL] & cb.pinnedPieces;
		while (piece != 0) {
			switch (cb.pieceIndexes[Long.numberOfTrailingZeros(piece)]) {
			case PAWN:
				addPawnMoves(threadData, Long.lowestOneBit(piece), cb,
						cb.emptySpaces & ChessConstants.PINNED_MOVEMENT[Long.numberOfTrailingZeros(piece)][cb.kingIndex[cb.colorToMove]]);
				break;
			case BISHOP:
				addBishopMoves(threadData, Long.lowestOneBit(piece), cb.allPieces,
						cb.emptySpaces & ChessConstants.PINNED_MOVEMENT[Long.numberOfTrailingZeros(piece)][cb.kingIndex[cb.colorToMove]]);
				break;
			case ROOK:
				addRookMoves(threadData, Long.lowestOneBit(piece), cb.allPieces,
						cb.emptySpaces & ChessConstants.PINNED_MOVEMENT[Long.numberOfTrailingZeros(piece)][cb.kingIndex[cb.colorToMove]]);
				break;
			case QUEEN:
				addQueenMoves(threadData, Long.lowestOneBit(piece), cb.allPieces,
						cb.emptySpaces & ChessConstants.PINNED_MOVEMENT[Long.numberOfTrailingZeros(piece)][cb.kingIndex[cb.colorToMove]]);
			}
			piece &= piece - 1;
		}

	}

	private static void generateOutOfCheckMoves(final ThreadData threadData, final ChessBoard cb) {
		final long inBetween = ChessConstants.IN_BETWEEN[cb.kingIndex[cb.colorToMove]][Long.numberOfTrailingZeros(cb.checkingPieces)];
		if (inBetween != 0) {
			final long nonPinned = ~cb.pinnedPieces;
			final long[] pieces = cb.pieces[cb.colorToMove];
			addPawnMoves(threadData, pieces[PAWN] & nonPinned, cb, inBetween);
			addNightMoves(threadData, pieces[NIGHT] & nonPinned, inBetween);
			addBishopMoves(threadData, pieces[BISHOP] & nonPinned, cb.allPieces, inBetween);
			addRookMoves(threadData, pieces[ROOK] & nonPinned, cb.allPieces, inBetween);
			addQueenMoves(threadData, pieces[QUEEN] & nonPinned, cb.allPieces, inBetween);
		}

		addKingMoves(threadData, cb);
	}

	private static void generateNotInCheckAttacks(final ThreadData threadData, final ChessBoard cb) {

		// non pinned pieces
		addEpAttacks(threadData, cb);
		final long nonPinned = ~cb.pinnedPieces;
		final long enemies = cb.pieces[cb.colorToMoveInverse][ALL];
		final long[] pieces = cb.pieces[cb.colorToMove];
		addPawnAttacksAndPromotions(threadData, pieces[PAWN] & nonPinned, cb, enemies, cb.emptySpaces);
		addNightAttacks(threadData, pieces[NIGHT] & nonPinned, cb.pieceIndexes, enemies);
		addBishopAttacks(threadData, pieces[BISHOP] & nonPinned, cb, enemies);
		addRookAttacks(threadData, pieces[ROOK] & nonPinned, cb, enemies);
		addQueenAttacks(threadData, pieces[QUEEN] & nonPinned, cb, enemies);
		addKingAttacks(threadData, cb);

		// pinned pieces
		long piece = cb.pieces[cb.colorToMove][ALL] & cb.pinnedPieces;
		while (piece != 0) {
			switch (cb.pieceIndexes[Long.numberOfTrailingZeros(piece)]) {
			case PAWN:
				addPawnAttacksAndPromotions(threadData, Long.lowestOneBit(piece), cb,
						enemies & ChessConstants.PINNED_MOVEMENT[Long.numberOfTrailingZeros(piece)][cb.kingIndex[cb.colorToMove]], 0);
				break;
			case BISHOP:
				addBishopAttacks(threadData, Long.lowestOneBit(piece), cb,
						enemies & ChessConstants.PINNED_MOVEMENT[Long.numberOfTrailingZeros(piece)][cb.kingIndex[cb.colorToMove]]);
				break;
			case ROOK:
				addRookAttacks(threadData, Long.lowestOneBit(piece), cb,
						enemies & ChessConstants.PINNED_MOVEMENT[Long.numberOfTrailingZeros(piece)][cb.kingIndex[cb.colorToMove]]);
				break;
			case QUEEN:
				addQueenAttacks(threadData, Long.lowestOneBit(piece), cb,
						enemies & ChessConstants.PINNED_MOVEMENT[Long.numberOfTrailingZeros(piece)][cb.kingIndex[cb.colorToMove]]);
			}
			piece &= piece - 1;
		}

	}

	private static void generateOutOfCheckAttacks(final ThreadData threadData, final ChessBoard cb) {
		// attack attacker
		final long nonPinned = ~cb.pinnedPieces;
		final long[] pieces = cb.pieces[cb.colorToMove];
		addEpAttacks(threadData, cb);
		addPawnAttacksAndPromotions(threadData, pieces[PAWN] & nonPinned, cb, cb.checkingPieces,
				ChessConstants.IN_BETWEEN[cb.kingIndex[cb.colorToMove]][Long.numberOfTrailingZeros(cb.checkingPieces)]);
		addNightAttacks(threadData, pieces[NIGHT] & nonPinned, cb.pieceIndexes, cb.checkingPieces);
		addBishopAttacks(threadData, pieces[BISHOP] & nonPinned, cb, cb.checkingPieces);
		addRookAttacks(threadData, pieces[ROOK] & nonPinned, cb, cb.checkingPieces);
		addQueenAttacks(threadData, pieces[QUEEN] & nonPinned, cb, cb.checkingPieces);
		addKingAttacks(threadData, cb);
	}

	private static void addPawnAttacksAndPromotions(final ThreadData threadData, final long pawns, final ChessBoard cb, final long enemies,
			final long emptySpaces) {

		if (pawns == 0) {
			return;
		}

		if (cb.colorToMove == WHITE) {

			// non-promoting
			long piece = pawns & Bitboard.RANK_NON_PROMOTION[WHITE] & Bitboard.getBlackPawnAttacks(enemies);
			while (piece != 0) {
				final int fromIndex = Long.numberOfTrailingZeros(piece);
				long moves = StaticMoves.PAWN_ATTACKS[WHITE][fromIndex] & enemies;
				while (moves != 0) {
					final int toIndex = Long.numberOfTrailingZeros(moves);
					threadData.addMove(MoveUtil.createAttackMove(fromIndex, toIndex, PAWN, cb.pieceIndexes[toIndex]));
					moves &= moves - 1;
				}
				piece &= piece - 1;
			}

			// promoting
			piece = pawns & Bitboard.RANK_7;
			while (piece != 0) {
				final int fromIndex = Long.numberOfTrailingZeros(piece);

				// promotion move
				if ((Long.lowestOneBit(piece) << 8 & emptySpaces) != 0) {
					addPromotionMove(threadData, fromIndex, fromIndex + 8);
				}

				// promotion attacks
				addPromotionAttacks(threadData, StaticMoves.PAWN_ATTACKS[WHITE][fromIndex] & enemies, fromIndex, cb.pieceIndexes);

				piece &= piece - 1;
			}
		} else {
			// non-promoting
			long piece = pawns & Bitboard.RANK_NON_PROMOTION[BLACK] & Bitboard.getWhitePawnAttacks(enemies);
			while (piece != 0) {
				final int fromIndex = Long.numberOfTrailingZeros(piece);
				long moves = StaticMoves.PAWN_ATTACKS[BLACK][fromIndex] & enemies;
				while (moves != 0) {
					final int toIndex = Long.numberOfTrailingZeros(moves);
					threadData.addMove(MoveUtil.createAttackMove(fromIndex, toIndex, PAWN, cb.pieceIndexes[toIndex]));
					moves &= moves - 1;
				}
				piece &= piece - 1;
			}

			// promoting
			piece = pawns & Bitboard.RANK_2;
			while (piece != 0) {
				final int fromIndex = Long.numberOfTrailingZeros(piece);

				// promotion move
				if ((Long.lowestOneBit(piece) >>> 8 & emptySpaces) != 0) {
					addPromotionMove(threadData, fromIndex, fromIndex - 8);
				}

				// promotion attacks
				addPromotionAttacks(threadData, StaticMoves.PAWN_ATTACKS[BLACK][fromIndex] & enemies, fromIndex, cb.pieceIndexes);

				piece &= piece - 1;
			}
		}
	}

	private static void addBishopAttacks(final ThreadData threadData, long piece, final ChessBoard cb, final long possiblePositions) {
		while (piece != 0) {
			final int fromIndex = Long.numberOfTrailingZeros(piece);
			long moves = MagicUtil.getBishopMoves(fromIndex, cb.allPieces) & possiblePositions;
			while (moves != 0) {
				final int toIndex = Long.numberOfTrailingZeros(moves);
				threadData.addMove(MoveUtil.createAttackMove(fromIndex, toIndex, BISHOP, cb.pieceIndexes[toIndex]));
				moves &= moves - 1;
			}
			piece &= piece - 1;
		}
	}

	private static void addRookAttacks(final ThreadData threadData, long piece, final ChessBoard cb, final long possiblePositions) {
		while (piece != 0) {
			final int fromIndex = Long.numberOfTrailingZeros(piece);
			long moves = MagicUtil.getRookMoves(fromIndex, cb.allPieces) & possiblePositions;
			while (moves != 0) {
				final int toIndex = Long.numberOfTrailingZeros(moves);
				threadData.addMove(MoveUtil.createAttackMove(fromIndex, toIndex, ROOK, cb.pieceIndexes[toIndex]));
				moves &= moves - 1;
			}
			piece &= piece - 1;
		}
	}

	private static void addQueenAttacks(final ThreadData threadData, long piece, final ChessBoard cb, final long possiblePositions) {
		while (piece != 0) {
			final int fromIndex = Long.numberOfTrailingZeros(piece);
			long moves = MagicUtil.getQueenMoves(fromIndex, cb.allPieces) & possiblePositions;
			while (moves != 0) {
				final int toIndex = Long.numberOfTrailingZeros(moves);
				threadData.addMove(MoveUtil.createAttackMove(fromIndex, toIndex, QUEEN, cb.pieceIndexes[toIndex]));
				moves &= moves - 1;
			}
			piece &= piece - 1;
		}
	}

	private static void addBishopMoves(final ThreadData threadData, long piece, final long allPieces, final long possiblePositions) {
		while (piece != 0) {
			final int fromIndex = Long.numberOfTrailingZeros(piece);
			long moves = MagicUtil.getBishopMoves(fromIndex, allPieces) & possiblePositions;
			while (moves != 0) {
				threadData.addMove(MoveUtil.createMove(fromIndex, Long.numberOfTrailingZeros(moves), BISHOP));
				moves &= moves - 1;
			}

			piece &= piece - 1;
		}
	}

	private static void addQueenMoves(final ThreadData threadData, long piece, final long allPieces, final long possiblePositions) {
		while (piece != 0) {
			final int fromIndex = Long.numberOfTrailingZeros(piece);
			long moves = MagicUtil.getQueenMoves(fromIndex, allPieces) & possiblePositions;
			while (moves != 0) {
				threadData.addMove(MoveUtil.createMove(fromIndex, Long.numberOfTrailingZeros(moves), QUEEN));
				moves &= moves - 1;
			}

			piece &= piece - 1;
		}
	}

	private static void addRookMoves(final ThreadData threadData, long piece, final long allPieces, final long possiblePositions) {
		while (piece != 0) {
			final int fromIndex = Long.numberOfTrailingZeros(piece);
			long moves = MagicUtil.getRookMoves(fromIndex, allPieces) & possiblePositions;
			while (moves != 0) {
				threadData.addMove(MoveUtil.createMove(fromIndex, Long.numberOfTrailingZeros(moves), ROOK));
				moves &= moves - 1;
			}
			piece &= piece - 1;
		}
	}

	private static void addNightMoves(final ThreadData threadData, long piece, final long possiblePositions) {
		while (piece != 0) {
			final int fromIndex = Long.numberOfTrailingZeros(piece);
			long moves = StaticMoves.KNIGHT_MOVES[fromIndex] & possiblePositions;
			while (moves != 0) {
				threadData.addMove(MoveUtil.createMove(fromIndex, Long.numberOfTrailingZeros(moves), NIGHT));
				moves &= moves - 1;
			}
			piece &= piece - 1;
		}
	}

	private static void addPawnMoves(final ThreadData threadData, final long pawns, final ChessBoard cb, final long possiblePositions) {

		if (pawns == 0) {
			return;
		}

		if (cb.colorToMove == WHITE) {
			// 1-move
			long piece = pawns & (possiblePositions >>> 8) & Bitboard.RANK_23456;
			while (piece != 0) {
				threadData.addMove(MoveUtil.createWhitePawnMove(Long.numberOfTrailingZeros(piece)));
				piece &= piece - 1;
			}
			// 2-move
			piece = pawns & (possiblePositions >>> 16) & Bitboard.RANK_2;
			while (piece != 0) {
				if ((cb.emptySpaces & (Long.lowestOneBit(piece) << 8)) != 0) {
					threadData.addMove(MoveUtil.createWhitePawn2Move(Long.numberOfTrailingZeros(piece)));
				}
				piece &= piece - 1;
			}
		} else {
			// 1-move
			long piece = pawns & (possiblePositions << 8) & Bitboard.RANK_34567;
			while (piece != 0) {
				threadData.addMove(MoveUtil.createBlackPawnMove(Long.numberOfTrailingZeros(piece)));
				piece &= piece - 1;
			}
			// 2-move
			piece = pawns & (possiblePositions << 16) & Bitboard.RANK_7;
			while (piece != 0) {
				if ((cb.emptySpaces & (Long.lowestOneBit(piece) >>> 8)) != 0) {
					threadData.addMove(MoveUtil.createBlackPawn2Move(Long.numberOfTrailingZeros(piece)));
				}
				piece &= piece - 1;
			}
		}
	}

	private static void addKingMoves(final ThreadData threadData, final ChessBoard cb) {
		final int fromIndex = cb.kingIndex[cb.colorToMove];
		long moves = StaticMoves.KING_MOVES[fromIndex] & cb.emptySpaces;
		while (moves != 0) {
			threadData.addMove(MoveUtil.createMove(fromIndex, Long.numberOfTrailingZeros(moves), KING));
			moves &= moves - 1;
		}

		// castling
		if (cb.checkingPieces == 0) {
			long castlingIndexes = CastlingUtil.getCastlingIndexes(cb);
			while (castlingIndexes != 0) {
				final int castlingIndex = Long.numberOfTrailingZeros(castlingIndexes);
				// no piece in between?
				if (CastlingUtil.isValidCastlingMove(cb, fromIndex, castlingIndex)) {
					threadData.addMove(MoveUtil.createCastlingMove(fromIndex, castlingIndex));
				}
				castlingIndexes &= castlingIndexes - 1;
			}
		}
	}

	private static void addKingAttacks(final ThreadData threadData, final ChessBoard cb) {
		final int fromIndex = cb.kingIndex[cb.colorToMove];
		long moves = StaticMoves.KING_MOVES[fromIndex] & cb.pieces[cb.colorToMoveInverse][ALL] & ~cb.discoveredPieces;
		while (moves != 0) {
			final int toIndex = Long.numberOfTrailingZeros(moves);
			threadData.addMove(MoveUtil.createAttackMove(fromIndex, toIndex, KING, cb.pieceIndexes[toIndex]));
			moves &= moves - 1;
		}
	}

	private static void addNightAttacks(final ThreadData threadData, long piece, final int[] pieceIndexes, final long possiblePositions) {
		while (piece != 0) {
			final int fromIndex = Long.numberOfTrailingZeros(piece);
			long moves = StaticMoves.KNIGHT_MOVES[fromIndex] & possiblePositions;
			while (moves != 0) {
				final int toIndex = Long.numberOfTrailingZeros(moves);
				threadData.addMove(MoveUtil.createAttackMove(fromIndex, toIndex, NIGHT, pieceIndexes[toIndex]));
				moves &= moves - 1;
			}
			piece &= piece - 1;
		}
	}

	private static void addEpAttacks(final ThreadData threadData, final ChessBoard cb) {
		if (cb.epIndex == 0) {
			return;
		}
		long piece = cb.pieces[cb.colorToMove][PAWN] & StaticMoves.PAWN_ATTACKS[cb.colorToMoveInverse][cb.epIndex];
		while (piece != 0) {
			if (cb.isLegalEPMove(Long.numberOfTrailingZeros(piece))) {
				threadData.addMove(MoveUtil.createEPMove(Long.numberOfTrailingZeros(piece), cb.epIndex));
			}
			piece &= piece - 1;
		}
	}

	private static void addPromotionMove(final ThreadData threadData, final int fromIndex, final int toIndex) {
		threadData.addMove(MoveUtil.createPromotionMove(MoveUtil.TYPE_PROMOTION_Q, fromIndex, toIndex));
		threadData.addMove(MoveUtil.createPromotionMove(MoveUtil.TYPE_PROMOTION_N, fromIndex, toIndex));
		if (EngineConstants.GENERATE_BR_PROMOTIONS) {
			threadData.addMove(MoveUtil.createPromotionMove(MoveUtil.TYPE_PROMOTION_B, fromIndex, toIndex));
			threadData.addMove(MoveUtil.createPromotionMove(MoveUtil.TYPE_PROMOTION_R, fromIndex, toIndex));
		}
	}

	private static void addPromotionAttacks(final ThreadData threadData, long moves, final int fromIndex, final int[] pieceIndexes) {
		while (moves != 0) {
			final int toIndex = Long.numberOfTrailingZeros(moves);
			threadData.addMove(MoveUtil.createPromotionAttack(MoveUtil.TYPE_PROMOTION_Q, fromIndex, toIndex, pieceIndexes[toIndex]));
			threadData.addMove(MoveUtil.createPromotionAttack(MoveUtil.TYPE_PROMOTION_N, fromIndex, toIndex, pieceIndexes[toIndex]));
			if (EngineConstants.GENERATE_BR_PROMOTIONS) {
				threadData.addMove(MoveUtil.createPromotionAttack(MoveUtil.TYPE_PROMOTION_B, fromIndex, toIndex, pieceIndexes[toIndex]));
				threadData.addMove(MoveUtil.createPromotionAttack(MoveUtil.TYPE_PROMOTION_R, fromIndex, toIndex, pieceIndexes[toIndex]));
			}
			moves &= moves - 1;
		}
	}

}
