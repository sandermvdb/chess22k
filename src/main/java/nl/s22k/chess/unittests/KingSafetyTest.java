package nl.s22k.chess.unittests;

import org.junit.Test;

import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.ChessBoardUtil;
import nl.s22k.chess.eval.EvalUtil;
import nl.s22k.chess.eval.KingSafetyEval;

public class KingSafetyTest {

	@Test
	public void onePieceAttackingTest() {
		System.out.println("One piece attacking test");
		String fen = "rnbqkbnr/pppppppp/8/8/8/4Q3/PPPPPPPP/RNB1KBNR w KQkq - 0 1 ";
		ChessBoard cb = ChessBoardUtil.getNewCB(fen);
		EvalUtil.calculateMobilityScoresAndSetAttackBoards(cb);
		System.out.println(KingSafetyEval.calculateKingSafetyScores(cb));
	}

	@Test
	public void twoPiecesAttackingTest() {
		System.out.println("Two pieces attacking test");
		String fen = "rnbqkbnr/pppppppp/8/5N2/8/4Q3/PPPPPPPP/RNB1KB1R w KQkq - 0 1 ";
		ChessBoard cb = ChessBoardUtil.getNewCB(fen);
		EvalUtil.calculateMobilityScoresAndSetAttackBoards(cb);
		System.out.println(KingSafetyEval.calculateKingSafetyScores(cb));
	}

	@Test
	public void threePiecesAttackingTest() {
		System.out.println("Three pieces attacking test");
		String fen = "rnbqkbnr/pppppppp/8/1B3N2/8/4Q3/PPPPPPPP/RNB1K2R w KQkq - 0 1 ";
		ChessBoard cb = ChessBoardUtil.getNewCB(fen);
		EvalUtil.calculateMobilityScoresAndSetAttackBoards(cb);
		System.out.println(KingSafetyEval.calculateKingSafetyScores(cb));
	}

	@Test
	public void fourPiecesAttackingTest() {
		System.out.println("Four pieces attacking test");
		String fen = "rnbqkbnr/pppppppp/8/1B3N2/8/3RQ3/PPPPPPPP/RNB1K3 w Qkq - 0 1 ";
		ChessBoard cb = ChessBoardUtil.getNewCB(fen);
		EvalUtil.calculateMobilityScoresAndSetAttackBoards(cb);
		System.out.println(KingSafetyEval.calculateKingSafetyScores(cb));
	}

	@Test
	public void randomTest() {
		System.out.println("Random test");
		String fen = "2r2rk1/1p2b1p1/4p2p/p3Pp2/2P1bQ1P/1N3P2/PP3qP1/K1R2B1R w - - 1 25 ";
		ChessBoard cb = ChessBoardUtil.getNewCB(fen);
		EvalUtil.calculateMobilityScoresAndSetAttackBoards(cb);
		System.out.println(KingSafetyEval.calculateKingSafetyScores(cb));
	}
}
