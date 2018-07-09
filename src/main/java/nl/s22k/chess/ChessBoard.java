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

import java.security.SecureRandom;
import java.util.Arrays;

import nl.s22k.chess.engine.EngineConstants;
import nl.s22k.chess.eval.EvalConstants;
import nl.s22k.chess.eval.EvalUtil;
import nl.s22k.chess.eval.KPKBitbase;
import nl.s22k.chess.eval.MaterialUtil;
import nl.s22k.chess.move.MagicUtil;
import nl.s22k.chess.move.MoveUtil;

public final class ChessBoard {

	private static ChessBoard instance = new ChessBoard();
	private static ChessBoard[] tuningInstances;

	// zobrist-keys
	public static long zkWhiteToMove;
	public static final long[] zkCastling = new long[16];
	public static final long[] zkEPIndex = new long[48];
	public static final long[][][] zkPieceValues = new long[64][2][7];

	/** color, piece */
	public final long[][] pieces = new long[2][7];
	public final long[] friendlyPieces = new long[2];

	/** 4 bits: white-king,white-queen,black-king,black-queen */
	public int castlingRights;
	public int psqtScore, psqtScoreEg;
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
	public final long[] kingArea = new long[2];

	public int moveCounter = 0;
	public final int[] psqtScoreHistory = new int[EngineConstants.MAX_MOVES];
	public final int[] psqtScoreEgHistory = new int[EngineConstants.MAX_MOVES];
	public final int[] castlingHistory = new int[EngineConstants.MAX_MOVES];
	public final int[] epIndexHistory = new int[EngineConstants.MAX_MOVES];
	public final long[] zobristKeyHistory = new long[EngineConstants.MAX_MOVES];
	public final long[] pawnZobristKeyHistory = new long[EngineConstants.MAX_MOVES];
	public final long[] checkingPiecesHistory = new long[EngineConstants.MAX_MOVES];
	public final long[] pinnedPiecesHistory = new long[EngineConstants.MAX_MOVES];
	public final long[] discoveredPiecesHistory = new long[EngineConstants.MAX_MOVES];

	// attack boards
	public final long[][] attacks = new long[2][7];
	public final long[] attacksAll = new long[2];
	public final long[] attacksWithoutKing = new long[2];
	public final int[] kingAttackersFlag = new int[2];

	public long passedPawnsAndOutposts;

	public static ChessBoard getInstance() {
		return instance;
	}

	public static ChessBoard getTestInstance() {
		return new ChessBoard();
	}

	public static void initTuningInstances(int numberOfInstances) {
		tuningInstances = new ChessBoard[numberOfInstances];
		for (int i = 0; i < numberOfInstances; i++) {
			tuningInstances[i] = new ChessBoard();
		}
	}

	public static ChessBoard getTuningInstance(int instanceNumber) {
		return tuningInstances[instanceNumber];
	}

	/**
	 * Initialize the zobrist-keys
	 */
	static {
		SecureRandom r = new SecureRandom();
		for (int bitIndex = 0; bitIndex < 64; bitIndex++) {
			for (int colorIndex = 0; colorIndex < zkPieceValues[0].length; colorIndex++) {
				for (int pieceIndex = 0; pieceIndex < zkPieceValues[0][0].length; pieceIndex++) {
					zkPieceValues[bitIndex][colorIndex][pieceIndex] = r.nextLong();
				}
			}
		}
		for (int i = 0; i < zkCastling.length; i++) {
			zkCastling[i] = r.nextLong();
		}

		// skip first item: contains only zeros, default value and has no effect when xorring
		for (int i = 1; i < zkEPIndex.length; i++) {
			zkEPIndex[i] = r.nextLong();
		}
		zkWhiteToMove = r.nextLong();
	}

	@Override
	public String toString() {
		return ChessBoardUtil.toString(this);
	}

	public boolean isDrawishByMaterial(final int color) {
		// no pawns or queens
		if (MaterialUtil.hasPawnsOrQueens(materialKey, color)) {
			return false;
		}

		// only nights?
		if (MaterialUtil.hasOnlyNights(materialKey, color)) {
			return true;
		}

		// material difference bigger than bishop + 50
		// TODO do not include pawn score (why...?)
		return EvalUtil.getImbalances(this) * ChessConstants.COLOR_FACTOR[color] < EvalConstants.MATERIAL[BISHOP] + 50;
	}

	public boolean isDrawByMaterial(final int color) {
		if (Long.bitCount(friendlyPieces[color]) > 2) {
			return false;
		}
		if (Long.bitCount(friendlyPieces[color]) == 1) {
			// K
			return true;
		} else {
			// KN or KB
			if (pieces[color][NIGHT] != 0 || pieces[color][BISHOP] != 0) {
				return true;
			}
			// KP, KR or KQ
			return false;
		}
	}

	public boolean isDrawByMaterial() {

		boolean isDraw = false;
		switch (Long.bitCount(allPieces)) {
		case 2:
			// KK
			isDraw = true;
			break;
		case 3:
			if (pieces[WHITE][PAWN] != 0 || pieces[BLACK][PAWN] != 0) {
				// KPK
				isDraw = KPKBitbase.isDraw(this);
			} else {
				// KKB or KKN?
				isDraw = pieces[WHITE][BISHOP] != 0 || pieces[BLACK][BISHOP] != 0 || pieces[WHITE][NIGHT] != 0 || pieces[BLACK][NIGHT] != 0;
			}
			break;
		case 4:
			isDraw = isDrawByMtrl4Pieces();
		}

		if (Statistics.ENABLED && isDraw) {
			Statistics.drawByMaterialCount++;
		}
		return isDraw;

	}

	private boolean isDrawByMtrl4Pieces() {
		if (Long.bitCount(pieces[WHITE][NIGHT]) + Long.bitCount(pieces[BLACK][NIGHT]) == 2) {
			// KNNK or KNKN
			return true;
		}
		switch (Long.bitCount(pieces[WHITE][BISHOP] | pieces[BLACK][BISHOP])) {
		case 1:
			if (Long.bitCount(pieces[WHITE][PAWN] | pieces[WHITE][BISHOP]) == 2) {
				if ((pieces[WHITE][PAWN] & Bitboard.FILE_A) != 0 && (Bitboard.WHITE_SQUARES & pieces[WHITE][BISHOP]) == 0) {
					return (pieces[BLACK][KING] & Bitboard.A8) != 0;
				}
				if ((pieces[WHITE][PAWN] & Bitboard.FILE_H) != 0 && (Bitboard.BLACK_SQUARES & pieces[WHITE][BISHOP]) == 0) {
					return (pieces[BLACK][KING] & Bitboard.H8) != 0;
				}
			} else if (Long.bitCount(pieces[BLACK][PAWN] | pieces[BLACK][BISHOP]) == 2) {
				if ((pieces[BLACK][PAWN] & Bitboard.FILE_A) != 0 && (Bitboard.BLACK_SQUARES & pieces[BLACK][BISHOP]) == 0) {
					return (pieces[WHITE][KING] & Bitboard.A1) != 0;
				}
				if ((pieces[BLACK][PAWN] & Bitboard.FILE_H) != 0 && (Bitboard.WHITE_SQUARES & pieces[BLACK][BISHOP]) == 0) {
					return (pieces[WHITE][KING] & Bitboard.H1) != 0;
				}
			} else {
				// KBKN or KNKB?
				return (Long.bitCount(pieces[WHITE][NIGHT] | pieces[BLACK][BISHOP]) == 2 || Long.bitCount(pieces[BLACK][NIGHT] | pieces[WHITE][BISHOP]) == 2);
			}
			break;
		case 2:
			// KBKB?
			return Long.bitCount(pieces[WHITE][BISHOP]) == 1;
		}

		return false;
	}

	public void changeSideToMove() {
		colorToMove = colorToMoveInverse;
		colorToMoveInverse = 1 - colorToMove;
	}

	private void pushHistoryValues() {
		psqtScoreHistory[moveCounter] = psqtScore;
		psqtScoreEgHistory[moveCounter] = psqtScoreEg;
		castlingHistory[moveCounter] = castlingRights;
		epIndexHistory[moveCounter] = epIndex;
		zobristKeyHistory[moveCounter] = zobristKey;
		pawnZobristKeyHistory[moveCounter] = pawnZobristKey;
		pinnedPiecesHistory[moveCounter] = pinnedPieces;
		discoveredPiecesHistory[moveCounter] = discoveredPieces;
		checkingPiecesHistory[moveCounter] = checkingPieces;
		moveCounter++;
	}

	private void popHistoryValues() {
		moveCounter--;
		epIndex = epIndexHistory[moveCounter];
		zobristKey = zobristKeyHistory[moveCounter];
		psqtScore = psqtScoreHistory[moveCounter];
		psqtScoreEg = psqtScoreEgHistory[moveCounter];
		castlingRights = castlingHistory[moveCounter];
		pawnZobristKey = pawnZobristKeyHistory[moveCounter];
		pinnedPieces = pinnedPiecesHistory[moveCounter];
		discoveredPieces = discoveredPiecesHistory[moveCounter];
		checkingPieces = checkingPiecesHistory[moveCounter];
	}

	public void doNullMove() {
		pushHistoryValues();

		zobristKey ^= zkEPIndex[epIndex] ^ zkWhiteToMove;
		epIndex = 0;
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

	public void doMove(int move) {

		moveCount++;

		final int fromIndex = MoveUtil.getFromIndex(move);
		final long fromMask = Util.POWER_LOOKUP[fromIndex];
		int toIndex = MoveUtil.getToIndex(move);
		long toMask = Util.POWER_LOOKUP[toIndex];
		final long fromToMask = fromMask ^ toMask;
		final int sourcePieceIndex = MoveUtil.getSourcePieceIndex(move);
		final int attackedPieceIndex = MoveUtil.getAttackedPieceIndex(move);

		if (EngineConstants.ASSERT) {
			Assert.isTrue(attackedPieceIndex != KING);
			Assert.isTrue(attackedPieceIndex == 0 || (Util.POWER_LOOKUP[toIndex] & friendlyPieces[colorToMove]) == 0);
		}

		pushHistoryValues();

		zobristKey ^= zkEPIndex[epIndex] ^ zkPieceValues[fromIndex][colorToMove][sourcePieceIndex] ^ zkPieceValues[toIndex][colorToMove][sourcePieceIndex]
				^ zkWhiteToMove;
		epIndex = 0;

		friendlyPieces[colorToMove] ^= fromToMask;
		pieceIndexes[fromIndex] = EMPTY;
		pieceIndexes[toIndex] = sourcePieceIndex;
		pieces[colorToMove][sourcePieceIndex] ^= fromToMask;
		psqtScore += EvalConstants.PSQT[sourcePieceIndex][colorToMove][toIndex] - EvalConstants.PSQT[sourcePieceIndex][colorToMove][fromIndex];
		psqtScoreEg += EvalConstants.PSQT_EG[sourcePieceIndex][colorToMove][toIndex] - EvalConstants.PSQT_EG[sourcePieceIndex][colorToMove][fromIndex];

		switch (sourcePieceIndex) {
		case PAWN:
			pawnZobristKey ^= zkPieceValues[fromIndex][colorToMove][PAWN];
			if (MoveUtil.isPromotion(move)) {
				phase -= EvalConstants.PHASE[MoveUtil.getMoveType(move)];
				materialKey += MaterialUtil.VALUES[colorToMove][MoveUtil.getMoveType(move)] - MaterialUtil.VALUES[colorToMove][PAWN];
				pieces[colorToMove][PAWN] ^= toMask;
				pieces[colorToMove][MoveUtil.getMoveType(move)] |= toMask;
				pieceIndexes[toIndex] = MoveUtil.getMoveType(move);
				zobristKey ^= zkPieceValues[toIndex][colorToMove][PAWN] ^ zkPieceValues[toIndex][colorToMove][MoveUtil.getMoveType(move)];
				psqtScore += EvalConstants.PSQT[MoveUtil.getMoveType(move)][colorToMove][toIndex] - EvalConstants.PSQT[PAWN][colorToMove][toIndex];
				psqtScoreEg += EvalConstants.PSQT_EG[MoveUtil.getMoveType(move)][colorToMove][toIndex] - EvalConstants.PSQT_EG[PAWN][colorToMove][toIndex];
			} else {
				pawnZobristKey ^= zkPieceValues[toIndex][colorToMove][PAWN];
				// check 2-move
				if (ChessConstants.IN_BETWEEN[fromIndex][toIndex] != 0) {
					epIndex = Long.numberOfTrailingZeros(ChessConstants.IN_BETWEEN[fromIndex][toIndex]);
					zobristKey ^= zkEPIndex[epIndex];
				}
			}
			break;

		case ROOK:
			if (castlingRights != 0) {
				zobristKey ^= zkCastling[castlingRights];
				castlingRights = CastlingUtil.getRookMovedOrAttackedCastlingRights(castlingRights, fromIndex);
				zobristKey ^= zkCastling[castlingRights];
			}
			break;

		case KING:
			updateKingValues(colorToMove, toIndex);
			if (castlingRights != 0) {
				if (MoveUtil.getMoveType(move) == MoveUtil.TYPE_CASTLING) {
					CastlingUtil.castleRookUpdateKeyAndPsqt(this, toIndex);
				}
				zobristKey ^= zkCastling[castlingRights];
				castlingRights = CastlingUtil.getKingMovedCastlingRights(castlingRights, fromIndex);
				zobristKey ^= zkCastling[castlingRights];
			}
		}

		// piece hit?
		switch (attackedPieceIndex) {
		case EMPTY:
			break;
		case PAWN:
			if (MoveUtil.getMoveType(move) == MoveUtil.TYPE_EP) {
				toIndex += ChessConstants.COLOR_FACTOR_8[colorToMoveInverse];
				toMask = Util.POWER_LOOKUP[toIndex];
				pieceIndexes[toIndex] = EMPTY;
			}
			pawnZobristKey ^= zkPieceValues[toIndex][colorToMoveInverse][PAWN];
			psqtScore -= EvalConstants.PSQT[attackedPieceIndex][colorToMoveInverse][toIndex];
			psqtScoreEg -= EvalConstants.PSQT_EG[attackedPieceIndex][colorToMoveInverse][toIndex];
			friendlyPieces[colorToMoveInverse] ^= toMask;
			pieces[colorToMoveInverse][attackedPieceIndex] ^= toMask;
			zobristKey ^= zkPieceValues[toIndex][colorToMoveInverse][attackedPieceIndex];
			materialKey -= MaterialUtil.VALUES[colorToMoveInverse][PAWN];
			break;
		case ROOK:
			if (castlingRights != 0) {
				zobristKey ^= zkCastling[castlingRights];
				castlingRights = CastlingUtil.getRookMovedOrAttackedCastlingRights(castlingRights, toIndex);
				zobristKey ^= zkCastling[castlingRights];
			}
		default:
			phase += EvalConstants.PHASE[attackedPieceIndex];
			psqtScore -= EvalConstants.PSQT[attackedPieceIndex][colorToMoveInverse][toIndex];
			psqtScoreEg -= EvalConstants.PSQT_EG[attackedPieceIndex][colorToMoveInverse][toIndex];
			friendlyPieces[colorToMoveInverse] ^= toMask;
			pieces[colorToMoveInverse][attackedPieceIndex] ^= toMask;
			zobristKey ^= zkPieceValues[toIndex][colorToMoveInverse][attackedPieceIndex];
			materialKey -= MaterialUtil.VALUES[colorToMoveInverse][attackedPieceIndex];
		}

		allPieces = friendlyPieces[colorToMove] | friendlyPieces[colorToMoveInverse];
		emptySpaces = ~allPieces;
		changeSideToMove();

		// update checking pieces
		if ((discoveredPieces & Util.POWER_LOOKUP[fromIndex]) == 0) {
			if (MoveUtil.getMoveType(move) == MoveUtil.TYPE_NORMAL) {
				checkingPieces = CheckUtil.getCheckingPieces(this, sourcePieceIndex);
			} else {
				checkingPieces = CheckUtil.getCheckingPieces(this);
			}
		} else {
			checkingPieces = CheckUtil.getCheckingPieces(this);
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

		for (int kingColor = ChessConstants.WHITE; kingColor <= ChessConstants.BLACK; kingColor++) {

			int enemyColor = 1 - kingColor;

			if (!MaterialUtil.hasSlidingPieces(materialKey, enemyColor)) {
				continue;
			}

			long enemyPiece = (pieces[enemyColor][BISHOP] | pieces[enemyColor][QUEEN]) & MagicUtil.bishopMovesEmptyBoard[kingIndex[kingColor]]
					| (pieces[enemyColor][ROOK] | pieces[enemyColor][QUEEN]) & MagicUtil.rookMovesEmptyBoard[kingIndex[kingColor]];
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

	public void undoMove(int move) {

		final int fromIndex = MoveUtil.getFromIndex(move);
		int toIndex = MoveUtil.getToIndex(move);
		long toMask = Util.POWER_LOOKUP[toIndex];
		final long fromToMask = Util.POWER_LOOKUP[fromIndex] ^ toMask;
		final int sourcePieceIndex = MoveUtil.getSourcePieceIndex(move);
		final int attackedPieceIndex = MoveUtil.getAttackedPieceIndex(move);

		popHistoryValues();

		// undo move
		friendlyPieces[colorToMoveInverse] ^= fromToMask;
		pieceIndexes[fromIndex] = sourcePieceIndex;
		pieces[colorToMoveInverse][sourcePieceIndex] ^= fromToMask;

		switch (sourcePieceIndex) {
		case EMPTY:
			// not necessary but provides a table-index
			break;
		case PAWN:
			if (MoveUtil.isPromotion(move)) {
				phase += EvalConstants.PHASE[MoveUtil.getMoveType(move)];
				materialKey -= MaterialUtil.VALUES[colorToMoveInverse][MoveUtil.getMoveType(move)] - MaterialUtil.VALUES[colorToMoveInverse][PAWN];
				pieces[colorToMoveInverse][PAWN] ^= toMask;
				pieces[colorToMoveInverse][MoveUtil.getMoveType(move)] ^= toMask;
			}
			break;
		case KING:
			if (MoveUtil.getMoveType(move) == MoveUtil.TYPE_CASTLING) {
				CastlingUtil.uncastleRook(this, toIndex);
			}
			updateKingValues(colorToMoveInverse, fromIndex);
		}

		// undo hit
		switch (attackedPieceIndex) {
		case EMPTY:
			break;
		case PAWN:
			if (MoveUtil.getMoveType(move) == MoveUtil.TYPE_EP) {
				pieceIndexes[toIndex] = EMPTY;
				toIndex += ChessConstants.COLOR_FACTOR_8[colorToMove];
				toMask = Util.POWER_LOOKUP[toIndex];
			}
			pieces[colorToMove][attackedPieceIndex] |= toMask;
			friendlyPieces[colorToMove] |= toMask;
			materialKey += MaterialUtil.VALUES[colorToMove][PAWN];
			break;
		default:
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

	public void updateKingValues(final int kingColor, final int index) {
		kingIndex[kingColor] = index;
		kingArea[kingColor] = ChessConstants.KING_AREA[kingColor][index];
	}

	public boolean isLegal(final int move) {
		if (MoveUtil.getSourcePieceIndex(move) == KING) {
			return !CheckUtil.isInCheckIncludingKing(MoveUtil.getToIndex(move), colorToMove, pieces[colorToMoveInverse],
					allPieces ^ Util.POWER_LOOKUP[MoveUtil.getFromIndex(move)], MaterialUtil.getMajorPieces(materialKey, colorToMoveInverse));
		}

		if (MoveUtil.getAttackedPieceIndex(move) != 0) {
			if (MoveUtil.getMoveType(move) == MoveUtil.TYPE_EP) {
				return isLegalEPMove(MoveUtil.getFromIndex(move));
			}
			return true;
		}

		if (checkingPieces != 0) {
			return !CheckUtil.isInCheck(kingIndex[colorToMove], colorToMove, pieces[colorToMoveInverse],
					allPieces ^ Util.POWER_LOOKUP[MoveUtil.getFromIndex(move)] ^ Util.POWER_LOOKUP[MoveUtil.getToIndex(move)]);
		}
		return true;
	}

	private boolean isLegalEPMove(final int fromIndex) {

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

	public boolean isValidQuietMove(int move) {

		// check piece at from-position
		final int fromIndex = MoveUtil.getFromIndex(move);
		if ((pieces[colorToMove][MoveUtil.getSourcePieceIndex(move)] & Util.POWER_LOOKUP[fromIndex]) == 0) {
			return false;
		}

		// no piece should be at to-position
		final int toIndex = MoveUtil.getToIndex(move);
		if (pieceIndexes[toIndex] != EMPTY) {
			return false;
		}

		// check if move is possible
		switch (MoveUtil.getSourcePieceIndex(move)) {
		case PAWN:
			// 2-move
			if (Math.abs(fromIndex - toIndex) > 8 && (allPieces & Util.POWER_LOOKUP[fromIndex + ChessConstants.COLOR_FACTOR_8[colorToMove]]) != 0) {
				return false;
			}
			break;
		case NIGHT:
			break;
		case BISHOP:
			if ((MagicUtil.getBishopMoves(fromIndex, allPieces) & Util.POWER_LOOKUP[toIndex]) == 0) {
				return false;
			}
			break;
		case ROOK:
			if ((MagicUtil.getRookMoves(fromIndex, allPieces) & Util.POWER_LOOKUP[toIndex]) == 0) {
				return false;
			}
			break;
		case QUEEN:
			if ((MagicUtil.getQueenMoves(fromIndex, allPieces) & Util.POWER_LOOKUP[toIndex]) == 0) {
				return false;
			}
			break;
		case KING:
			if (MoveUtil.getMoveType(move) == MoveUtil.TYPE_CASTLING) {
				long castlingIndexes = CastlingUtil.getCastlingIndexes(this);
				while (castlingIndexes != 0) {
					if (toIndex == Long.numberOfTrailingZeros(castlingIndexes)) {
						return CastlingUtil.isValidCastlingMove(this, fromIndex, toIndex);
					}
					castlingIndexes &= castlingIndexes - 1;
				}
				return false;
			}
			return true;

		}

		if ((Util.POWER_LOOKUP[fromIndex] & pinnedPieces) == 0) {
			return true;
		}
		return (ChessConstants.PINNED_MOVEMENT[fromIndex][kingIndex[colorToMove]] & Util.POWER_LOOKUP[toIndex]) != 0;
	}

	/**
	 * Method for testing the validity of tt-moves. This test is most of the time disabled.
	 */
	public boolean isValidMove(int move) {
		if (MoveUtil.getAttackedPieceIndex(move) == 0 && !MoveUtil.isPromotion(move)) {
			return isValidQuietMove(move);
		}

		if (MoveUtil.isPromotion(move) || MoveUtil.getMoveType(move) == MoveUtil.TYPE_CASTLING || MoveUtil.getMoveType(move) == MoveUtil.TYPE_EP) {
			// TODO
			return true;
		}

		// check piece at from-position
		final int fromIndex = MoveUtil.getFromIndex(move);
		if ((pieces[colorToMove][MoveUtil.getSourcePieceIndex(move)] & Util.POWER_LOOKUP[fromIndex]) == 0) {
			return false;
		}

		// same piece should be at to-position
		final int toIndex = MoveUtil.getToIndex(move);
		if ((pieces[colorToMoveInverse][MoveUtil.getAttackedPieceIndex(move)] & Util.POWER_LOOKUP[toIndex]) == 0) {
			return false;
		}

		// TODO
		return true;
	}

	public boolean isRepetition() {

		if (!EngineConstants.ENABLE_REPETITION_TABLE) {
			return false;
		}

		// TODO same position but other side to move is also a draw?
		for (int i = moveCounter - 1; i >= 0 && i > moveCounter - 50; i--) {
			// TODO if move was an attacking-move or pawn move, no repetition!
			if (zobristKey == zobristKeyHistory[i]) {
				if (Statistics.ENABLED) {
					Statistics.repetitions++;
				}
				return true;
			}
		}
		return false;
	}

	public void clearHistoryValues() {
		// history
		Arrays.fill(psqtScoreHistory, 0);
		Arrays.fill(castlingHistory, 0);
		Arrays.fill(epIndexHistory, 0);
		Arrays.fill(zobristKeyHistory, 0);
		Arrays.fill(pawnZobristKeyHistory, 0);
		Arrays.fill(checkingPiecesHistory, 0);
		Arrays.fill(pinnedPiecesHistory, 0);
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
	}

}