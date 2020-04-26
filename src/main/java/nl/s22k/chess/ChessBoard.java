package nl.s22k.chess;

import static nl.s22k.chess.ChessConstants.ALL;
import static nl.s22k.chess.ChessConstants.BISHOP;
import static nl.s22k.chess.ChessConstants.BLACK;
import static nl.s22k.chess.ChessConstants.EMPTY;
import static nl.s22k.chess.ChessConstants.KING;
import static nl.s22k.chess.ChessConstants.NIGHT;
import static nl.s22k.chess.ChessConstants.PAWN;
import static nl.s22k.chess.ChessConstants.QUEEN;
import static nl.s22k.chess.ChessConstants.ROOK;
import static nl.s22k.chess.ChessConstants.WHITE;

import nl.s22k.chess.engine.EngineConstants;
import nl.s22k.chess.eval.EvalConstants;
import nl.s22k.chess.eval.MaterialUtil;
import nl.s22k.chess.eval.SchroderUtil;
import nl.s22k.chess.move.MagicUtil;
import nl.s22k.chess.move.MoveUtil;
import nl.s22k.chess.move.StaticMoves;

public final class ChessBoard {

	public int castlingRights;
	public int psqtScore;
	public int colorToMove, colorToMoveInverse;
	public int epIndex;
	public int materialKey;
	public int phase;
	public int moveCounter = 0;

	public long allPieces, emptySpaces;
	public long zobristKey, pawnZobristKey;
	public long checkingPieces, pinnedPieces, discoveredPieces;
	public long moveCount;
	public long passedPawnsAndOutposts;

	public final int[] kingIndex = new int[2];
	public final int[] kingAttackersFlag = new int[2];
	public final int[] pieceIndexes = new int[64];

	public final long[] doubleAttacks = new long[2];

	public final long[][] pieces = new long[2][7];
	public final long[][] attacks = new long[2][7];

	private final int[] castlingAndEpHistory = new int[EngineConstants.MAX_MOVES];
	public final long[] zobristKeyHistory = new long[EngineConstants.MAX_MOVES];

	@Override
	public String toString() {
		return ChessBoardUtil.toString(this);
	}

	public void changeSideToMove() {
		colorToMove = colorToMoveInverse;
		colorToMoveInverse = 1 - colorToMove;
	}

	public boolean isDiscoveredMove(final int fromIndex) {
		if (discoveredPieces == 0) {
			return false;
		}
		return (discoveredPieces & (1L << fromIndex)) != 0;
	}

	private void pushHistoryValues() {
		zobristKeyHistory[moveCounter] = zobristKey;
		castlingAndEpHistory[moveCounter] = castlingRights << 10 | epIndex;
		moveCounter++;
	}

	private void popHistoryValues() {
		moveCounter--;
		zobristKey = zobristKeyHistory[moveCounter];
		if (castlingAndEpHistory[moveCounter] == 0) {
			castlingRights = 0;
			epIndex = 0;
		} else {
			castlingRights = castlingAndEpHistory[moveCounter] >>> 10;
			epIndex = castlingAndEpHistory[moveCounter] & 255;
		}
	}

	public void doNullMove() {
		pushHistoryValues();

		zobristKey ^= Zobrist.sideToMove;
		if (epIndex != 0) {
			zobristKey ^= Zobrist.epIndex[epIndex];
			epIndex = 0;
		}
		changeSideToMove();

		if (EngineConstants.ASSERT) {
			ChessBoardTestUtil.testValues(this);
		}
	}

	public void undoNullMove() {
		popHistoryValues();
		changeSideToMove();

		if (EngineConstants.ASSERT) {
			ChessBoardTestUtil.testValues(this);
		}
	}

	public void doMove(final int move) {

		moveCount++;

		final int fromIndex = MoveUtil.getFromIndex(move);
		int toIndex = MoveUtil.getToIndex(move);
		long toMask = 1L << toIndex;
		final long fromToMask = (1L << fromIndex) ^ toMask;
		final int sourcePieceIndex = MoveUtil.getSourcePieceIndex(move);
		final int attackedPieceIndex = MoveUtil.getAttackedPieceIndex(move);

		if (EngineConstants.ASSERT) {
			Assert.isTrue(move != 0);
			Assert.isTrue(attackedPieceIndex != KING);
			Assert.isTrue(attackedPieceIndex == 0 || (Util.POWER_LOOKUP[toIndex] & pieces[colorToMove][ALL]) == 0);
			Assert.isTrue(isValidMove(move));
		}

		pushHistoryValues();

		zobristKey ^= Zobrist.piece[colorToMove][sourcePieceIndex][fromIndex] ^ Zobrist.piece[colorToMove][sourcePieceIndex][toIndex] ^ Zobrist.sideToMove;
		if (epIndex != 0) {
			zobristKey ^= Zobrist.epIndex[epIndex];
			epIndex = 0;
		}

		pieces[colorToMove][ALL] ^= fromToMask;
		pieces[colorToMove][sourcePieceIndex] ^= fromToMask;
		pieceIndexes[fromIndex] = EMPTY;
		pieceIndexes[toIndex] = sourcePieceIndex;
		psqtScore += EvalConstants.PSQT[sourcePieceIndex][colorToMove][toIndex] - EvalConstants.PSQT[sourcePieceIndex][colorToMove][fromIndex];

		switch (sourcePieceIndex) {
		case PAWN:
			pawnZobristKey ^= Zobrist.piece[colorToMove][PAWN][fromIndex];
			if (MoveUtil.isPromotion(move)) {
				phase -= EvalConstants.PHASE[MoveUtil.getMoveType(move)];
				materialKey += MaterialUtil.VALUES[colorToMove][MoveUtil.getMoveType(move)] - MaterialUtil.VALUES[colorToMove][PAWN];
				pieces[colorToMove][PAWN] ^= toMask;
				pieces[colorToMove][MoveUtil.getMoveType(move)] |= toMask;
				pieceIndexes[toIndex] = MoveUtil.getMoveType(move);
				zobristKey ^= Zobrist.piece[colorToMove][PAWN][toIndex] ^ Zobrist.piece[colorToMove][MoveUtil.getMoveType(move)][toIndex];
				psqtScore += EvalConstants.PSQT[MoveUtil.getMoveType(move)][colorToMove][toIndex] - EvalConstants.PSQT[PAWN][colorToMove][toIndex];
			} else {
				pawnZobristKey ^= Zobrist.piece[colorToMove][PAWN][toIndex];
				// 2-move
				if (ChessConstants.IN_BETWEEN[fromIndex][toIndex] != 0) {
					if ((StaticMoves.PAWN_ATTACKS[colorToMove][Long.numberOfTrailingZeros(ChessConstants.IN_BETWEEN[fromIndex][toIndex])]
							& pieces[colorToMoveInverse][PAWN]) != 0) {
						epIndex = Long.numberOfTrailingZeros(ChessConstants.IN_BETWEEN[fromIndex][toIndex]);
						zobristKey ^= Zobrist.epIndex[epIndex];
					}
				}
			}
			break;

		case ROOK:
			if (castlingRights != 0) {
				zobristKey ^= Zobrist.castling[castlingRights];
				castlingRights = CastlingUtil.getRookMovedOrAttackedCastlingRights(castlingRights, fromIndex);
				zobristKey ^= Zobrist.castling[castlingRights];
			}
			break;

		case KING:
			kingIndex[colorToMove] = toIndex;
			if (castlingRights != 0) {
				if (MoveUtil.isCastlingMove(move)) {
					CastlingUtil.castleRookUpdateKeyAndPsqt(this, toIndex);
				}
				zobristKey ^= Zobrist.castling[castlingRights];
				castlingRights = CastlingUtil.getKingMovedCastlingRights(castlingRights, fromIndex);
				zobristKey ^= Zobrist.castling[castlingRights];
			}
		}

		// piece hit?
		switch (attackedPieceIndex) {
		case EMPTY:
			break;
		case PAWN:
			if (MoveUtil.isEPMove(move)) {
				toIndex += ChessConstants.COLOR_FACTOR_8[colorToMoveInverse];
				toMask = Util.POWER_LOOKUP[toIndex];
				pieceIndexes[toIndex] = EMPTY;
			}
			pawnZobristKey ^= Zobrist.piece[colorToMoveInverse][PAWN][toIndex];
			psqtScore -= EvalConstants.PSQT[PAWN][colorToMoveInverse][toIndex];
			pieces[colorToMoveInverse][ALL] ^= toMask;
			pieces[colorToMoveInverse][PAWN] ^= toMask;
			zobristKey ^= Zobrist.piece[colorToMoveInverse][PAWN][toIndex];
			materialKey -= MaterialUtil.VALUES[colorToMoveInverse][PAWN];
			break;
		case ROOK:
			if (castlingRights != 0) {
				zobristKey ^= Zobrist.castling[castlingRights];
				castlingRights = CastlingUtil.getRookMovedOrAttackedCastlingRights(castlingRights, toIndex);
				zobristKey ^= Zobrist.castling[castlingRights];
			}
			// fall-through
		default:
			phase += EvalConstants.PHASE[attackedPieceIndex];
			psqtScore -= EvalConstants.PSQT[attackedPieceIndex][colorToMoveInverse][toIndex];
			pieces[colorToMoveInverse][ALL] ^= toMask;
			pieces[colorToMoveInverse][attackedPieceIndex] ^= toMask;
			zobristKey ^= Zobrist.piece[colorToMoveInverse][attackedPieceIndex][toIndex];
			materialKey -= MaterialUtil.VALUES[colorToMoveInverse][attackedPieceIndex];
		}

		allPieces = pieces[colorToMove][ALL] | pieces[colorToMoveInverse][ALL];
		emptySpaces = ~allPieces;
		changeSideToMove();
		setCheckingPinnedAndDiscoPieces();

		if (EngineConstants.ASSERT) {
			ChessBoardTestUtil.testValues(this);
		}

	}

	public void setCheckingPinnedAndDiscoPieces() {

		pinnedPieces = 0;
		discoveredPieces = 0;
		checkingPieces = pieces[colorToMoveInverse][NIGHT] & StaticMoves.KNIGHT_MOVES[kingIndex[colorToMove]]
				| pieces[colorToMoveInverse][PAWN] & StaticMoves.PAWN_ATTACKS[colorToMove][kingIndex[colorToMove]];

		for (int kingColor = WHITE; kingColor <= BLACK; kingColor++) {

			int enemyColor = 1 - kingColor;

			if (!MaterialUtil.hasSlidingPieces(materialKey, enemyColor)) {
				continue;
			}

			long enemyPiece = (pieces[enemyColor][BISHOP] | pieces[enemyColor][QUEEN]) & MagicUtil.getBishopMovesEmptyBoard(kingIndex[kingColor])
					| (pieces[enemyColor][ROOK] | pieces[enemyColor][QUEEN]) & MagicUtil.getRookMovesEmptyBoard(kingIndex[kingColor]);
			while (enemyPiece != 0) {
				final long checkedPiece = ChessConstants.IN_BETWEEN[kingIndex[kingColor]][Long.numberOfTrailingZeros(enemyPiece)] & allPieces;
				if (checkedPiece == 0) {
					checkingPieces |= Long.lowestOneBit(enemyPiece);
				} else if (Long.bitCount(checkedPiece) == 1) {
					pinnedPieces |= checkedPiece & pieces[kingColor][ALL];
					discoveredPieces |= checkedPiece & pieces[enemyColor][ALL];
				}
				enemyPiece &= enemyPiece - 1;
			}
		}
	}

	public void undoMove(final int move) {

		final int fromIndex = MoveUtil.getFromIndex(move);
		int toIndex = MoveUtil.getToIndex(move);
		long toMask = 1L << toIndex;
		final long fromToMask = (1L << fromIndex) ^ toMask;
		final int sourcePieceIndex = MoveUtil.getSourcePieceIndex(move);
		final int attackedPieceIndex = MoveUtil.getAttackedPieceIndex(move);

		popHistoryValues();

		// undo move
		pieces[colorToMoveInverse][ALL] ^= fromToMask;
		pieces[colorToMoveInverse][sourcePieceIndex] ^= fromToMask;
		pieceIndexes[fromIndex] = sourcePieceIndex;
		psqtScore += EvalConstants.PSQT[sourcePieceIndex][colorToMoveInverse][fromIndex] - EvalConstants.PSQT[sourcePieceIndex][colorToMoveInverse][toIndex];

		switch (sourcePieceIndex) {
		case EMPTY:
			// not necessary but provides a table-index
			break;
		case PAWN:
			pawnZobristKey ^= Zobrist.piece[colorToMoveInverse][PAWN][fromIndex];
			if (MoveUtil.isPromotion(move)) {
				phase += EvalConstants.PHASE[MoveUtil.getMoveType(move)];
				materialKey -= MaterialUtil.VALUES[colorToMoveInverse][MoveUtil.getMoveType(move)] - MaterialUtil.VALUES[colorToMoveInverse][PAWN];
				pieces[colorToMoveInverse][PAWN] ^= toMask;
				pieces[colorToMoveInverse][MoveUtil.getMoveType(move)] ^= toMask;
				psqtScore += EvalConstants.PSQT[PAWN][colorToMoveInverse][toIndex]
						- EvalConstants.PSQT[MoveUtil.getMoveType(move)][colorToMoveInverse][toIndex];
			} else {
				pawnZobristKey ^= Zobrist.piece[colorToMoveInverse][PAWN][toIndex];
			}
			break;
		case KING:
			if (MoveUtil.isCastlingMove(move)) {
				CastlingUtil.uncastleRookUpdatePsqt(this, toIndex);
			}
			kingIndex[colorToMoveInverse] = fromIndex;
		}

		// undo hit
		switch (attackedPieceIndex) {
		case EMPTY:
			break;
		case PAWN:
			if (MoveUtil.isEPMove(move)) {
				pieceIndexes[toIndex] = EMPTY;
				toIndex += ChessConstants.COLOR_FACTOR_8[colorToMove];
				toMask = Util.POWER_LOOKUP[toIndex];
			}
			pawnZobristKey ^= Zobrist.piece[colorToMove][PAWN][toIndex];
			// fall-through
		default:
			psqtScore += EvalConstants.PSQT[attackedPieceIndex][colorToMove][toIndex];
			phase -= EvalConstants.PHASE[attackedPieceIndex];
			materialKey += MaterialUtil.VALUES[colorToMove][attackedPieceIndex];
			pieces[colorToMove][ALL] |= toMask;
			pieces[colorToMove][attackedPieceIndex] |= toMask;
		}

		pieceIndexes[toIndex] = attackedPieceIndex;
		allPieces = pieces[colorToMove][ALL] | pieces[colorToMoveInverse][ALL];
		emptySpaces = ~allPieces;
		changeSideToMove();
		setCheckingPinnedAndDiscoPieces();

		if (EngineConstants.ASSERT) {
			ChessBoardTestUtil.testValues(this);
		}
	}

	public boolean isLegal(final int move) {
		if (MoveUtil.getSourcePieceIndex(move) == KING) {
			return isLegalKingMove(move);
		}
		return true;
	}

	private boolean isLegalKingMove(final int move) {
		return !CheckUtil.isInCheckIncludingKing(MoveUtil.getToIndex(move), colorToMove, pieces[colorToMoveInverse],
				allPieces ^ Util.POWER_LOOKUP[MoveUtil.getFromIndex(move)]);
	}

	private boolean isLegalNonKingMove(final int move) {
		return !CheckUtil.isInCheck(kingIndex[colorToMove], colorToMove, pieces[colorToMoveInverse],
				allPieces ^ Util.POWER_LOOKUP[MoveUtil.getFromIndex(move)] ^ Util.POWER_LOOKUP[MoveUtil.getToIndex(move)]);
	}

	public boolean isLegalEPMove(final int fromIndex) {

		if (epIndex == 0) {
			// required for tt-moves
			return false;
		}

		// do-move and hit
		pieces[colorToMoveInverse][PAWN] ^= Util.POWER_LOOKUP[epIndex + ChessConstants.COLOR_FACTOR_8[colorToMoveInverse]];

		// check if is in check
		final boolean isInCheck = CheckUtil.isInCheck(kingIndex[colorToMove], colorToMove, pieces[colorToMoveInverse],
				pieces[colorToMove][ALL] ^ Util.POWER_LOOKUP[fromIndex] ^ Util.POWER_LOOKUP[epIndex]
						| pieces[colorToMoveInverse][ALL] ^ Util.POWER_LOOKUP[epIndex + ChessConstants.COLOR_FACTOR_8[colorToMoveInverse]]);

		// undo-move and hit
		pieces[colorToMoveInverse][PAWN] |= Util.POWER_LOOKUP[epIndex + ChessConstants.COLOR_FACTOR_8[colorToMoveInverse]];

		return !isInCheck;
	}

	public boolean isValidMove(final int move) {

		// check piece at from square
		final int fromIndex = MoveUtil.getFromIndex(move);
		final long fromSquare = Util.POWER_LOOKUP[fromIndex];
		if ((pieces[colorToMove][MoveUtil.getSourcePieceIndex(move)] & fromSquare) == 0) {
			return false;
		}

		// check piece at to square
		final int toIndex = MoveUtil.getToIndex(move);
		final long toSquare = Util.POWER_LOOKUP[toIndex];
		final int attackedPieceIndex = MoveUtil.getAttackedPieceIndex(move);
		if (attackedPieceIndex == 0) {
			if (pieceIndexes[toIndex] != EMPTY) {
				return false;
			}
		} else {
			if ((pieces[colorToMoveInverse][attackedPieceIndex] & toSquare) == 0 && !MoveUtil.isEPMove(move)) {
				return false;
			}
		}

		// check if move is possible
		switch (MoveUtil.getSourcePieceIndex(move)) {
		case PAWN:
			if (MoveUtil.isEPMove(move)) {
				if (toIndex != epIndex) {
					return false;
				}
				return isLegalEPMove(fromIndex);
			} else {
				if (colorToMove == WHITE) {
					if (fromIndex > toIndex) {
						return false;
					}
					// 2-move
					if (toIndex - fromIndex == 16 && (allPieces & Util.POWER_LOOKUP[fromIndex + 8]) != 0) {
						return false;
					}
				} else {
					if (fromIndex < toIndex) {
						return false;
					}
					// 2-move
					if (fromIndex - toIndex == 16 && (allPieces & Util.POWER_LOOKUP[fromIndex - 8]) != 0) {
						return false;
					}
				}
			}
			break;
		case NIGHT:
			break;
		case BISHOP:
			// fall-through
		case ROOK:
			// fall-through
		case QUEEN:
			if ((ChessConstants.IN_BETWEEN[fromIndex][toIndex] & allPieces) != 0) {
				return false;
			}
			break;
		case KING:
			if (MoveUtil.isCastlingMove(move)) {
				long castlingIndexes = CastlingUtil.getCastlingIndexes(this);
				while (castlingIndexes != 0) {
					if (toIndex == Long.numberOfTrailingZeros(castlingIndexes)) {
						return CastlingUtil.isValidCastlingMove(this, fromIndex, toIndex);
					}
					castlingIndexes &= castlingIndexes - 1;
				}
				return false;
			}
			return isLegalKingMove(move);
		}

		if ((fromSquare & pinnedPieces) != 0) {
			if ((ChessConstants.PINNED_MOVEMENT[fromIndex][kingIndex[colorToMove]] & toSquare) == 0) {
				return false;
			}
		}

		if (checkingPieces != 0) {
			if (attackedPieceIndex == 0) {
				return isLegalNonKingMove(move);
			} else {
				if (Long.bitCount(checkingPieces) == 2) {
					return false;
				}
				return (toSquare & checkingPieces) != 0;
			}
		}

		return true;
	}

	public boolean isRepetition(final int move) {

		if (!EngineConstants.ENABLE_REPETITION_TABLE) {
			return false;
		}

		// if move was an attacking-move or pawn move, no repetition
		if (!MoveUtil.isQuiet(move) || MoveUtil.getSourcePieceIndex(move) == PAWN) {
			return false;
		}

		final int moveCountMin = Math.max(0, moveCounter - 50);
		for (int i = moveCounter - 2; i >= moveCountMin; i -= 2) {
			if (zobristKey == zobristKeyHistory[i]) {
				if (Statistics.ENABLED) {
					Statistics.repetitions++;
				}
				return true;
			}
		}
		return false;
	}

	public void clearEvalAttacks() {
		kingAttackersFlag[WHITE] = 0;
		kingAttackersFlag[BLACK] = 0;
		attacks[WHITE][ALL] = 0;
		attacks[WHITE][PAWN] = 0;
		attacks[WHITE][NIGHT] = 0;
		attacks[WHITE][BISHOP] = 0;
		attacks[WHITE][ROOK] = 0;
		attacks[WHITE][QUEEN] = 0;
		attacks[BLACK][ALL] = 0;
		attacks[BLACK][PAWN] = 0;
		attacks[BLACK][NIGHT] = 0;
		attacks[BLACK][BISHOP] = 0;
		attacks[BLACK][ROOK] = 0;
		attacks[BLACK][QUEEN] = 0;
		doubleAttacks[WHITE] = 0;
		doubleAttacks[BLACK] = 0;
	}

	public void updateAttacks(final long moves, final int piece, final int color, final long kingArea) {
		if ((moves & kingArea) != 0) {
			kingAttackersFlag[color] |= SchroderUtil.FLAGS[piece];
		}
		doubleAttacks[color] |= attacks[color][ALL] & moves;
		attacks[color][ALL] |= moves;
		attacks[color][piece] |= moves;
	}

	public void updatePawnAttacks(final long moves, final int color) {
		doubleAttacks[color] |= attacks[color][PAWN] & moves;
		attacks[color][PAWN] |= moves;
	}

	public void updatePawnAttacks(final int color, final long kingArea) {
		attacks[color][ALL] = attacks[color][PAWN];
		if ((attacks[color][PAWN] & kingArea) != 0) {
			kingAttackersFlag[color] |= SchroderUtil.FLAGS[PAWN];
		}
	}

}