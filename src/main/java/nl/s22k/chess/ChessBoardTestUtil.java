package nl.s22k.chess;

import static nl.s22k.chess.ChessConstants.BLACK;
import static nl.s22k.chess.ChessConstants.KING;
import static nl.s22k.chess.ChessConstants.WHITE;

public class ChessBoardTestUtil {

	public static void testValues(ChessBoard cb, String method) {

		long iterativeZK = cb.zobristKey;
		long iterativeZKPawn = cb.pawnZobristKey;
		long iterativeWhitePieces = cb.friendlyPieces[WHITE];
		long iterativeBlackPieces = cb.friendlyPieces[BLACK];
		long iterativeAllPieces = cb.allPieces;
		long iterativeWhiteBishopAttacks = cb.bishopRayAttacks[WHITE];
		long iterativeBlackBishopAttacks = cb.bishopRayAttacks[BLACK];
		long iterativeWhiteRookAttacks = cb.rookRayAttacks[WHITE];
		long iterativeBlackRookAttacks = cb.rookRayAttacks[BLACK];
		long iterativeWhiteQueenAttacks = cb.queenRayAttacks[WHITE];
		long iterativeBlackQueenAttacks = cb.queenRayAttacks[BLACK];
		long pinnedPiecesWhite = cb.pinnedPieces[WHITE];
		long pinnedPiecesBlack = cb.pinnedPieces[BLACK];
		int iterativePsqt = cb.psqtScore;

		if (Long.numberOfTrailingZeros(cb.pieces[WHITE][KING]) != cb.kingIndex[WHITE]) {
			System.out.println(String.format("Incorrect white king-index in chessBoard.%s()", method));
		}
		if (Long.numberOfTrailingZeros(cb.pieces[BLACK][KING]) != cb.kingIndex[BLACK]) {
			System.out.println(String.format("Incorrect black king-index in chessBoard.%s()", method));
		}

		// endgame
		if (cb.isEndGame(WHITE) != cb.isEndGame[WHITE]) {
			System.out.println(String.format("Incorrect white endGame set in chessBoard.%s()", method));
		}
		if (cb.isEndGame(BLACK) != cb.isEndGame[BLACK]) {
			System.out.println(String.format("Incorrect white endGame set in chessBoard.%s()", method));
		}

		ChessBoardUtil.init(cb);

		// zobrist keys
		if (iterativeZK != cb.zobristKey) {
			System.out.println(String.format("Incorrect zobrist-key calculated in chessBoard.%s()", method));
		}
		if (iterativeZKPawn != cb.pawnZobristKey) {
			System.out.println(String.format("Incorrect pawn-zobrist-key calculated in chessBoard.%s()", method));
		}

		// pinned-pieces
		if (pinnedPiecesWhite != cb.pinnedPieces[WHITE]) {
			System.out.println(String.format("Incorrect white pinned-pieces calculated in chessBoard.%s()", method));
		}
		if (pinnedPiecesBlack != cb.pinnedPieces[BLACK]) {
			System.out.println(String.format("Incorrect black pinned-pieces calculated in chessBoard.%s()", method));
		}

		// attack-pieces
		if (iterativeBlackBishopAttacks != cb.bishopRayAttacks[BLACK]) {
			System.out.println(String.format("Incorrect black bishop attacks calculated in chessBoard.%s()", method));
		}
		if (iterativeWhiteBishopAttacks != cb.bishopRayAttacks[WHITE]) {
			System.out.println(String.format("Incorrect white bishop attacks calculated in chessBoard.%s()", method));
		}
		if (iterativeBlackRookAttacks != cb.rookRayAttacks[BLACK]) {
			System.out.println(String.format("Incorrect black rook attacks calculated in chessBoard.%s()", method));
		}
		if (iterativeWhiteRookAttacks != cb.rookRayAttacks[WHITE]) {
			System.out.println(String.format("Incorrect white rook attacks calculated in chessBoard.%s()", method));
		}
		if (iterativeBlackQueenAttacks != cb.queenRayAttacks[BLACK]) {
			System.out.println(String.format("Incorrect black queen attacks calculated in chessBoard.%s()", method));
		}
		if (iterativeWhiteQueenAttacks != cb.queenRayAttacks[WHITE]) {
			System.out.println(String.format("Incorrect white queen attacks calculated in chessBoard.%s()", method));
		}

		// combined pieces
		if (iterativeWhitePieces != cb.friendlyPieces[WHITE]) {
			System.out.println(String.format("Incorrect whitePieces calculated in chessBoard.%s()", method));
		}
		if (iterativeBlackPieces != cb.friendlyPieces[BLACK]) {
			System.out.println(String.format("Incorrect blackPieces calculated in chessBoard.%s()", method));
		}
		if (iterativeAllPieces != cb.allPieces) {
			System.out.println(String.format("Incorrect allPieces calculated in chessBoard.%s()", method));
		}
		if ((iterativeBlackPieces & iterativeWhitePieces) != 0) {
			System.out.println(String.format("Overlapping pieces calculated in chessBoard.%s()", method));
		}

		// psqt
		if (iterativePsqt != cb.psqtScore) {
			System.out.println(String.format("Incorrect psqt calculated in chessBoard.%s(): %s, %s", method, iterativePsqt, cb.psqtScore));
		}

		// piece-indexes
		int[] iterativePieceScores = new int[64];
		System.arraycopy(cb.pieceIndexes, 0, iterativePieceScores, 0, 64);
		for (int i = 0; i < 64; i++) {
			if (iterativePieceScores[i] != cb.pieceIndexes[i]) {
				System.out.println(String.format("Incorrect pieceScores calculated in chessBoard.%s()", method));
			}
		}
	}

}
