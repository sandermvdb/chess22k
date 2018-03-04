package nl.s22k.chess;

import static nl.s22k.chess.ChessConstants.BLACK;
import static nl.s22k.chess.ChessConstants.KING;
import static nl.s22k.chess.ChessConstants.WHITE;
import static org.junit.Assert.assertEquals;

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
		int phase = cb.phase;
		long materialKey = cb.materialKey;
		System.arraycopy(cb.pieceIndexes, 0, testPieceIndexes, 0, cb.pieceIndexes.length);

		assertEquals(Long.numberOfTrailingZeros(cb.pieces[WHITE][KING]), cb.kingIndex[WHITE]);
		assertEquals(Long.numberOfTrailingZeros(cb.pieces[BLACK][KING]), cb.kingIndex[BLACK]);

		ChessBoardUtil.init(cb);

		// zobrist keys
		assertEquals(iterativeZK, cb.zobristKey);
		assertEquals(iterativeZKPawn, cb.pawnZobristKey);

		// king area
		assertEquals(whiteKingArea, cb.kingArea[WHITE]);
		assertEquals(blackKingArea, cb.kingArea[BLACK]);

		// pinned and discovered pieces
		assertEquals(pinnedPieces, cb.pinnedPieces);
		assertEquals(discoveredPieces, cb.discoveredPieces);

		// combined pieces
		assertEquals(iterativeWhitePieces, cb.friendlyPieces[WHITE]);
		assertEquals(iterativeBlackPieces, cb.friendlyPieces[BLACK]);
		assertEquals(iterativeAllPieces, cb.allPieces);
		assertEquals((iterativeBlackPieces & iterativeWhitePieces), 0);

		// psqt
		assertEquals(iterativePsqt, cb.psqtScore);
		assertEquals(iterativePsqtEg, cb.psqtScoreEg);

		// piece-indexes
		for (int i = 0; i < testPieceIndexes.length; i++) {
			assertEquals(testPieceIndexes[i], cb.pieceIndexes[i]);
		}

		assertEquals(phase, cb.phase);
		assertEquals(materialKey, cb.materialKey);
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
