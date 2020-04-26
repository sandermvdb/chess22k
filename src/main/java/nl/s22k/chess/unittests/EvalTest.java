package nl.s22k.chess.unittests;

import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.ChessBoardInstances;
import nl.s22k.chess.ChessBoardUtil;
import nl.s22k.chess.eval.EvalUtil;
import nl.s22k.chess.search.ThreadData;

public class EvalTest {

	public static void main(String[] args) {
		ChessBoard cb = ChessBoardInstances.get(0);
		ChessBoardUtil.setFen("1r1q1rk1/2p1npb1/b3p1p1/p5N1/1ppPB2R/P1N1P1P1/1P2QPP1/2K4R w - - 0 20 ", cb);
		EvalUtil.calculateScore(cb, ThreadData.getInstance(0));
	}

}
