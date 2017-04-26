package nl.s22k.test.chess;

import org.junit.Test;

import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.ChessBoardUtil;
import nl.s22k.chess.move.MagicUtil;
import nl.s22k.chess.search.NegamaxUtil;
import nl.s22k.chess.search.TimeUtil;

public class CoberturaTest {

	@Test
	public void doTest() {
		MagicUtil.init();

		ChessBoard cb = ChessBoardUtil.getNewCB("r2qr1k1/2p2ppp/p3bn2/2bpN1B1/8/2NQ4/PPP2PPP/3RR1K1 b - - 3 14 ");

		/* time-managed */
		TimeUtil.setTimeWindow(300000, 0, 0);
		NegamaxUtil.start(cb);
	}

}
