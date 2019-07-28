package nl.s22k.chess.unittests;

import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.ChessBoardUtil;
import nl.s22k.chess.eval.EvalUtil;

public class EvalTest {

	public static void main(String[] args) {
		ChessBoard cb = ChessBoardUtil.getNewCB("8/5pk1/Q2b2q1/7p/P1pP3P/2N2pP1/5K2/5R2 b - - 0 31 ");
		EvalUtil.calculateScore(cb);
	}

}
