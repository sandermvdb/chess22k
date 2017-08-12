package nl.s22k.chess;

import static nl.s22k.chess.ChessConstants.BLACK;
import static nl.s22k.chess.ChessConstants.KING;
import static nl.s22k.chess.ChessConstants.WHITE;

public class ChessBoardTestUtil {

	private static int[] testPieceIndexes = new int[64];

	public static void testValues(ChessBoard cb) {

		long iterativeZK = cb.zobristKey;
		long iterativeZKPawn = cb.pawnZobristKey;
		long iterativeWhitePieces = cb.friendlyPieces[WHITE];
		long iterativeBlackPieces = cb.friendlyPieces[BLACK];
		long iterativeAllPieces = cb.allPieces;
		long pinnedPiecesWhite = cb.pinnedPieces[WHITE];
		long pinnedPiecesBlack = cb.pinnedPieces[BLACK];
		int iterativePsqt = cb.psqtScore;
		int iterativePsqtEg = cb.psqtScoreEg;
		System.arraycopy(cb.pieceIndexes, 0, testPieceIndexes, 0, cb.pieceIndexes.length);

		assert Long.numberOfTrailingZeros(cb.pieces[WHITE][KING]) == cb.kingIndex[WHITE] : "Incorrect white king-index";
		assert Long.numberOfTrailingZeros(cb.pieces[BLACK][KING]) == cb.kingIndex[BLACK] : "Incorrect black king-index";

		ChessBoardUtil.init(cb);

		// zobrist keys
		assert iterativeZK == cb.zobristKey : "Incorrect zobrist-key";
		assert iterativeZKPawn == cb.pawnZobristKey : "Incorrect pawn-zobrist-key";

		// pinned-pieces
		assert pinnedPiecesWhite == cb.pinnedPieces[WHITE] : "Incorrect white pinned-pieces";
		assert pinnedPiecesBlack == cb.pinnedPieces[BLACK] : "Incorrect black pinned-pieces";

		// combined pieces
		assert iterativeWhitePieces == cb.friendlyPieces[WHITE] : "Incorrect whitePieces";
		assert iterativeBlackPieces == cb.friendlyPieces[BLACK] : "Incorrect blackPieces";
		assert iterativeAllPieces == cb.allPieces : "Incorrect allPieces";
		assert (iterativeBlackPieces & iterativeWhitePieces) == 0 : "Overlapping pieces";

		// psqt
		assert iterativePsqt == cb.psqtScore : "Incorrect psqt: " + iterativePsqt + " " + cb.psqtScore;
		assert iterativePsqtEg == cb.psqtScoreEg : "Incorrect psqt eg: " + iterativePsqtEg + " " + cb.psqtScoreEg;

		// piece-indexes
		for (int i = 0; i < testPieceIndexes.length; i++) {
			assert testPieceIndexes[i] == cb.pieceIndexes[i] : "Incorrect piece indexes";
		}
	}

	public static ChessBoard getHorizontalMirroredCb(ChessBoard cb) {
		ChessBoard testCb = ChessBoard.getTestInstance();

		for (int color = ChessConstants.WHITE; color <= ChessConstants.BLACK; color++) {
			for (int piece = ChessConstants.PAWN; piece <= ChessConstants.KING; piece++) {
				testCb.pieces[color][piece] = Util.mirrorHorizontal(cb.pieces[color][piece]);
			}
		}

		testCb.colorToMove = cb.colorToMove;
		ChessBoardUtil.init(testCb);
		testCb.moveCounter = cb.moveCounter;
		return testCb;
	}

	public static ChessBoard getVerticalMirroredCb(ChessBoard cb) {
		ChessBoard testCb = ChessBoard.getTestInstance();

		for (int piece = ChessConstants.PAWN; piece <= ChessConstants.KING; piece++) {
			testCb.pieces[WHITE][piece] = Util.mirrorVertical(cb.pieces[BLACK][piece]);
		}
		for (int piece = ChessConstants.PAWN; piece <= ChessConstants.KING; piece++) {
			testCb.pieces[BLACK][piece] = Util.mirrorVertical(cb.pieces[WHITE][piece]);
		}

		testCb.colorToMove = cb.colorToMoveInverse;
		ChessBoardUtil.init(testCb);
		testCb.moveCounter = cb.moveCounter;
		return testCb;
	}

}
