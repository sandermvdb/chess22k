package nl.s22k.chess.move;

import static nl.s22k.chess.ChessConstants.BISHOP;
import static nl.s22k.chess.ChessConstants.BLACK;
import static nl.s22k.chess.ChessConstants.KING;
import static nl.s22k.chess.ChessConstants.NIGHT;
import static nl.s22k.chess.ChessConstants.PAWN;
import static nl.s22k.chess.ChessConstants.QUEEN;
import static nl.s22k.chess.ChessConstants.ROOK;
import static nl.s22k.chess.ChessConstants.WHITE;

import java.util.Arrays;

import nl.s22k.chess.Assert;
import nl.s22k.chess.Bitboard;
import nl.s22k.chess.CastlingUtil;
import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.ChessConstants;
import nl.s22k.chess.engine.EngineConstants;
import nl.s22k.chess.engine.MainEngine;

public final class MoveGenerator {

	private static MoveGenerator[] instances;
	static {
		initInstances(MainEngine.nrOfThreads);
	}

	private final int[] moves = new int[1500];
	private final int[] moveScores = new int[1500];
	private final int[] nextToGenerate = new int[EngineConstants.MAX_PLIES * 2];
	private final int[] nextToMove = new int[EngineConstants.MAX_PLIES * 2];
	private int currentPly;

	private final int[][][] COUNTER_MOVES = new int[2][7][64];

	private final int[] KILLER_MOVE_1 = new int[EngineConstants.MAX_PLIES * 2];
	private final int[] KILLER_MOVE_2 = new int[EngineConstants.MAX_PLIES * 2];

	private final int[][] HH_MOVES = new int[2][64 * 64];
	private final int[][] BF_MOVES = new int[2][64 * 64];

	public static MoveGenerator getInstance(int instanceNumber) {
		return instances[instanceNumber];
	}

	public static void initInstances(int nrOfInstances) {
		instances = new MoveGenerator[nrOfInstances];
		for (int i = 0; i < instances.length; i++) {
			instances[i] = new MoveGenerator();
		}
	}

	public MoveGenerator() {
		clearHistoryHeuristics();
	}

	public void clearHistoryHeuristics() {
		Arrays.fill(HH_MOVES[WHITE], 1);
		Arrays.fill(HH_MOVES[BLACK], 1);
		Arrays.fill(BF_MOVES[WHITE], 1);
		Arrays.fill(BF_MOVES[BLACK], 1);
	}

	public void addHHValue(final int color, final int move, final int depth) {
		HH_MOVES[color][MoveUtil.getFromToIndex(move)] += depth * depth;
		if (EngineConstants.ASSERT) {
			Assert.isTrue(HH_MOVES[color][MoveUtil.getFromToIndex(move)] >= 0);
		}
	}

	public void addBFValue(final int color, final int move, final int depth) {
		BF_MOVES[color][MoveUtil.getFromToIndex(move)] += depth * depth;
		if (EngineConstants.ASSERT) {
			Assert.isTrue(BF_MOVES[color][MoveUtil.getFromToIndex(move)] >= 0);
		}
	}

	public int getHHScore(final int color, final int fromToIndex) {
		if (!EngineConstants.ENABLE_HISTORY_HEURISTIC) {
			return 1;
		}
		return 100 * HH_MOVES[color][fromToIndex] / BF_MOVES[color][fromToIndex];
	}

	public void addKillerMove(final int move, final int ply) {
		if (EngineConstants.ENABLE_KILLER_MOVES) {
			if (KILLER_MOVE_1[ply] != move) {
				KILLER_MOVE_2[ply] = KILLER_MOVE_1[ply];
				KILLER_MOVE_1[ply] = move;
			}
		}
	}

	public void addCounterMove(final int color, final int parentMove, final int counterMove) {
		if (EngineConstants.ENABLE_COUNTER_MOVES) {
			COUNTER_MOVES[color][MoveUtil.getSourcePieceIndex(parentMove)][MoveUtil.getToIndex(parentMove)] = counterMove;
		}
	}

	public int getCounter(final int color, final int parentMove) {
		return COUNTER_MOVES[color][MoveUtil.getSourcePieceIndex(parentMove)][MoveUtil.getToIndex(parentMove)];
	}

	public int getKiller1(final int ply) {
		return KILLER_MOVE_1[ply];
	}

	public int getKiller2(final int ply) {
		return KILLER_MOVE_2[ply];
	}

	public void startPly() {
		nextToGenerate[currentPly + 1] = nextToGenerate[currentPly];
		nextToMove[currentPly + 1] = nextToGenerate[currentPly];
		currentPly++;
	}

	public void endPly() {
		currentPly--;
	}

	public int next() {
		return moves[nextToMove[currentPly]++];
	}

	public int getScore() {
		return moveScores[nextToMove[currentPly] - 1];
	}

	public int previous() {
		return moves[nextToMove[currentPly] - 1];
	}

	public boolean hasNext() {
		return nextToGenerate[currentPly] != nextToMove[currentPly];
	}

	public void addMove(final int move) {
		moves[nextToGenerate[currentPly]++] = move;
	}

	public void setMVVLVAScores() {
		for (int j = nextToMove[currentPly]; j < nextToGenerate[currentPly]; j++) {
			moveScores[j] = MoveUtil.getAttackedPieceIndex(moves[j]) * 6 - MoveUtil.getSourcePieceIndex(moves[j]);
		}
	}

	public void setHHScores(final int colorToMove) {
		for (int j = nextToMove[currentPly]; j < nextToGenerate[currentPly]; j++) {
			moveScores[j] = getHHScore(colorToMove, MoveUtil.getFromToIndex(moves[j]));
		}
	}

	public void sort() {
		final int left = nextToMove[currentPly];
		for (int i = left, j = i; i < nextToGenerate[currentPly] - 1; j = ++i) {
			final int score = moveScores[i + 1];
			final int move = moves[i + 1];
			while (score > moveScores[j]) {
				moveScores[j + 1] = moveScores[j];
				moves[j + 1] = moves[j];
				if (j-- == left) {
					break;
				}
			}
			moveScores[j + 1] = score;
			moves[j + 1] = move;
		}
	}

	public String getMovesAsString() {
		StringBuilder sb = new StringBuilder();
		for (int j = nextToMove[currentPly]; j < nextToGenerate[currentPly]; j++) {
			sb.append(new MoveWrapper(moves[j]) + ", ");
		}
		return sb.toString();
	}

	public void generateMoves(final ChessBoard cb) {
		if (cb.checkingPieces == 0) {
			generateNotInCheckMoves(cb);
		} else if (Long.bitCount(cb.checkingPieces) == 1) {
			if (cb.pieceIndexes[Long.numberOfTrailingZeros(cb.checkingPieces)] <= NIGHT) {
				addKingMoves(cb);
			} else {
				generateOutOfSlidingCheckMoves(cb);
			}
		} else {
			// double check, only the king can move
			addKingMoves(cb);
		}
	}

	public void generateAttacks(final ChessBoard cb) {
		if (cb.checkingPieces == 0) {
			generateNotInCheckAttacks(cb);
		} else if (Long.bitCount(cb.checkingPieces) == 1) {
			generateOutOfCheckAttacks(cb);
		} else {
			// double check, only the king can attack
			addKingAttacks(cb);
		}
	}

	private void generateNotInCheckMoves(final ChessBoard cb) {

		// non pinned pieces
		final long nonPinned = ~cb.pinnedPieces;
		final long[] pieces = cb.pieces[cb.colorToMove];
		addNightMoves(pieces[NIGHT] & nonPinned, cb.emptySpaces);
		addBishopMoves(pieces[BISHOP] & nonPinned, cb.allPieces, cb.emptySpaces);
		addRookMoves(pieces[ROOK] & nonPinned, cb.allPieces, cb.emptySpaces);
		addQueenMoves(pieces[QUEEN] & nonPinned, cb.allPieces, cb.emptySpaces);
		addPawnMoves(pieces[PAWN] & nonPinned, cb, cb.emptySpaces);
		addKingMoves(cb);

		// pinned pieces
		long piece = cb.friendlyPieces[cb.colorToMove] & cb.pinnedPieces;
		while (piece != 0) {
			switch (cb.pieceIndexes[Long.numberOfTrailingZeros(piece)]) {
			case PAWN:
				addPawnMoves(Long.lowestOneBit(piece), cb,
						cb.emptySpaces & ChessConstants.PINNED_MOVEMENT[Long.numberOfTrailingZeros(piece)][cb.kingIndex[cb.colorToMove]]);
				break;
			case BISHOP:
				addBishopMoves(Long.lowestOneBit(piece), cb.allPieces,
						cb.emptySpaces & ChessConstants.PINNED_MOVEMENT[Long.numberOfTrailingZeros(piece)][cb.kingIndex[cb.colorToMove]]);
				break;
			case ROOK:
				addRookMoves(Long.lowestOneBit(piece), cb.allPieces,
						cb.emptySpaces & ChessConstants.PINNED_MOVEMENT[Long.numberOfTrailingZeros(piece)][cb.kingIndex[cb.colorToMove]]);
				break;
			case QUEEN:
				addQueenMoves(Long.lowestOneBit(piece), cb.allPieces,
						cb.emptySpaces & ChessConstants.PINNED_MOVEMENT[Long.numberOfTrailingZeros(piece)][cb.kingIndex[cb.colorToMove]]);
			}
			piece &= piece - 1;
		}

	}

	private void generateOutOfSlidingCheckMoves(final ChessBoard cb) {

		// TODO when check is blocked -> pinned piece

		// move king or block sliding piece
		final long inBetween = ChessConstants.IN_BETWEEN[cb.kingIndex[cb.colorToMove]][Long.numberOfTrailingZeros(cb.checkingPieces)];
		if (inBetween != 0) {
			final long nonPinned = ~cb.pinnedPieces;
			final long[] pieces = cb.pieces[cb.colorToMove];
			addPawnMoves(pieces[PAWN] & nonPinned, cb, inBetween);
			addNightMoves(pieces[NIGHT] & nonPinned, inBetween);
			addBishopMoves(pieces[BISHOP] & nonPinned, cb.allPieces, inBetween);
			addRookMoves(pieces[ROOK] & nonPinned, cb.allPieces, inBetween);
			addQueenMoves(pieces[QUEEN] & nonPinned, cb.allPieces, inBetween);
		}

		addKingMoves(cb);
	}

	private void generateNotInCheckAttacks(final ChessBoard cb) {

		// non pinned pieces
		addEpAttacks(cb);
		final long nonPinned = ~cb.pinnedPieces;
		final long enemies = cb.friendlyPieces[cb.colorToMoveInverse];
		final long[] pieces = cb.pieces[cb.colorToMove];
		addPawnAttacksAndPromotions(pieces[PAWN] & nonPinned, cb, enemies, cb.emptySpaces);
		addNightAttacks(pieces[NIGHT] & nonPinned, cb.pieceIndexes, enemies);
		addBishopAttacks(pieces[BISHOP] & nonPinned, cb, enemies);
		addRookAttacks(pieces[ROOK] & nonPinned, cb, enemies);
		addQueenAttacks(pieces[QUEEN] & nonPinned, cb, enemies);
		addKingAttacks(cb);

		// pinned pieces
		long piece = cb.friendlyPieces[cb.colorToMove] & cb.pinnedPieces;
		while (piece != 0) {
			switch (cb.pieceIndexes[Long.numberOfTrailingZeros(piece)]) {
			case PAWN:
				addPawnAttacksAndPromotions(Long.lowestOneBit(piece), cb,
						enemies & ChessConstants.PINNED_MOVEMENT[Long.numberOfTrailingZeros(piece)][cb.kingIndex[cb.colorToMove]], 0);
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

	private void generateOutOfCheckAttacks(final ChessBoard cb) {
		// attack attacker
		final long nonPinned = ~cb.pinnedPieces;
		final long[] pieces = cb.pieces[cb.colorToMove];
		addEpAttacks(cb);
		addPawnAttacksAndPromotions(pieces[PAWN] & nonPinned, cb, cb.checkingPieces,
				ChessConstants.IN_BETWEEN[cb.kingIndex[cb.colorToMove]][Long.numberOfTrailingZeros(cb.checkingPieces)]);
		addNightAttacks(pieces[NIGHT] & nonPinned, cb.pieceIndexes, cb.checkingPieces);
		addBishopAttacks(pieces[BISHOP] & nonPinned, cb, cb.checkingPieces);
		addRookAttacks(pieces[ROOK] & nonPinned, cb, cb.checkingPieces);
		addQueenAttacks(pieces[QUEEN] & nonPinned, cb, cb.checkingPieces);
		addKingAttacks(cb);
	}

	private void addPawnAttacksAndPromotions(final long pawns, final ChessBoard cb, final long enemies, final long emptySpaces) {

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
					addMove(MoveUtil.createAttackMove(fromIndex, toIndex, PAWN, cb.pieceIndexes[toIndex]));
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
					addPromotionMove(fromIndex, fromIndex + 8);
				}

				// promotion attacks
				addPromotionAttacks(StaticMoves.PAWN_ATTACKS[WHITE][fromIndex] & enemies, fromIndex, cb.pieceIndexes);

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
					addMove(MoveUtil.createAttackMove(fromIndex, toIndex, PAWN, cb.pieceIndexes[toIndex]));
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
					addPromotionMove(fromIndex, fromIndex - 8);
				}

				// promotion attacks
				addPromotionAttacks(StaticMoves.PAWN_ATTACKS[BLACK][fromIndex] & enemies, fromIndex, cb.pieceIndexes);

				piece &= piece - 1;
			}
		}
	}

	private void addBishopAttacks(long piece, final ChessBoard cb, final long possiblePositions) {
		while (piece != 0) {
			final int fromIndex = Long.numberOfTrailingZeros(piece);
			long moves = MagicUtil.getBishopMoves(fromIndex, cb.allPieces) & possiblePositions;
			while (moves != 0) {
				final int toIndex = Long.numberOfTrailingZeros(moves);
				addMove(MoveUtil.createAttackMove(fromIndex, toIndex, BISHOP, cb.pieceIndexes[toIndex]));
				moves &= moves - 1;
			}
			piece &= piece - 1;
		}
	}

	private void addRookAttacks(long piece, final ChessBoard cb, final long possiblePositions) {
		while (piece != 0) {
			final int fromIndex = Long.numberOfTrailingZeros(piece);
			long moves = MagicUtil.getRookMoves(fromIndex, cb.allPieces) & possiblePositions;
			while (moves != 0) {
				final int toIndex = Long.numberOfTrailingZeros(moves);
				addMove(MoveUtil.createAttackMove(fromIndex, toIndex, ROOK, cb.pieceIndexes[toIndex]));
				moves &= moves - 1;
			}
			piece &= piece - 1;
		}
	}

	private void addQueenAttacks(long piece, final ChessBoard cb, final long possiblePositions) {
		while (piece != 0) {
			final int fromIndex = Long.numberOfTrailingZeros(piece);
			long moves = MagicUtil.getQueenMoves(fromIndex, cb.allPieces) & possiblePositions;
			while (moves != 0) {
				final int toIndex = Long.numberOfTrailingZeros(moves);
				addMove(MoveUtil.createAttackMove(fromIndex, toIndex, QUEEN, cb.pieceIndexes[toIndex]));
				moves &= moves - 1;
			}
			piece &= piece - 1;
		}
	}

	private void addBishopMoves(long piece, final long allPieces, final long possiblePositions) {
		while (piece != 0) {
			final int fromIndex = Long.numberOfTrailingZeros(piece);
			long moves = MagicUtil.getBishopMoves(fromIndex, allPieces) & possiblePositions;
			while (moves != 0) {
				addMove(MoveUtil.createMove(fromIndex, Long.numberOfTrailingZeros(moves), BISHOP));
				moves &= moves - 1;
			}

			piece &= piece - 1;
		}
	}

	private void addQueenMoves(long piece, final long allPieces, final long possiblePositions) {
		while (piece != 0) {
			final int fromIndex = Long.numberOfTrailingZeros(piece);
			long moves = MagicUtil.getQueenMoves(fromIndex, allPieces) & possiblePositions;
			while (moves != 0) {
				addMove(MoveUtil.createMove(fromIndex, Long.numberOfTrailingZeros(moves), QUEEN));
				moves &= moves - 1;
			}

			piece &= piece - 1;
		}
	}

	private void addRookMoves(long piece, final long allPieces, final long possiblePositions) {
		while (piece != 0) {
			final int fromIndex = Long.numberOfTrailingZeros(piece);
			long moves = MagicUtil.getRookMoves(fromIndex, allPieces) & possiblePositions;
			while (moves != 0) {
				addMove(MoveUtil.createMove(fromIndex, Long.numberOfTrailingZeros(moves), ROOK));
				moves &= moves - 1;
			}
			piece &= piece - 1;
		}
	}

	private void addNightMoves(long piece, final long possiblePositions) {
		while (piece != 0) {
			final int fromIndex = Long.numberOfTrailingZeros(piece);
			long moves = StaticMoves.KNIGHT_MOVES[fromIndex] & possiblePositions;
			while (moves != 0) {
				addMove(MoveUtil.createMove(fromIndex, Long.numberOfTrailingZeros(moves), NIGHT));
				moves &= moves - 1;
			}
			piece &= piece - 1;
		}
	}

	private void addPawnMoves(final long pawns, final ChessBoard cb, final long possiblePositions) {

		if (pawns == 0) {
			return;
		}

		if (cb.colorToMove == WHITE) {
			// 1-move
			long piece = pawns & (possiblePositions >>> 8) & Bitboard.RANK_23456;
			while (piece != 0) {
				addMove(MoveUtil.createWhitePawnMove(Long.numberOfTrailingZeros(piece)));
				piece &= piece - 1;
			}
			// 2-move
			piece = pawns & (possiblePositions >>> 16) & Bitboard.RANK_2;
			while (piece != 0) {
				if ((cb.emptySpaces & (Long.lowestOneBit(piece) << 8)) != 0) {
					addMove(MoveUtil.createWhitePawn2Move(Long.numberOfTrailingZeros(piece)));
				}
				piece &= piece - 1;
			}
		} else {
			// 1-move
			long piece = pawns & (possiblePositions << 8) & Bitboard.RANK_34567;
			while (piece != 0) {
				addMove(MoveUtil.createBlackPawnMove(Long.numberOfTrailingZeros(piece)));
				piece &= piece - 1;
			}
			// 2-move
			piece = pawns & (possiblePositions << 16) & Bitboard.RANK_7;
			while (piece != 0) {
				if ((cb.emptySpaces & (Long.lowestOneBit(piece) >>> 8)) != 0) {
					addMove(MoveUtil.createBlackPawn2Move(Long.numberOfTrailingZeros(piece)));
				}
				piece &= piece - 1;
			}
		}
	}

	private void addKingMoves(final ChessBoard cb) {
		final int fromIndex = cb.kingIndex[cb.colorToMove];
		long moves = StaticMoves.KING_MOVES[fromIndex] & cb.emptySpaces;
		while (moves != 0) {
			addMove(MoveUtil.createMove(fromIndex, Long.numberOfTrailingZeros(moves), KING));
			moves &= moves - 1;
		}

		// castling
		if (cb.checkingPieces == 0) {
			long castlingIndexes = CastlingUtil.getCastlingIndexes(cb);
			while (castlingIndexes != 0) {
				final int castlingIndex = Long.numberOfTrailingZeros(castlingIndexes);
				// no piece in between?
				if (CastlingUtil.isValidCastlingMove(cb, fromIndex, castlingIndex)) {
					addMove(MoveUtil.createCastlingMove(fromIndex, castlingIndex));
				}
				castlingIndexes &= castlingIndexes - 1;
			}
		}
	}

	private void addKingAttacks(final ChessBoard cb) {
		final int fromIndex = cb.kingIndex[cb.colorToMove];
		long moves = StaticMoves.KING_MOVES[fromIndex] & cb.friendlyPieces[cb.colorToMoveInverse];
		while (moves != 0) {
			final int toIndex = Long.numberOfTrailingZeros(moves);
			addMove(MoveUtil.createAttackMove(fromIndex, toIndex, KING, cb.pieceIndexes[toIndex]));
			moves &= moves - 1;
		}
	}

	private void addNightAttacks(long piece, final int[] pieceIndexes, final long possiblePositions) {
		while (piece != 0) {
			final int fromIndex = Long.numberOfTrailingZeros(piece);
			long moves = StaticMoves.KNIGHT_MOVES[fromIndex] & possiblePositions;
			while (moves != 0) {
				final int toIndex = Long.numberOfTrailingZeros(moves);
				addMove(MoveUtil.createAttackMove(fromIndex, toIndex, NIGHT, pieceIndexes[toIndex]));
				moves &= moves - 1;
			}
			piece &= piece - 1;
		}
	}

	private void addEpAttacks(final ChessBoard cb) {
		if (cb.epIndex == 0) {
			return;
		}
		long piece = cb.pieces[cb.colorToMove][PAWN] & StaticMoves.PAWN_ATTACKS[cb.colorToMoveInverse][cb.epIndex];
		while (piece != 0) {
			if (cb.isLegalEPMove(Long.numberOfTrailingZeros(piece))) {
				addMove(MoveUtil.createEPMove(Long.numberOfTrailingZeros(piece), cb.epIndex));
			}
			piece &= piece - 1;
		}
	}

	private void addPromotionMove(final int fromIndex, final int toIndex) {
		addMove(MoveUtil.createPromotionMove(MoveUtil.TYPE_PROMOTION_Q, fromIndex, toIndex));
		addMove(MoveUtil.createPromotionMove(MoveUtil.TYPE_PROMOTION_N, fromIndex, toIndex));
		if (EngineConstants.GENERATE_BR_PROMOTIONS) {
			addMove(MoveUtil.createPromotionMove(MoveUtil.TYPE_PROMOTION_B, fromIndex, toIndex));
			addMove(MoveUtil.createPromotionMove(MoveUtil.TYPE_PROMOTION_R, fromIndex, toIndex));
		}
	}

	private void addPromotionAttacks(long moves, final int fromIndex, final int[] pieceIndexes) {
		while (moves != 0) {
			final int toIndex = Long.numberOfTrailingZeros(moves);
			addMove(MoveUtil.createPromotionAttack(MoveUtil.TYPE_PROMOTION_Q, fromIndex, toIndex, pieceIndexes[toIndex]));
			addMove(MoveUtil.createPromotionAttack(MoveUtil.TYPE_PROMOTION_N, fromIndex, toIndex, pieceIndexes[toIndex]));
			if (EngineConstants.GENERATE_BR_PROMOTIONS) {
				addMove(MoveUtil.createPromotionAttack(MoveUtil.TYPE_PROMOTION_B, fromIndex, toIndex, pieceIndexes[toIndex]));
				addMove(MoveUtil.createPromotionAttack(MoveUtil.TYPE_PROMOTION_R, fromIndex, toIndex, pieceIndexes[toIndex]));
			}
			moves &= moves - 1;
		}
	}

}
