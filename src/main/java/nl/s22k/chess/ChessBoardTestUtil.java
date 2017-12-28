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
		long pinnedPieces = cb.pinnedPieces;
		long discoveredPieces = cb.discoveredPieces;
		int iterativePsqt = cb.psqtScore;
		int iterativePsqtEg = cb.psqtScoreEg;
		long whiteKingArea = cb.kingArea[WHITE];
		long blackKingArea = cb.kingArea[BLACK];
		int whiteMajorPieces = cb.majorPieces[WHITE];
		int blackMajorPieces = cb.majorPieces[BLACK];
		int phase = cb.phase;
		int materialWithoutPawnScore = cb.materialWithoutPawnScore;
		System.arraycopy(cb.pieceIndexes, 0, testPieceIndexes, 0, cb.pieceIndexes.length);

		assert Long.numberOfTrailingZeros(cb.pieces[WHITE][KING]) == cb.kingIndex[WHITE] : "Incorrect white king-index";
		assert Long.numberOfTrailingZeros(cb.pieces[BLACK][KING]) == cb.kingIndex[BLACK] : "Incorrect black king-index";

		ChessBoardUtil.init(cb);

		// zobrist keys
		assert iterativeZK == cb.zobristKey : "Incorrect zobrist-key";
		assert iterativeZKPawn == cb.pawnZobristKey : "Incorrect pawn-zobrist-key";

		// king area
		assert whiteKingArea == cb.kingArea[WHITE] : "Incorrect white king area";
		assert blackKingArea == cb.kingArea[BLACK] : "Incorrect black king area";

		// pinned and discovered pieces
		assert pinnedPieces == cb.pinnedPieces : "Incorrect pinned-pieces";
		assert discoveredPieces == cb.discoveredPieces : "Incorrect discovered-pieces";

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

		// major pieces
		assert whiteMajorPieces == cb.majorPieces[WHITE] : "Incorrect white major pieces";
		assert blackMajorPieces == cb.majorPieces[BLACK] : "Incorrect black major pieces";

		assert phase == cb.phase : "Incorrect phase";
		assert materialWithoutPawnScore == cb.materialWithoutPawnScore : "Incorrect materialWithoutPawnScore";
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
