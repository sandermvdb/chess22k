package nl.s22k.test.chess;

import org.junit.Test;

import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.ChessBoardUtil;
import nl.s22k.chess.Statistics;
import nl.s22k.chess.engine.MainEngine;
import nl.s22k.chess.move.MagicUtil;
import nl.s22k.chess.search.NegamaxUtil;

public class CoberturaTest {

	@Test
	public void doTest() {
		MagicUtil.init();
		NegamaxUtil.chessEngine = new MainEngine();

		Statistics.reset();
		ChessBoard cb = ChessBoardUtil.getNewCB();

		/* time-managed */
		long timer = 300000L;
		NegamaxUtil.start(cb, timer);
	}

}
