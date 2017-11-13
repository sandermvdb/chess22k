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

import nl.s22k.chess.engine.EngineConstants;
import nl.s22k.chess.eval.EvalConstants;
import nl.s22k.chess.eval.EvalUtil;
import nl.s22k.chess.move.MagicUtil;
import nl.s22k.chess.move.MoveUtil;
import nl.s22k.chess.move.StaticMoves;

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
	private final long[] temporaryEnemyPieces = new long[7];

	/** 4 bits: white-king,white-queen,black-king,black-queen */
	public int castlingRights;
	public int psqtScore, psqtScoreEg;
	public int colorToMove, colorToMoveInverse;
	public int epIndex;

	public long allPieces, emptySpaces;
	public long zobristKey, pawnZobristKey;
	public long checkingPieces, pinnedPieces;

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

	// attack boards
	public final long[][] attacks = new long[2][7];
	public final long[] attacksAll = new long[2];
	public final long[] attacksWithoutKing = new long[2];

	public final int[] mobilityScore = new int[2];
	public long passedPawns = 0;

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

	public boolean hasOnlyPawns(final int color) {
		return (pieces[color][ROOK] | pieces[color][BISHOP] | pieces[color][QUEEN] | pieces[color][NIGHT]) == 0;
	}

	public boolean isDrawishByMaterial(int color) {
		// no pawns or queens
		if ((pieces[WHITE][PAWN] | pieces[BLACK][PAWN] | pieces[WHITE][QUEEN] | pieces[BLACK][QUEEN]) != 0) {
			return false;
		}
		// material difference bigger than bishop + 50
		return EvalUtil.calculateMaterialExcludingPawnScores(this) * ChessConstants.COLOR_FACTOR[color] < EvalConstants.MATERIAL_SCORES[BISHOP] + 50;
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

		switch (Long.bitCount(allPieces)) {
		case 2:
			// KK
			return true;
		case 3:
			// KKB or KKN?
			return pieces[WHITE][BISHOP] != 0 || pieces[BLACK][BISHOP] != 0 || pieces[WHITE][NIGHT] != 0 || pieces[BLACK][NIGHT] != 0;
		case 4:
			if (Long.bitCount(pieces[WHITE][NIGHT]) + Long.bitCount(pieces[BLACK][NIGHT]) == 2) {
				// KNNK or KNKN
				return true;
			}
			switch (Long.bitCount(pieces[WHITE][BISHOP] | pieces[BLACK][BISHOP])) {
			case 1:
				// KBKN or KNKB?
				return (Long.bitCount(pieces[WHITE][NIGHT] | pieces[BLACK][BISHOP]) == 2 || Long.bitCount(pieces[BLACK][NIGHT] | pieces[WHITE][BISHOP]) == 2);
			case 2:
				// KBKB?
				return Long.bitCount(pieces[WHITE][BISHOP]) == 1;
			default:
				return false;
			}

		default:
			return false;
		}

	}

	public boolean isBadBishopEndgame() {
		if (Long.bitCount(allPieces) != 4) {
			return false;
		}

		if (Long.bitCount(pieces[WHITE][PAWN]) == 1 && Long.bitCount(pieces[WHITE][BISHOP]) == 1) {
			if ((pieces[WHITE][PAWN] & Bitboard.FILE_A) != 0 && (Bitboard.WHITE_SQUARES & pieces[WHITE][BISHOP]) == 0) {
				return (pieces[BLACK][KING] & Bitboard.A8) != 0;
			}
			if ((pieces[WHITE][PAWN] & Bitboard.FILE_H) != 0 && (Bitboard.BLACK_SQUARES & pieces[WHITE][BISHOP]) == 0) {
				return (pieces[BLACK][KING] & Bitboard.H8) != 0;
			}
		}

		else if (Long.bitCount(pieces[BLACK][PAWN]) == 1 && Long.bitCount(pieces[BLACK][BISHOP]) == 1) {
			if ((pieces[BLACK][PAWN] & Bitboard.FILE_A) != 0 && (Bitboard.BLACK_SQUARES & pieces[BLACK][BISHOP]) == 0) {
				return (pieces[WHITE][KING] & Bitboard.A1) != 0;
			}
			if ((pieces[BLACK][PAWN] & Bitboard.FILE_H) != 0 && (Bitboard.WHITE_SQUARES & pieces[BLACK][BISHOP]) == 0) {
				return (pieces[WHITE][KING] & Bitboard.H1) != 0;
			}
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

		Statistics.moveCount++;

		final int fromIndex = MoveUtil.getFromIndex(move);
		final long fromMask = Util.POWER_LOOKUP[fromIndex];
		int toIndex = MoveUtil.getToIndex(move);
		long toMask = Util.POWER_LOOKUP[toIndex];
		final long fromToMask = fromMask ^ toMask;
		final int sourcePieceIndex = MoveUtil.getSourcePieceIndex(move);
		final int attackedPieceIndex = MoveUtil.getAttackedPieceIndex(move);

		if (EngineConstants.ASSERT) {
			assert attackedPieceIndex != KING : "ChessBoard: Illegal move: king is being hit!";
			assert attackedPieceIndex == 0 || (Util.POWER_LOOKUP[toIndex] & friendlyPieces[colorToMove]) == 0 : "ChessBoard: Hitting own piece!";
		}

		pushHistoryValues();

		zobristKey ^= zkEPIndex[epIndex] ^ zkPieceValues[fromIndex][colorToMove][sourcePieceIndex] ^ zkPieceValues[toIndex][colorToMove][sourcePieceIndex]
				^ zkWhiteToMove;
		epIndex = 0;

		friendlyPieces[colorToMove] ^= fromToMask;
		pieceIndexes[fromIndex] = EMPTY;
		pieceIndexes[toIndex] = sourcePieceIndex;
		pieces[colorToMove][sourcePieceIndex] ^= fromToMask;
		psqtScore += EvalConstants.PSQT_SCORES[sourcePieceIndex][colorToMove][toIndex] - EvalConstants.PSQT_SCORES[sourcePieceIndex][colorToMove][fromIndex];
		psqtScoreEg += EvalConstants.PSQT_EG_SCORES[sourcePieceIndex][colorToMove][toIndex]
				- EvalConstants.PSQT_EG_SCORES[sourcePieceIndex][colorToMove][fromIndex];

		switch (sourcePieceIndex) {
		case PAWN:
			pawnZobristKey ^= zkPieceValues[fromIndex][colorToMove][PAWN];
			if (MoveUtil.isPromotion(move)) {
				pieces[colorToMove][PAWN] ^= toMask;
				pieces[colorToMove][MoveUtil.getMoveType(move)] |= toMask;
				pieceIndexes[toIndex] = MoveUtil.getMoveType(move);
				zobristKey ^= zkPieceValues[toIndex][colorToMove][PAWN] ^ zkPieceValues[toIndex][colorToMove][MoveUtil.getMoveType(move)];
				psqtScore += EvalConstants.PSQT_SCORES[MoveUtil.getMoveType(move)][colorToMove][toIndex]
						- EvalConstants.PSQT_SCORES[PAWN][colorToMove][toIndex];
				psqtScoreEg += EvalConstants.PSQT_EG_SCORES[MoveUtil.getMoveType(move)][colorToMove][toIndex]
						- EvalConstants.PSQT_EG_SCORES[PAWN][colorToMove][toIndex];
			} else {
				pawnZobristKey ^= zkPieceValues[toIndex][colorToMove][PAWN];
				// check 2-move
				if (ChessConstants.ROOK_IN_BETWEEN[fromIndex][toIndex] != 0) {
					epIndex = Long.numberOfTrailingZeros(ChessConstants.ROOK_IN_BETWEEN[fromIndex][toIndex]);
					zobristKey ^= zkEPIndex[epIndex];
				}
			}
			break;

		case ROOK:
			zobristKey ^= zkCastling[castlingRights];
			castlingRights = CastlingUtil.getRookMovedOrAttackedCastlingRights(castlingRights, fromIndex);
			zobristKey ^= zkCastling[castlingRights];
			break;

		case KING:
			kingIndex[colorToMove] = toIndex;
			kingArea[colorToMove] = ChessConstants.KING_SAFETY_FRONT_FURTHER[colorToMove][toIndex] | ChessConstants.KING_SAFETY_FRONT[colorToMove][toIndex]
					| ChessConstants.KING_SAFETY_NEXT[toIndex] | ChessConstants.KING_SAFETY_BEHIND[colorToMove][toIndex];
			if (MoveUtil.getMoveType(move) == MoveUtil.TYPE_CASTLING) {
				CastlingUtil.castleRookUpdateKeyAndPsqt(this, toIndex);
			}
			zobristKey ^= zkCastling[castlingRights];
			castlingRights = CastlingUtil.getKingMovedCastlingRights(castlingRights, fromIndex);
			zobristKey ^= zkCastling[castlingRights];
		}

		// piece hit?
		if (attackedPieceIndex != EMPTY) {

			switch (attackedPieceIndex) {
			case PAWN:
				if (MoveUtil.getMoveType(move) == MoveUtil.TYPE_EP) {
					toIndex += ChessConstants.COLOR_FACTOR_8[colorToMoveInverse];
					toMask = Util.POWER_LOOKUP[toIndex];
					pieceIndexes[toIndex] = EMPTY;
				}
				pawnZobristKey ^= zkPieceValues[toIndex][colorToMoveInverse][PAWN];
				break;
			case ROOK:
				zobristKey ^= zkCastling[castlingRights];
				castlingRights = CastlingUtil.getRookMovedOrAttackedCastlingRights(castlingRights, toIndex);
				zobristKey ^= zkCastling[castlingRights];
				break;
			}

			psqtScore -= EvalConstants.PSQT_SCORES[attackedPieceIndex][colorToMoveInverse][toIndex];
			psqtScoreEg -= EvalConstants.PSQT_EG_SCORES[attackedPieceIndex][colorToMoveInverse][toIndex];
			friendlyPieces[colorToMoveInverse] ^= toMask;
			pieces[colorToMoveInverse][attackedPieceIndex] ^= toMask;
			zobristKey ^= zkPieceValues[toIndex][colorToMoveInverse][attackedPieceIndex];
		}

		allPieces = friendlyPieces[colorToMove] | friendlyPieces[colorToMoveInverse];
		emptySpaces = ~allPieces;
		changeSideToMove();

		// update checking pieces
		switch (sourcePieceIndex) {
		case PAWN:
			if (MoveUtil.getMoveType(move) == MoveUtil.TYPE_PROMOTION_N) {
				checkingPieces = CheckUtil.getCheckingPieces(this);
			} else {
				checkingPieces = CheckUtil.getCheckingPiecesWithoutKnight(this);
			}
			break;
		case NIGHT:
			checkingPieces = CheckUtil.getCheckingPiecesWithoutPawn(this);
			break;
		default:
			checkingPieces = CheckUtil.getCheckingPiecesWithoutKnightAndPawn(this);
		}

		// TODO can this be done iteratively?
		pinnedPieces = getPinnedPieces();

		if (EngineConstants.ASSERT) {
			ChessBoardTestUtil.testValues(this);
		}

	}

	public long getPinnedPieces() {

		long checkedPiece;
		long enemyPiece;
		int enemyColor;

		long pinnedPieces = 0;

		for (int kingColor = ChessConstants.WHITE; kingColor <= ChessConstants.BLACK; kingColor++) {

			enemyColor = 1 - kingColor;

			// bishop and queen
			enemyPiece = pieces[enemyColor][BISHOP] | pieces[enemyColor][QUEEN];
			while (enemyPiece != 0) {
				checkedPiece = ChessConstants.BISHOP_IN_BETWEEN[kingIndex[kingColor]][Long.numberOfTrailingZeros(enemyPiece)] & allPieces;
				if (Long.bitCount(checkedPiece) == 1) {
					pinnedPieces |= checkedPiece & friendlyPieces[kingColor];
				}
				enemyPiece &= enemyPiece - 1;
			}

			// rook and queen
			enemyPiece = pieces[enemyColor][ROOK] | pieces[enemyColor][QUEEN];
			while (enemyPiece != 0) {
				checkedPiece = ChessConstants.ROOK_IN_BETWEEN[kingIndex[kingColor]][Long.numberOfTrailingZeros(enemyPiece)] & allPieces;
				if (Long.bitCount(checkedPiece) == 1) {
					pinnedPieces |= checkedPiece & friendlyPieces[kingColor];
				}
				enemyPiece &= enemyPiece - 1;
			}
		}

		return pinnedPieces;
	}

	public long getPinnedPieces(final int color, final long allPieces) {

		final int colorInverse = 1 - color;
		long pinnedPieces = 0;

		long checkedPiece;
		long piece;
		// bishop and queen
		piece = (pieces[colorInverse][BISHOP] | pieces[colorInverse][QUEEN]) & allPieces;
		while (piece != 0) {
			checkedPiece = ChessConstants.BISHOP_IN_BETWEEN[kingIndex[color]][Long.numberOfTrailingZeros(piece)] & allPieces;
			if (Long.bitCount(checkedPiece) == 1) {
				pinnedPieces |= checkedPiece;
			}
			piece &= piece - 1;
		}

		// rook and queen
		piece = (pieces[colorInverse][ROOK] | pieces[colorInverse][QUEEN]) & allPieces;
		while (piece != 0) {
			checkedPiece = ChessConstants.ROOK_IN_BETWEEN[kingIndex[color]][Long.numberOfTrailingZeros(piece)] & allPieces;
			if (Long.bitCount(checkedPiece) == 1) {
				pinnedPieces |= checkedPiece;
			}
			piece &= piece - 1;
		}

		return pinnedPieces;
	}

	public void undoMove(int move) {

		final int fromIndex = MoveUtil.getFromIndex(move);
		int toIndex = MoveUtil.getToIndex(move);
		final long fromMask = Util.POWER_LOOKUP[fromIndex];
		long toMask = Util.POWER_LOOKUP[toIndex];
		final long fromToMask = fromMask ^ toMask;
		final int sourcePieceIndex = MoveUtil.getSourcePieceIndex(move);
		final int attackedPieceIndex = MoveUtil.getAttackedPieceIndex(move);

		popHistoryValues();

		// undo move
		friendlyPieces[colorToMoveInverse] ^= fromToMask;
		pieceIndexes[fromIndex] = sourcePieceIndex;
		pieces[colorToMoveInverse][sourcePieceIndex] ^= fromToMask;

		switch (sourcePieceIndex) {
		case PAWN:
			if (MoveUtil.isPromotion(move)) {
				pieces[colorToMoveInverse][PAWN] ^= toMask;
				pieces[colorToMoveInverse][MoveUtil.getMoveType(move)] ^= toMask;
			}
			break;
		case KING:
			if (MoveUtil.getMoveType(move) == MoveUtil.TYPE_CASTLING) {
				CastlingUtil.uncastleRook(this, toIndex);
			}
			kingIndex[colorToMoveInverse] = fromIndex;
			kingArea[colorToMoveInverse] = ChessConstants.KING_SAFETY_FRONT_FURTHER[colorToMoveInverse][fromIndex]
					| ChessConstants.KING_SAFETY_FRONT[colorToMoveInverse][fromIndex] | ChessConstants.KING_SAFETY_NEXT[fromIndex]
					| ChessConstants.KING_SAFETY_BEHIND[colorToMoveInverse][fromIndex];
		}

		// undo hit
		if (attackedPieceIndex != EMPTY) {
			if (MoveUtil.getMoveType(move) == MoveUtil.TYPE_EP) {
				pieceIndexes[toIndex] = EMPTY;
				toIndex += ChessConstants.COLOR_FACTOR_8[colorToMove];
				toMask = Util.POWER_LOOKUP[toIndex];
			}
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

	public boolean isLegalMove(final int fromIndex, final int toIndex) {
		// called when king is in check or piece is pinned and by killer-move-validity-check
		return !CheckUtil.isInCheck(kingIndex[colorToMove], colorToMove, friendlyPieces[colorToMove], pieces[colorToMoveInverse],
				allPieces ^ Util.POWER_LOOKUP[fromIndex] ^ Util.POWER_LOOKUP[toIndex]);
	}

	public boolean isLegalKingMove(final int fromIndex, final int toIndex) {
		final long fromToMask = Util.POWER_LOOKUP[fromIndex] ^ Util.POWER_LOOKUP[toIndex];
		return !CheckUtil.isInCheckIncludingKing(Long.numberOfTrailingZeros(pieces[colorToMove][KING] ^ fromToMask), colorToMove, friendlyPieces[colorToMove],
				pieces[colorToMoveInverse], allPieces ^ fromToMask);
	}

	public boolean isLegalAttackMove(final int fromIndex, final int toIndex) {

		System.arraycopy(pieces[colorToMoveInverse], 0, temporaryEnemyPieces, 0, 7);
		temporaryEnemyPieces[pieceIndexes[toIndex]] ^= Util.POWER_LOOKUP[toIndex];

		/* Check if is in check */
		return !CheckUtil.isInCheck(kingIndex[colorToMove], colorToMove, friendlyPieces[colorToMove], temporaryEnemyPieces,
				allPieces ^ Util.POWER_LOOKUP[fromIndex]);
	}

	public boolean isLegalKingAttackMove(final int fromIndex, final int toIndex) {

		final long fromToMask = Util.POWER_LOOKUP[fromIndex] ^ Util.POWER_LOOKUP[toIndex];

		System.arraycopy(pieces[colorToMoveInverse], 0, temporaryEnemyPieces, 0, 7);
		temporaryEnemyPieces[pieceIndexes[toIndex]] ^= Util.POWER_LOOKUP[toIndex];
		return !CheckUtil.isInCheckIncludingKing(Long.numberOfTrailingZeros(pieces[colorToMove][KING] ^ fromToMask), colorToMove, friendlyPieces[colorToMove],
				temporaryEnemyPieces, allPieces ^ fromToMask);

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
		final boolean isInCheck = CheckUtil.isInCheck(this);

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
			if ((StaticMoves.PAWN_MOVES_2[colorToMove][fromIndex] & Util.POWER_LOOKUP[toIndex]) != 0
					&& (allPieces & Util.POWER_LOOKUP[fromIndex + ChessConstants.COLOR_FACTOR_8[colorToMove]]) != 0) {
				return false;
			}
			break;

		case NIGHT:
			break;

		case BISHOP:
			if ((MagicUtil.getBishopMoves(fromIndex, allPieces, friendlyPieces[colorToMove]) & Util.POWER_LOOKUP[toIndex]) == 0) {
				return false;
			}
			break;
		case ROOK:
			if ((MagicUtil.getRookMoves(fromIndex, allPieces, friendlyPieces[colorToMove]) & Util.POWER_LOOKUP[toIndex]) == 0) {
				return false;
			}
			break;

		case QUEEN:
			if (((MagicUtil.getBishopMoves(fromIndex, allPieces, friendlyPieces[colorToMove])
					| MagicUtil.getRookMoves(fromIndex, allPieces, friendlyPieces[colorToMove])) & Util.POWER_LOOKUP[toIndex]) == 0) {
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
			return isLegalKingMove(fromIndex, toIndex);

		}

		// check legality (if in check or piece is pinned)
		if (checkingPieces != 0) {
			return isLegalMove(fromIndex, toIndex);
		}
		if ((pinnedPieces & Util.POWER_LOOKUP[fromIndex]) == 0) {
			return true;
		}
		return isLegalMove(fromIndex, toIndex);
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

}