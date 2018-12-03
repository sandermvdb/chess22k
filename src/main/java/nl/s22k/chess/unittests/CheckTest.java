package nl.s22k.chess.unittests;

import org.junit.Test;

import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.ChessBoardUtil;
import nl.s22k.chess.ChessConstants;
import nl.s22k.chess.Statistics;
import nl.s22k.chess.move.MoveUtil;

public class CheckTest {

	// @Test
	// public void mateInOneTest() {
	// Statistics.reset();
	// ChessBoard cb = new ChessBoard("8/6pp/1R1Q4/5p2/2k1p3/P3P3/b4PPP/6K1 w - - 2 50 ");
	// NegamaxUtil nm = new NegamaxUtil();
	// nm.calculateBestMove(cb, 5);
	// Statistics.print();
	// }

	@Test
	public void doCheckTest() {
		Statistics.reset();
		ChessBoard cb = ChessBoardUtil.getNewCB("r1bq1bnr/3k4/1pnpppQp/p7/2Pp1PP1/N2K3B/PP2P2P/1RB3NR b - - 2 14");
		int blackMove = MoveUtil.createMove(60, 59, ChessConstants.QUEEN);
		int whiteMove = MoveUtil.createAttackMove(41, 59, ChessConstants.QUEEN, ChessConstants.QUEEN);
		cb.doMove(blackMove);
		cb.doMove(whiteMove);
		// System.out.println(MoveGenerator.getLegalMoves(cb));
	}

	@Test
	public void doPawnPromotionCheckTest() {
		Statistics.reset();
		ChessBoard cb = ChessBoardUtil.getNewCB("4k3/3P4/8/8/8/8/8/4K3 b - - 2 14");
		System.out.println("Should not be 0: " + cb.checkingPieces);
	}

}
