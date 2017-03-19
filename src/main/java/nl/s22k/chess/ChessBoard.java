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
import nl.s22k.chess.move.MoveGenerator;
import nl.s22k.chess.move.MoveUtil;
import nl.s22k.chess.move.StaticMoves;

public final class ChessBoard {

	private static ChessBoard instance = new ChessBoard();

	// zobrist-keys
	public long zkWhiteToMove;
	public final long[] zkCastling = new long[16];
	public final long[] zkEPIndex = new long[48];
	public final long[][][] zkPieceValues = new long[64][2][7];
	public final long[][] zkKingPosition = new long[2][3];
	public final long[] zkEndGame = new long[2];

	/** color, piece */
	public final long[][] pieces = new long[2][7];
	public final long[] friendlyPieces = new long[2];
	public long allPieces, emptySpaces;
	private long[] temporaryEnemyPieces = new long[7];

	public int colorToMove, colorToMoveInverse;
	public long zobristKey, pawnZobristKey;
	public long checkingPieces;
	public int epIndex;
	public int[] kingIndex = new int[2];
	public long[] pinnedPieces = new long[2];

	public int psqtScore;
	public int colorFactor;
	public final boolean[] isEndGame = new boolean[2];

	/** 4 bits: white-king,white-queen,black-king,black-queen */
	public int castlingRights;

	public int moveCounter = 0;
	public final int[] psqtScoreHistory = new int[ChessConstants.MAX_DEPTH];
	public final int[] castlingHistory = new int[ChessConstants.MAX_DEPTH];
	public final int[] epIndexHistory = new int[ChessConstants.MAX_DEPTH];
	public final long[] zobristKeyHistory = new long[ChessConstants.MAX_DEPTH];
	public final long[] pawnZobristKeyHistory = new long[ChessConstants.MAX_DEPTH];
	public final long[] checkingPiecesHistory = new long[ChessConstants.MAX_DEPTH];
	public final long[][] pinnedPiecesHistory = new long[2][ChessConstants.MAX_DEPTH];

	/** which piece is on which square */
	public final int[] pieceIndexes = new int[64];

	public static ChessBoard getInstance() {
		return instance;
	}

	/**
	 * Constructor which only initializes the zobrist-keys
	 */
	private ChessBoard() {
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

		for (int colorIndex = 0; colorIndex < 2; colorIndex++) {
			for (int kingIndex = 0; kingIndex < zkKingPosition[0].length; kingIndex++) {
				zkKingPosition[colorIndex][kingIndex] = r.nextLong();
			}
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
		color = color * -1 + 1;

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
			// witte pion op a-file en zwarte koning op a8 of b8
			if ((pieces[WHITE][PAWN] & ChessConstants.MASKS_FILE[7]) != 0) {
				return (pieces[BLACK][KING] & ChessConstants.MASK_A8_B8) != 0;
			}

			// witte pion op h-file en zwarte koning op g8 of h8
			if ((pieces[WHITE][PAWN] & ChessConstants.MASKS_FILE[0]) != 0) {
				return (pieces[BLACK][KING] & ChessConstants.MASK_G8_H8) != 0;
			}
		}

		else if (Long.bitCount(pieces[BLACK][PAWN]) == 1 && Long.bitCount(pieces[BLACK][BISHOP]) == 1) {
			// zwarte pion op a-file en witte koning op a1 of b1
			if ((pieces[BLACK][PAWN] & ChessConstants.MASKS_FILE[7]) != 0) {
				return (pieces[WHITE][KING] & ChessConstants.MASK_A1_B1) != 0;
			}

			// zwarte pion op h-file en witte koning op g1 of h1
			if ((pieces[BLACK][PAWN] & ChessConstants.MASKS_FILE[0]) != 0) {
				return (pieces[WHITE][KING] & ChessConstants.MASK_G1_H1) != 0;
			}
		}

		return false;
	}

	public void changeSideToMove() {
		colorToMove = colorToMoveInverse;
		colorToMoveInverse = colorToMove * -1 + 1;
		colorFactor *= -1;
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

		checkingPieces = CheckUtil.getCheckingPieces(this);

		if (EngineConstants.TEST_VALUES) {
			ChessBoardTestUtil.testValues(this, "doNullMove");
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

		if (EngineConstants.TEST_VALUES) {
			ChessBoardTestUtil.testValues(this, "undoNullMove");
		}
	}

	public void doMove(int move) {

		Statistics.moveCount++;

		final int fromIndex = MoveUtil.getFromIndex(move);
		int toIndex = MoveUtil.getToIndex(move);
		final long fromMask = Util.POWER_LOOKUP[fromIndex];
		long toMask = Util.POWER_LOOKUP[toIndex];
		final long fromToMask = fromMask ^ toMask;
		final int zkSourcePieceIndex = MoveUtil.getZKSourcePieceIndex(move);
		final int zkAttackedPieceIndex = MoveUtil.getZKAttackedPieceIndex(move);

		if (EngineConstants.TEST_VALUES) {
			if (zkAttackedPieceIndex == KING) {
				System.out.println("ChessBoard: Illegal move: king is being hit!");
			}
			if (zkAttackedPieceIndex > 0) {
				if ((Util.POWER_LOOKUP[toIndex] & friendlyPieces[colorToMove]) != 0) {
					System.out.println("ChessBoard: Hitting own piece!");
				}
			}
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

		zobristKey ^= zkEPIndex[epIndex] ^ zkPieceValues[fromIndex][colorToMove][zkSourcePieceIndex] ^ zkPieceValues[toIndex][colorToMove][zkSourcePieceIndex]
				^ zkWhiteToMove;
		epIndex = 0;

		friendlyPieces[colorToMove] ^= fromToMask;
		pieceIndexes[fromIndex] = EMPTY;
		pieceIndexes[toIndex] = zkSourcePieceIndex;

		switch (zkSourcePieceIndex) {
		case PAWN:
			pawnZobristKey ^= zkPieceValues[fromIndex][colorToMove][zkSourcePieceIndex];
			if (MoveUtil.isPromotion(move)) {
				pieces[colorToMove][zkSourcePieceIndex] ^= fromMask;
				if (MoveUtil.isNightPromotion(move)) {
					pieces[colorToMove][NIGHT] |= toMask;
					psqtScore += colorFactor * EvalConstants.KNIGHT_POSITION_SCORES[colorToMove][toIndex];
					zobristKey ^= zkPieceValues[toIndex][colorToMove][zkSourcePieceIndex] ^ zkPieceValues[toIndex][colorToMove][NIGHT];
					pieceIndexes[toIndex] = NIGHT;
				} else {
					pieces[colorToMove][QUEEN] |= toMask;
					zobristKey ^= zkPieceValues[toIndex][colorToMove][zkSourcePieceIndex] ^ zkPieceValues[toIndex][colorToMove][QUEEN];
					pieceIndexes[toIndex] = QUEEN;
				}
				// update other players endgame
				if (isEndGame(colorToMoveInverse) != isEndGame[colorToMoveInverse]) {
					isEndGame[colorToMoveInverse] = !isEndGame[colorToMoveInverse];
					pawnZobristKey ^= zkEndGame[colorToMoveInverse];
					psqtScore = EvalUtil.calculatePositionScores(this);

				}
			} else {
				pawnZobristKey ^= zkPieceValues[toIndex][colorToMove][zkSourcePieceIndex];
				pieces[colorToMove][zkSourcePieceIndex] ^= fromToMask;
				// check 2-move
				if (Math.abs(toIndex - fromIndex) == 16) {
					epIndex = fromIndex + MoveGenerator.PAWN_2_MOVE_IN_BETWEEN[colorToMove];
					zobristKey ^= zkEPIndex[epIndex];
				}
			}
			break;

		case NIGHT:
			pieces[colorToMove][zkSourcePieceIndex] ^= fromToMask;
			psqtScore += colorFactor
					* (EvalConstants.KNIGHT_POSITION_SCORES[colorToMove][toIndex] - EvalConstants.KNIGHT_POSITION_SCORES[colorToMove][fromIndex]);
			break;

		case BISHOP:
			pieces[colorToMove][zkSourcePieceIndex] ^= fromToMask;
			psqtScore += colorFactor
					* (EvalConstants.BISHOP_POSITION_SCORES[colorToMove][toIndex] - EvalConstants.BISHOP_POSITION_SCORES[colorToMove][fromIndex]);
			break;

		case ROOK:
			pieces[colorToMove][zkSourcePieceIndex] ^= fromToMask;
			psqtScore += colorFactor * (EvalConstants.ROOK_POSITION_SCORES[colorToMove][toIndex] - EvalConstants.ROOK_POSITION_SCORES[colorToMove][fromIndex]);
			zobristKey ^= zkCastling[castlingRights];
			CastlingUtil.setRookMovedOrAttackedCastlingRights(this, fromIndex);
			zobristKey ^= zkCastling[castlingRights];
			break;

		case QUEEN:
			pieces[colorToMove][zkSourcePieceIndex] ^= fromToMask;
			break;

		case KING:
			kingIndex[colorToMove] = toIndex;
			if (isEndGame[colorToMove]) {
				psqtScore += colorFactor * (EvalConstants.KING_POSITION_SCORES_ENDGAME[colorToMove][toIndex]
						- EvalConstants.KING_POSITION_SCORES_ENDGAME[colorToMove][fromIndex]);
			} else {
				psqtScore += colorFactor
						* (EvalConstants.KING_POSITION_SCORES[colorToMove][toIndex] - EvalConstants.KING_POSITION_SCORES[colorToMove][fromIndex]);
			}
			if (MoveUtil.isCastling(move)) {
				CastlingUtil.castleRookUpdateKeyAndPsqt(this, toIndex);
			}
			zobristKey ^= zkCastling[castlingRights];
			CastlingUtil.setKingMovedCastlingRights(this, fromIndex);
			zobristKey ^= zkCastling[castlingRights];
			pawnZobristKey ^= zkKingPosition[colorToMove][EvalConstants.getKingPositionIndex(colorToMove, fromIndex)]
					^ zkKingPosition[colorToMove][EvalConstants.getKingPositionIndex(colorToMove, toIndex)];
			pieces[colorToMove][zkSourcePieceIndex] ^= fromToMask;
		}

		// piece hit?
		switch (zkAttackedPieceIndex) {
		case EMPTY:
			break;

		case PAWN:
			if (MoveUtil.isEP(move)) {
				toIndex += MoveGenerator.PAWN_2_MOVE_IN_BETWEEN[colorToMoveInverse];
				toMask = Util.POWER_LOOKUP[toIndex];
			}
			zobristKey ^= zkPieceValues[toIndex][colorToMoveInverse][PAWN];
			pawnZobristKey ^= zkPieceValues[toIndex][colorToMoveInverse][zkAttackedPieceIndex];
			pieces[colorToMoveInverse][zkAttackedPieceIndex] ^= toMask;
			friendlyPieces[colorToMoveInverse] ^= toMask;
			break;

		case NIGHT:
			pieces[colorToMoveInverse][zkAttackedPieceIndex] ^= toMask;
			psqtScore += colorFactor * EvalConstants.KNIGHT_POSITION_SCORES[colorToMoveInverse][toIndex];
			zobristKey ^= zkPieceValues[toIndex][colorToMoveInverse][zkAttackedPieceIndex];
			friendlyPieces[colorToMoveInverse] ^= toMask;
			break;

		case BISHOP:
			pieces[colorToMoveInverse][zkAttackedPieceIndex] ^= toMask;
			psqtScore += colorFactor * EvalConstants.BISHOP_POSITION_SCORES[colorToMoveInverse][toIndex];
			zobristKey ^= zkPieceValues[toIndex][colorToMoveInverse][zkAttackedPieceIndex];
			friendlyPieces[colorToMoveInverse] ^= toMask;
			break;

		case ROOK:
			pieces[colorToMoveInverse][zkAttackedPieceIndex] ^= toMask;
			psqtScore += colorFactor * EvalConstants.ROOK_POSITION_SCORES[colorToMoveInverse][toIndex];
			zobristKey ^= zkCastling[castlingRights];
			CastlingUtil.setRookMovedOrAttackedCastlingRights(this, toIndex);
			zobristKey ^= zkCastling[castlingRights] ^ zkPieceValues[toIndex][colorToMoveInverse][zkAttackedPieceIndex];
			friendlyPieces[colorToMoveInverse] ^= toMask;
			break;

		case QUEEN:
			pieces[colorToMoveInverse][zkAttackedPieceIndex] ^= toMask;
			zobristKey ^= zkPieceValues[toIndex][colorToMoveInverse][zkAttackedPieceIndex];
			friendlyPieces[colorToMoveInverse] ^= toMask;
		}

		// update current players endgame
		if (zkAttackedPieceIndex != 0) {
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
		switch (zkSourcePieceIndex) {
		case PAWN:
			if (MoveUtil.isNightPromotion(move)) {
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

		if (EngineConstants.TEST_VALUES) {
			ChessBoardTestUtil.testValues(this, "doMove");
		}

	}

	public void updatePinnedPieces(final int color) {

		pinnedPieces[color] = 0;

		final int colorInverse = color * -1 + 1;
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
		final int zkSourcePieceIndex = MoveUtil.getZKSourcePieceIndex(move);
		final int zkAttackedPieceIndex = MoveUtil.getZKAttackedPieceIndex(move);

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

		friendlyPieces[colorToMoveInverse] ^= fromToMask;
		pieceIndexes[fromIndex] = zkSourcePieceIndex;
		pieceIndexes[toIndex] = zkAttackedPieceIndex;

		// undo move
		switch (zkSourcePieceIndex) {
		case PAWN:
			if (MoveUtil.isPromotion(move)) {
				pieces[colorToMoveInverse][PAWN] |= fromMask;
				if (MoveUtil.isNightPromotion(move)) {
					pieces[colorToMoveInverse][NIGHT] ^= toMask;
				} else {
					pieces[colorToMoveInverse][QUEEN] ^= toMask;
				}
				// update other players endgame
				isEndGame[colorToMove] = isEndGame(colorToMove);
			} else {
				pieces[colorToMoveInverse][zkSourcePieceIndex] ^= fromToMask;
			}
			break;
		case NIGHT:
			pieces[colorToMoveInverse][zkSourcePieceIndex] ^= fromToMask;
			break;
		case ROOK:
			pieces[colorToMoveInverse][zkSourcePieceIndex] ^= fromToMask;
			break;
		case BISHOP:
			pieces[colorToMoveInverse][zkSourcePieceIndex] ^= fromToMask;
			break;
		case QUEEN:
			pieces[colorToMoveInverse][zkSourcePieceIndex] ^= fromToMask;
			break;
		case KING:
			if (MoveUtil.isCastling(move)) {
				pieces[colorToMoveInverse][KING] = fromMask;
				CastlingUtil.uncastleRook(this, toIndex);
				// updateRookRayAttacks(colorToMoveInverse);
			} else {
				pieces[colorToMoveInverse][zkSourcePieceIndex] ^= fromToMask;
			}
			kingIndex[colorToMoveInverse] = fromIndex;
		}

		// undo hit
		if (zkAttackedPieceIndex != 0) {
			if (MoveUtil.isEP(move)) {
				pieces[colorToMove][PAWN] |= Util.POWER_LOOKUP[toIndex + MoveGenerator.PAWN_2_MOVE_IN_BETWEEN[colorToMove]];
				friendlyPieces[colorToMove] |= Util.POWER_LOOKUP[toIndex + MoveGenerator.PAWN_2_MOVE_IN_BETWEEN[colorToMove]];
				pieceIndexes[toIndex] = EMPTY;
				pieceIndexes[toIndex + MoveGenerator.PAWN_2_MOVE_IN_BETWEEN[colorToMove]] = PAWN;
			} else {
				pieces[colorToMove][zkAttackedPieceIndex] |= toMask;
				friendlyPieces[colorToMove] |= toMask;
			}
			// update current players endgame
			isEndGame[colorToMoveInverse] = isEndGame(colorToMoveInverse);
		}

		allPieces = friendlyPieces[colorToMove] | friendlyPieces[colorToMoveInverse];
		emptySpaces = ~allPieces;
		changeSideToMove();

		if (EngineConstants.TEST_VALUES) {
			ChessBoardTestUtil.testValues(this, "undoMove");
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
		pieces[colorToMoveInverse][PAWN] ^= Util.POWER_LOOKUP[epIndex + MoveGenerator.PAWN_2_MOVE_IN_BETWEEN[colorToMoveInverse]];
		allPieces = friendlyPieces[colorToMove]
				| friendlyPieces[colorToMoveInverse] ^ Util.POWER_LOOKUP[epIndex + MoveGenerator.PAWN_2_MOVE_IN_BETWEEN[colorToMoveInverse]];

		/* Check if is in check */
		final boolean isInCheck = CheckUtil.isInCheck(this);

		// undo-move and hit
		friendlyPieces[colorToMove] ^= fromToMask;
		pieces[colorToMoveInverse][PAWN] |= Util.POWER_LOOKUP[epIndex + MoveGenerator.PAWN_2_MOVE_IN_BETWEEN[colorToMoveInverse]];
		allPieces = friendlyPieces[colorToMove] | friendlyPieces[colorToMoveInverse];

		return !isInCheck;

	}

	public boolean isValidKillerMove(int move) {

		// check piece at from-position
		final int fromIndex = MoveUtil.getFromIndex(move);
		if ((pieces[colorToMove][MoveUtil.getZKSourcePieceIndex(move)] & Util.POWER_LOOKUP[fromIndex]) == 0) {
			return false;
		}

		// no piece should be at to-position
		final int toIndex = MoveUtil.getToIndex(move);
		if (pieceIndexes[toIndex] != EMPTY) {
			return false;
		}

		// check if move is possible
		switch (MoveUtil.getZKSourcePieceIndex(move)) {
		case PAWN:
			// 2-move
			if ((StaticMoves.PAWN_MOVES_2[colorToMove][fromIndex] & Util.POWER_LOOKUP[toIndex]) != 0
					&& (allPieces & Util.POWER_LOOKUP[fromIndex + MoveGenerator.PAWN_2_MOVE_IN_BETWEEN[colorToMove]]) != 0) {
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
			if (MoveUtil.isCastling(move)) {
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

}