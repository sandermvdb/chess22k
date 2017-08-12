package nl.s22k.test.chess;

import org.junit.Test;

import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.ChessBoardUtil;
import nl.s22k.chess.move.MagicUtil;
import nl.s22k.chess.search.NegamaxUtil;
import nl.s22k.chess.search.TimeUtil;
import nl.s22k.chess.unittests.MainTest;

public class CoberturaTest {

	@Test
	public void doTest() {
		MagicUtil.init();

		ChessBoard cb = ChessBoardUtil.getNewCB(MainTest.FEN_STANDARD_MIDDLEGAME);

		/* time-managed */
		TimeUtil.setTimeWindow(300000, 0, 0);
		NegamaxUtil.start(cb);
	}

}
