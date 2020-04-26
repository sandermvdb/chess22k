package nl.s22k.chess;

import static nl.s22k.chess.ChessConstants.BLACK;
import static nl.s22k.chess.ChessConstants.KING;
import static nl.s22k.chess.ChessConstants.WHITE;

import nl.s22k.chess.eval.EvalUtil;
import nl.s22k.chess.eval.KingSafetyEval;
import nl.s22k.chess.eval.PassedPawnEval;

public class ChessBoardTestUtil {

	public static void compareScores(final ChessBoard cb) {
		ChessBoard testCb = ChessBoardTestUtil.getHorizontalMirroredCb(cb);
		ChessBoardTestUtil.compareScores(cb, testCb, 1);

		testCb = ChessBoardTestUtil.getVerticalMirroredCb(cb);
		ChessBoardTestUtil.compareScores(cb, testCb, -1);
	}

	private static void compareScores(final ChessBoard cb1, final ChessBoard cb2, final int factor) {

		EvalUtil.calculateMobilityScoresAndSetAttacks(cb1);
		EvalUtil.calculateMobilityScoresAndSetAttacks(cb2);

		if (KingSafetyEval.calculateScores(cb2) != KingSafetyEval.calculateScores(cb1) * factor) {
			System.out.println("Unequal king-safety: " + KingSafetyEval.calculateScores(cb1) + " " + KingSafetyEval.calculateScores(cb2) * factor);
		}
		if (EvalUtil.calculatePositionScores(cb1) != EvalUtil.calculatePositionScores(cb2) * factor) {
			System.out.println("Unequal position score: " + EvalUtil.calculatePositionScores(cb1) + " " + EvalUtil.calculatePositionScores(cb2) * factor);
		}
		// if (EvalUtil.getPawnScores(cb1) != EvalUtil.getPawnScores(cb2) * factor) {
		// System.out.println("Unequal pawns: " + EvalUtil.getPawnScores(cb1) + " " + EvalUtil.getPawnScores(cb2) *
		// factor);
		// }
		// if (EvalUtil.getImbalances(cb1) != EvalUtil.getImbalances(cb2) * factor) {
		// System.out.println("Unequal imbalances: " + EvalUtil.getImbalances(cb1) + " " + EvalUtil.getImbalances(cb2) *
		// factor);
		// }
		if (EvalUtil.calculateOthers(cb2) != EvalUtil.calculateOthers(cb1) * factor) {
			System.out.println("Unequal others: " + EvalUtil.calculateOthers(cb1) + " " + EvalUtil.calculateOthers(cb2) * factor);
		}
		if (EvalUtil.calculateThreats(cb2) != EvalUtil.calculateThreats(cb1) * factor) {
			System.out.println("Unequal threats: " + EvalUtil.calculateThreats(cb1) + " " + EvalUtil.calculateThreats(cb2) * factor);
		}
		if (PassedPawnEval.calculateScores(cb1) != PassedPawnEval.calculateScores(cb2) * factor) {
			System.out.println("Unequal passed-pawns: " + PassedPawnEval.calculateScores(cb1) + " " + PassedPawnEval.calculateScores(cb2) * factor);
		}
	}

	public static void testValues(ChessBoard cb) {

		long iterativeZK = cb.zobristKey;
		long iterativeZKPawn = cb.pawnZobristKey;
		long iterativeAllPieces = cb.allPieces;
		int iterativePsqt = cb.psqtScore;
		int phase = cb.phase;
		long materialKey = cb.materialKey;
		int[] testPieceIndexes = new int[64];
		System.arraycopy(cb.pieceIndexes, 0, testPieceIndexes, 0, cb.pieceIndexes.length);

		Assert.isTrue(Long.numberOfTrailingZeros(cb.pieces[WHITE][KING]) == cb.kingIndex[WHITE]);
		Assert.isTrue(Long.numberOfTrailingZeros(cb.pieces[BLACK][KING]) == cb.kingIndex[BLACK]);

		ChessBoardUtil.init(cb);

		// zobrist keys
		Assert.isTrue(iterativeZK == cb.zobristKey);
		Assert.isTrue(iterativeZKPawn == cb.pawnZobristKey);

		// combined pieces
		Assert.isTrue(iterativeAllPieces == cb.allPieces);

		// psqt
		Assert.isTrue(iterativePsqt == cb.psqtScore);

		// piece-indexes
		for (int i = 0; i < testPieceIndexes.length; i++) {
			Assert.isTrue(testPieceIndexes[i] == cb.pieceIndexes[i]);
		}

		Assert.isTrue(phase == cb.phase);
		Assert.isTrue(materialKey == cb.materialKey);
	}

	private static ChessBoard getHorizontalMirroredCb(ChessBoard cb) {
		ChessBoard testCb = ChessBoardInstances.get(1);

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

	private static ChessBoard getVerticalMirroredCb(ChessBoard cb) {
		ChessBoard testCb = ChessBoardInstances.get(1);

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
