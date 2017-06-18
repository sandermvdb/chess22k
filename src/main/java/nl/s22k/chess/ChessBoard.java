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
	public static final long[] zkEndGame = new long[2];

	/** color, piece */
	public final long[][] pieces = new long[2][7];
	public final long[] friendlyPieces = new long[2];
	private final long[] temporaryEnemyPieces = new long[7];

	/** 4 bits: white-king,white-queen,black-king,black-queen */
	public int castlingRights;
	public int psqtScore;
	public int colorToMove, colorToMoveInverse;
	public int epIndex;

	public long allPieces, emptySpaces;
	public long zobristKey, pawnZobristKey;
	public long checkingPieces;

	/** which piece is on which square */
	public final int[] pieceIndexes = new int[64];
	public final int[] kingIndex = new int[2];
	public final long[] pinnedPieces = new long[2];
	public final boolean[] isEndGame = new boolean[2];

	public int moveCounter = 0;
	public final int[] psqtScoreHistory = new int[EngineConstants.MAX_DEPTH];
	public final int[] castlingHistory = new int[EngineConstants.MAX_DEPTH];
	public final int[] epIndexHistory = new int[EngineConstants.MAX_DEPTH];
	public final long[] zobristKeyHistory = new long[EngineConstants.MAX_DEPTH];
	public final long[] pawnZobristKeyHistory = new long[EngineConstants.MAX_DEPTH];
	public final long[] checkingPiecesHistory = new long[EngineConstants.MAX_DEPTH];
	public final long[][] pinnedPiecesHistory = new long[2][EngineConstants.MAX_DEPTH];

	// evaluation values
	// borrowed from Ed Schroder
	// k,q,r,bn,p
	public int[][] attackBoards = new int[2][64];
	public int[] passerFiles = new int[2];
	public int[] protectedPasserFiles = new int[2];

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
	 * Constructor which only initializes the zobrist-keys
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
		zkEndGame[WHITE] = r.nextLong();
		zkEndGame[BLACK] = r.nextLong();
		zkWhiteToMove = r.nextLong();
	}

	@Override
	public String toString() {
		return ChessBoardUtil.toString(this);
	}

	public boolean hasOnlyPawns(final int color) {
		return (pieces[color][ROOK] | pieces[color][BISHOP] | pieces[color][QUEEN] | pieces[color][NIGHT]) == 0;
	}

	public boolean isEndGame(int color) {

		// TODO whose endgame?
		color = 1 - color;

		// - 0 queens, 1 rook and 3 little pieces
		// - 0 queens, 2 rooks
		// - 1 queen
		if (pieces[color][QUEEN] == 0) {
			if (Long.bitCount(pieces[color][ROOK]) < 2) {
				return Long.bitCount(pieces[color][BISHOP] | pieces[color][NIGHT]) <= 3;
			} else {
				return (pieces[color][BISHOP] | pieces[color][NIGHT]) == 0;
			}
		} else {
			return pieces[color][ROOK] == 0 && Long.bitCount(pieces[color][BISHOP] | pieces[color][NIGHT]) < 2;
		}
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
		if (Long.bitCount(allPieces) > 3) {
			return false;
		}
		if (Long.bitCount(allPieces) == 2) {
			// KK
			return true;
		}

		// KKB or KKN?
		return pieces[WHITE][BISHOP] != 0 || pieces[BLACK][BISHOP] != 0 || pieces[WHITE][NIGHT] != 0 || pieces[BLACK][NIGHT] != 0;

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

	public void doNullMove() {

		// set history values
		psqtScoreHistory[moveCounter] = psqtScore;
		castlingHistory[moveCounter] = castlingRights;
		epIndexHistory[moveCounter] = epIndex;
		zobristKeyHistory[moveCounter] = zobristKey;
		pawnZobristKeyHistory[moveCounter] = pawnZobristKey;
		pinnedPiecesHistory[WHITE][moveCounter] = pinnedPieces[WHITE];
		pinnedPiecesHistory[BLACK][moveCounter] = pinnedPieces[BLACK];
		checkingPiecesHistory[moveCounter] = checkingPieces;
		moveCounter++;

		zobristKey ^= zkEPIndex[epIndex] ^ zkWhiteToMove;
		epIndex = 0;

		changeSideToMove();

		if (EngineConstants.ASSERT) {
			ChessBoardTestUtil.testValues(this);
		}
	}

	public void undoNullMove() {
		// reset history values
		moveCounter--;
		epIndex = epIndexHistory[moveCounter];
		zobristKey = zobristKeyHistory[moveCounter];
		psqtScore = psqtScoreHistory[moveCounter];
		castlingRights = castlingHistory[moveCounter];
		pawnZobristKey = pawnZobristKeyHistory[moveCounter];
		pinnedPieces[WHITE] = pinnedPiecesHistory[WHITE][moveCounter];
		pinnedPieces[BLACK] = pinnedPiecesHistory[BLACK][moveCounter];
		checkingPieces = checkingPiecesHistory[moveCounter];

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

		// set history values
		psqtScoreHistory[moveCounter] = psqtScore;
		castlingHistory[moveCounter] = castlingRights;
		epIndexHistory[moveCounter] = epIndex;
		zobristKeyHistory[moveCounter] = zobristKey;
		pawnZobristKeyHistory[moveCounter] = pawnZobristKey;
		checkingPiecesHistory[moveCounter] = checkingPieces;
		pinnedPiecesHistory[WHITE][moveCounter] = pinnedPieces[WHITE];
		pinnedPiecesHistory[BLACK][moveCounter] = pinnedPieces[BLACK];
		moveCounter++;

		zobristKey ^= zkEPIndex[epIndex] ^ zkPieceValues[fromIndex][colorToMove][sourcePieceIndex] ^ zkPieceValues[toIndex][colorToMove][sourcePieceIndex]
				^ zkWhiteToMove;
		epIndex = 0;

		friendlyPieces[colorToMove] ^= fromToMask;
		pieceIndexes[fromIndex] = EMPTY;
		pieceIndexes[toIndex] = sourcePieceIndex;
		pieces[colorToMove][sourcePieceIndex] ^= fromToMask;

		switch (sourcePieceIndex) {
		case PAWN:
			pawnZobristKey ^= zkPieceValues[fromIndex][colorToMove][sourcePieceIndex];
			if (MoveUtil.isPromotion(move)) {
				pieces[colorToMove][sourcePieceIndex] ^= toMask;
				pieces[colorToMove][MoveUtil.getMoveType(move)] |= toMask;
				pieceIndexes[toIndex] = MoveUtil.getMoveType(move);
				zobristKey ^= zkPieceValues[toIndex][colorToMove][sourcePieceIndex] ^ zkPieceValues[toIndex][colorToMove][MoveUtil.getMoveType(move)];

				switch (MoveUtil.getMoveType(move)) {
				case MoveUtil.PROMOTION_B:
					psqtScore += EvalConstants.PSQT_BISHOP[colorToMove][toIndex];
					break;
				case MoveUtil.PROMOTION_N:
					psqtScore += EvalConstants.PSQT_KNIGHT[colorToMove][toIndex];
					break;
				case MoveUtil.PROMOTION_R:
					psqtScore += EvalConstants.PSQT_ROOK[colorToMove][toIndex];
				}

				// update other players endgame
				if (isEndGame(colorToMoveInverse) != isEndGame[colorToMoveInverse]) {
					isEndGame[colorToMoveInverse] = !isEndGame[colorToMoveInverse];
					pawnZobristKey ^= zkEndGame[colorToMoveInverse];
					psqtScore = EvalUtil.calculatePositionScores(this);

				}
			} else {
				pawnZobristKey ^= zkPieceValues[toIndex][colorToMove][sourcePieceIndex];
				// check 2-move
				if (ChessConstants.ROOK_IN_BETWEEN[fromIndex][toIndex] != 0) {
					epIndex = Long.numberOfTrailingZeros(ChessConstants.ROOK_IN_BETWEEN[fromIndex][toIndex]);
					zobristKey ^= zkEPIndex[epIndex];
				}
			}
			break;

		case NIGHT:
			psqtScore += EvalConstants.PSQT_KNIGHT[colorToMove][toIndex] - EvalConstants.PSQT_KNIGHT[colorToMove][fromIndex];
			break;

		case BISHOP:
			psqtScore += EvalConstants.PSQT_BISHOP[colorToMove][toIndex] - EvalConstants.PSQT_BISHOP[colorToMove][fromIndex];
			break;

		case ROOK:
			psqtScore += EvalConstants.PSQT_ROOK[colorToMove][toIndex] - EvalConstants.PSQT_ROOK[colorToMove][fromIndex];
			zobristKey ^= zkCastling[castlingRights];
			CastlingUtil.setRookMovedOrAttackedCastlingRights(this, fromIndex);
			zobristKey ^= zkCastling[castlingRights];
			break;

		case KING:
			kingIndex[colorToMove] = toIndex;
			if (isEndGame[colorToMove]) {
				psqtScore += EvalConstants.PSQT_KING_ENDGAME[colorToMove][toIndex]
						- EvalConstants.PSQT_KING_ENDGAME[colorToMove][fromIndex];
			} else {
				psqtScore += EvalConstants.PSQT_KING[colorToMove][toIndex] - EvalConstants.PSQT_KING[colorToMove][fromIndex];
			}
			if (MoveUtil.getMoveType(move) == MoveUtil.CASTLING) {
				CastlingUtil.castleRookUpdateKeyAndPsqt(this, toIndex);
			}
			zobristKey ^= zkCastling[castlingRights];
			CastlingUtil.setKingMovedCastlingRights(this, fromIndex);
			zobristKey ^= zkCastling[castlingRights];
		}

		// piece hit?
		if (attackedPieceIndex != EMPTY) {

			switch (attackedPieceIndex) {

			case PAWN:
				if (MoveUtil.getMoveType(move) == MoveUtil.EP) {
					toIndex += ChessConstants.COLOR_FACTOR_8[colorToMoveInverse];
					pieceIndexes[toIndex] = EMPTY;
					toMask = Util.POWER_LOOKUP[toIndex];
				}
				pawnZobristKey ^= zkPieceValues[toIndex][colorToMoveInverse][attackedPieceIndex];
				break;

			case NIGHT:
				psqtScore -= EvalConstants.PSQT_KNIGHT[colorToMoveInverse][toIndex];
				break;

			case BISHOP:
				psqtScore -= EvalConstants.PSQT_BISHOP[colorToMoveInverse][toIndex];
				break;

			case ROOK:
				psqtScore -= EvalConstants.PSQT_ROOK[colorToMoveInverse][toIndex];
				zobristKey ^= zkCastling[castlingRights];
				CastlingUtil.setRookMovedOrAttackedCastlingRights(this, toIndex);
				zobristKey ^= zkCastling[castlingRights];
				break;

			}

			friendlyPieces[colorToMoveInverse] ^= toMask;
			pieces[colorToMoveInverse][attackedPieceIndex] ^= toMask;
			zobristKey ^= zkPieceValues[toIndex][colorToMoveInverse][attackedPieceIndex];

			// update current players endgame
			if (isEndGame(colorToMove) != isEndGame[colorToMove]) {
				isEndGame[colorToMove] = !isEndGame[colorToMove];
				pawnZobristKey ^= zkEndGame[colorToMove];
				psqtScore = EvalUtil.calculatePositionScores(this);
			}
		}

		allPieces = friendlyPieces[colorToMove] | friendlyPieces[colorToMoveInverse];
		emptySpaces = ~allPieces;
		changeSideToMove();

		// update checking pieces
		switch (sourcePieceIndex) {
		case PAWN:
			if (MoveUtil.getMoveType(move) == MoveUtil.PROMOTION_N) {
				checkingPieces = CheckUtil.getCheckingPieces(this);
			} else {
				checkingPieces = CheckUtil.getCheckingPiecesWithoutKnight(this);
			}
			break;
		case NIGHT:
			checkingPieces = CheckUtil.getCheckingPiecesWithoutPawn(this);
			break;
		case BISHOP:
		case ROOK:
		case QUEEN:
		case KING:
			checkingPieces = CheckUtil.getCheckingPiecesWithoutKnightAndPawn(this);
		}

		// TODO can this be done iteratively?
		updatePinnedPieces(WHITE);
		updatePinnedPieces(BLACK);

		if (EngineConstants.ASSERT) {
			ChessBoardTestUtil.testValues(this);
		}

	}

	public void updatePinnedPieces(final int color) {

		pinnedPieces[color] = 0;

		final int colorInverse = 1 - color;
		long checkedPinnedPiece;
		long piece;

		// bishop and queen
		piece = pieces[colorInverse][BISHOP] | pieces[colorInverse][QUEEN];
		while (piece != 0) {
			checkedPinnedPiece = ChessConstants.BISHOP_IN_BETWEEN[kingIndex[color]][Long.numberOfTrailingZeros(piece)] & allPieces;
			if (Long.bitCount(checkedPinnedPiece) == 1) {
				pinnedPieces[color] |= checkedPinnedPiece & friendlyPieces[color];
			}
			piece &= piece - 1;
		}

		// rook and queen
		piece = pieces[colorInverse][ROOK] | pieces[colorInverse][QUEEN];
		while (piece != 0) {
			checkedPinnedPiece = ChessConstants.ROOK_IN_BETWEEN[kingIndex[color]][Long.numberOfTrailingZeros(piece)] & allPieces;
			if (Long.bitCount(checkedPinnedPiece) == 1) {
				pinnedPieces[color] |= checkedPinnedPiece & friendlyPieces[color];
			}
			piece &= piece - 1;
		}
	}

	public void undoMove(int move) {

		final int fromIndex = MoveUtil.getFromIndex(move);
		final int toIndex = MoveUtil.getToIndex(move);
		final long fromMask = Util.POWER_LOOKUP[fromIndex];
		final long toMask = Util.POWER_LOOKUP[toIndex];
		final long fromToMask = fromMask ^ toMask;
		final int sourcePieceIndex = MoveUtil.getSourcePieceIndex(move);
		final int attackedPieceIndex = MoveUtil.getAttackedPieceIndex(move);

		// reset history values
		moveCounter--;
		psqtScore = psqtScoreHistory[moveCounter];
		epIndex = epIndexHistory[moveCounter];
		castlingRights = castlingHistory[moveCounter];
		zobristKey = zobristKeyHistory[moveCounter];
		pawnZobristKey = pawnZobristKeyHistory[moveCounter];
		checkingPieces = checkingPiecesHistory[moveCounter];
		pinnedPieces[WHITE] = pinnedPiecesHistory[WHITE][moveCounter];
		pinnedPieces[BLACK] = pinnedPiecesHistory[BLACK][moveCounter];

		// undo move
		friendlyPieces[colorToMoveInverse] ^= fromToMask;
		pieceIndexes[fromIndex] = sourcePieceIndex;
		pieceIndexes[toIndex] = attackedPieceIndex;
		pieces[colorToMoveInverse][sourcePieceIndex] ^= fromToMask;

		switch (sourcePieceIndex) {
		case PAWN:
			if (MoveUtil.isPromotion(move)) {
				pieces[colorToMoveInverse][PAWN] ^= toMask;
				pieces[colorToMoveInverse][MoveUtil.getMoveType(move)] ^= toMask;
				isEndGame[colorToMove] = isEndGame(colorToMove);
			}
			break;
		case KING:
			if (MoveUtil.getMoveType(move) == MoveUtil.CASTLING) {
				CastlingUtil.uncastleRook(this, toIndex);
			}
			kingIndex[colorToMoveInverse] = fromIndex;
		}

		// undo hit
		if (attackedPieceIndex != 0) {
			if (MoveUtil.getMoveType(move) == MoveUtil.EP) {
				pieces[colorToMove][PAWN] |= Util.POWER_LOOKUP[toIndex + ChessConstants.COLOR_FACTOR_8[colorToMove]];
				friendlyPieces[colorToMove] |= Util.POWER_LOOKUP[toIndex + ChessConstants.COLOR_FACTOR_8[colorToMove]];
				pieceIndexes[toIndex] = EMPTY;
				pieceIndexes[toIndex + ChessConstants.COLOR_FACTOR_8[colorToMove]] = PAWN;
			} else {
				pieces[colorToMove][attackedPieceIndex] |= toMask;
				friendlyPieces[colorToMove] |= toMask;
			}
			// update current players endgame
			isEndGame[colorToMoveInverse] = isEndGame(colorToMoveInverse);
		}

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
			if (MoveUtil.getMoveType(move) == MoveUtil.CASTLING) {
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
		if ((pinnedPieces[colorToMove] & Util.POWER_LOOKUP[fromIndex]) == 0) {
			return true;
		}
		return isLegalMove(fromIndex, toIndex);
	}

	public boolean isValidMove(int move) {
		if (MoveUtil.getAttackedPieceIndex(move) == 0 && !MoveUtil.isPromotion(move)) {
			return isValidQuietMove(move);
		}

		if (MoveUtil.isPromotion(move) || MoveUtil.getMoveType(move) == MoveUtil.CASTLING || MoveUtil.getMoveType(move) == MoveUtil.EP) {
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