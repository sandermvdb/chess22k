package nl.s22k.chess;

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
import nl.s22k.chess.engine.MainEngine;
import nl.s22k.chess.eval.EvalConstants;
import nl.s22k.chess.eval.MaterialUtil;
import nl.s22k.chess.eval.SchroderUtil;
import nl.s22k.chess.move.MagicUtil;
import nl.s22k.chess.move.MoveUtil;
import nl.s22k.chess.move.StaticMoves;

public final class ChessBoard {

	private ChessBoard() {

	}

	private static ChessBoard[] instances;
	static {
		if (EngineConstants.TEST_EVAL_VALUES) {
			initInstances(2);
		} else {
			initInstances(MainEngine.nrOfThreads);
		}
	}

	public static ChessBoard getInstance() {
		return instances[0];
	}

	public static ChessBoard getInstance(final int instanceNumber) {
		return instances[instanceNumber];
	}

	public static void initInstances(final int numberOfInstances) {
		instances = new ChessBoard[numberOfInstances];
		for (int i = 0; i < numberOfInstances; i++) {
			instances[i] = new ChessBoard();
		}
	}

	public static long totalMoveCount;

	public static void calculateTotalMoveCount() {
		totalMoveCount = 0;
		for (int i = 0; i < MainEngine.nrOfThreads; i++) {
			totalMoveCount += getInstance(i).moveCount;
		}
	}

	/** color, piece */
	public final long[][] pieces = new long[2][7];
	public final long[] friendlyPieces = new long[2];

	/** 4 bits: white-king,white-queen,black-king,black-queen */
	public int castlingRights;
	public int psqtScore;
	public int colorToMove, colorToMoveInverse;
	public int epIndex;
	public int materialKey;
	public int phase;

	public long allPieces, emptySpaces;
	public long zobristKey, pawnZobristKey;
	public long checkingPieces, pinnedPieces, discoveredPieces;

	public long moveCount;

	/** which piece is on which square */
	public final int[] pieceIndexes = new int[64];
	public final int[] kingIndex = new int[2];

	public int moveCounter = 0;
	private final int[] castlingAndEpHistory = new int[EngineConstants.MAX_MOVES];
	private final long[] checkingPiecesHistory = new long[EngineConstants.MAX_MOVES];
	private final long[] pinnedPiecesHistory = new long[EngineConstants.MAX_MOVES];
	private final long[] discoveredPiecesHistory = new long[EngineConstants.MAX_MOVES];
	public final long[] zobristKeyHistory = new long[EngineConstants.MAX_MOVES];

	// attack boards
	public final long[][] attacks = new long[2][7];
	public final long[] attacksAll = new long[2];
	public final long[] doubleAttacks = new long[2];
	public final int[] kingAttackersFlag = new int[2];

	public long passedPawnsAndOutposts;

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
		pinnedPiecesHistory[moveCounter] = pinnedPieces;
		discoveredPiecesHistory[moveCounter] = discoveredPieces;
		checkingPiecesHistory[moveCounter] = checkingPieces;
		setCastlingAndEpHistory();
		moveCounter++;
	}

	private void popHistoryValues() {
		moveCounter--;
		zobristKey = zobristKeyHistory[moveCounter];
		pinnedPieces = pinnedPiecesHistory[moveCounter];
		discoveredPieces = discoveredPiecesHistory[moveCounter];
		checkingPieces = checkingPiecesHistory[moveCounter];
		setCastlingAndEp();
	}

	private void setCastlingAndEpHistory() {
		castlingAndEpHistory[moveCounter] = castlingRights << 10 | epIndex;
	}

	private void setCastlingAndEp() {
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
			Assert.isTrue(attackedPieceIndex != KING);
			Assert.isTrue(attackedPieceIndex == 0 || (Util.POWER_LOOKUP[toIndex] & friendlyPieces[colorToMove]) == 0);
		}

		pushHistoryValues();

		zobristKey ^= Zobrist.piece[colorToMove][sourcePieceIndex][fromIndex] ^ Zobrist.piece[colorToMove][sourcePieceIndex][toIndex] ^ Zobrist.sideToMove;
		if (epIndex != 0) {
			zobristKey ^= Zobrist.epIndex[epIndex];
			epIndex = 0;
		}

		friendlyPieces[colorToMove] ^= fromToMask;
		pieceIndexes[fromIndex] = EMPTY;
		pieceIndexes[toIndex] = sourcePieceIndex;
		pieces[colorToMove][sourcePieceIndex] ^= fromToMask;
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
			friendlyPieces[colorToMoveInverse] ^= toMask;
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
			friendlyPieces[colorToMoveInverse] ^= toMask;
			pieces[colorToMoveInverse][attackedPieceIndex] ^= toMask;
			zobristKey ^= Zobrist.piece[colorToMoveInverse][attackedPieceIndex][toIndex];
			materialKey -= MaterialUtil.VALUES[colorToMoveInverse][attackedPieceIndex];
		}

		allPieces = friendlyPieces[colorToMove] | friendlyPieces[colorToMoveInverse];
		emptySpaces = ~allPieces;
		changeSideToMove();

		// update checking pieces
		if (isDiscoveredMove(fromIndex)) {
			checkingPieces = CheckUtil.getCheckingPieces(this);
		} else {
			if (MoveUtil.isNormalMove(move)) {
				checkingPieces = CheckUtil.getCheckingPieces(this, sourcePieceIndex);
			} else {
				checkingPieces = CheckUtil.getCheckingPieces(this);
			}
		}

		// TODO can this be done incrementally?
		setPinnedAndDiscoPieces();

		if (EngineConstants.ASSERT) {
			ChessBoardTestUtil.testValues(this);
		}

	}

	public void setPinnedAndDiscoPieces() {

		pinnedPieces = 0;
		discoveredPieces = 0;

		for (int kingColor = WHITE; kingColor <= BLACK; kingColor++) {

			int enemyColor = 1 - kingColor;

			if (!MaterialUtil.hasSlidingPieces(materialKey, enemyColor)) {
				continue;
			}

			long enemyPiece = (pieces[enemyColor][BISHOP] | pieces[enemyColor][QUEEN]) & MagicUtil.getBishopMovesEmptyBoard(kingIndex[kingColor])
					| (pieces[enemyColor][ROOK] | pieces[enemyColor][QUEEN]) & MagicUtil.getRookMovesEmptyBoard(kingIndex[kingColor]);
			while (enemyPiece != 0) {
				final long checkedPiece = ChessConstants.IN_BETWEEN[kingIndex[kingColor]][Long.numberOfTrailingZeros(enemyPiece)] & allPieces;
				if (Long.bitCount(checkedPiece) == 1) {
					pinnedPieces |= checkedPiece & friendlyPieces[kingColor];
					discoveredPieces |= checkedPiece & friendlyPieces[enemyColor];
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
		friendlyPieces[colorToMoveInverse] ^= fromToMask;
		pieceIndexes[fromIndex] = sourcePieceIndex;
		pieces[colorToMoveInverse][sourcePieceIndex] ^= fromToMask;
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
			pieces[colorToMove][attackedPieceIndex] |= toMask;
			friendlyPieces[colorToMove] |= toMask;
		}

		pieceIndexes[toIndex] = attackedPieceIndex;
		allPieces = friendlyPieces[colorToMove] | friendlyPieces[colorToMoveInverse];
		emptySpaces = ~allPieces;
		changeSideToMove();

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
				allPieces ^ Util.POWER_LOOKUP[MoveUtil.getFromIndex(move)], MaterialUtil.getMajorPieces(materialKey, colorToMoveInverse));
	}

	private boolean isLegalNonKingMove(final int move) {
		return !CheckUtil.isInCheck(kingIndex[colorToMove], colorToMove, pieces[colorToMoveInverse],
				allPieces ^ Util.POWER_LOOKUP[MoveUtil.getFromIndex(move)] ^ Util.POWER_LOOKUP[MoveUtil.getToIndex(move)]);
	}

	public boolean isLegalEPMove(final int fromIndex) {

		// do move, check if in check, undo move. slow but also not called very often

		final long fromToMask = Util.POWER_LOOKUP[fromIndex] ^ Util.POWER_LOOKUP[epIndex];

		// do-move and hit
		friendlyPieces[colorToMove] ^= fromToMask;
		pieces[colorToMoveInverse][PAWN] ^= Util.POWER_LOOKUP[epIndex + ChessConstants.COLOR_FACTOR_8[colorToMoveInverse]];
		allPieces = friendlyPieces[colorToMove]
				| friendlyPieces[colorToMoveInverse] ^ Util.POWER_LOOKUP[epIndex + ChessConstants.COLOR_FACTOR_8[colorToMoveInverse]];

		/* Check if is in check */
		final boolean isInCheck = CheckUtil.getCheckingPieces(this) != 0;

		// undo-move and hit
		friendlyPieces[colorToMove] ^= fromToMask;
		pieces[colorToMoveInverse][PAWN] |= Util.POWER_LOOKUP[epIndex + ChessConstants.COLOR_FACTOR_8[colorToMoveInverse]];
		allPieces = friendlyPieces[colorToMove] | friendlyPieces[colorToMoveInverse];

		return !isInCheck;

	}

	public boolean isValidQuietMove(final int move) {

		// check piece at from square
		final int fromIndex = MoveUtil.getFromIndex(move);
		final long fromSquare = Util.POWER_LOOKUP[fromIndex];
		if ((pieces[colorToMove][MoveUtil.getSourcePieceIndex(move)] & fromSquare) == 0) {
			return false;
		}

		// no piece should be at to square
		final int toIndex = MoveUtil.getToIndex(move);
		final long toSquare = Util.POWER_LOOKUP[toIndex];
		if (pieceIndexes[toIndex] != EMPTY) {
			return false;
		}

		// check if move is possible
		switch (MoveUtil.getSourcePieceIndex(move)) {
		case PAWN:
			if (colorToMove == WHITE) {
				if (fromIndex > toIndex) {
					return false;
				}
				// 2-move
				if (toIndex - fromIndex > 8 && (allPieces & Util.POWER_LOOKUP[fromIndex + 8]) != 0) {
					return false;
				}
			} else {
				if (fromIndex < toIndex) {
					return false;
				}
				// 2-move
				if (fromIndex - toIndex > 8 && (allPieces & Util.POWER_LOOKUP[fromIndex - 8]) != 0) {
					return false;
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
			return isLegalNonKingMove(move);
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
		attacks[WHITE][NIGHT] = 0;
		attacks[BLACK][NIGHT] = 0;
		attacks[WHITE][BISHOP] = 0;
		attacks[BLACK][BISHOP] = 0;
		attacks[WHITE][ROOK] = 0;
		attacks[BLACK][ROOK] = 0;
		attacks[WHITE][QUEEN] = 0;
		attacks[BLACK][QUEEN] = 0;
		doubleAttacks[WHITE] = 0;
		doubleAttacks[BLACK] = 0;
	}

	public void updatePawnAttacks() {
		updatePawnAttacks(Bitboard.getWhitePawnAttacks(pieces[WHITE][PAWN] & ~pinnedPieces), WHITE);
		updatePawnAttacks(Bitboard.getBlackPawnAttacks(pieces[BLACK][PAWN] & ~pinnedPieces), BLACK);
	}

	private void updatePawnAttacks(final long pawnAttacks, final int color) {
		attacks[color][PAWN] = pawnAttacks;
		if ((pawnAttacks & ChessConstants.KING_AREA[1 - color][kingIndex[1 - color]]) != 0) {
			kingAttackersFlag[color] = SchroderUtil.FLAG_PAWN;
		}
		long pinned = pieces[color][PAWN] & pinnedPieces;
		while (pinned != 0) {
			attacks[color][PAWN] |= StaticMoves.PAWN_ATTACKS[color][Long.numberOfTrailingZeros(pinned)]
					& ChessConstants.PINNED_MOVEMENT[Long.numberOfTrailingZeros(pinned)][kingIndex[color]];
			pinned &= pinned - 1;
		}
		attacksAll[color] = attacks[color][PAWN];
	}

	public void updateAttacks(final long moves, final int piece, final int color, final long kingArea) {
		if ((moves & kingArea) != 0) {
			kingAttackersFlag[color] |= SchroderUtil.FLAGS[piece];
		}
		doubleAttacks[color] |= attacksAll[color] & moves;
		attacksAll[color] |= moves;
		attacks[color][piece] |= moves;
	}

}