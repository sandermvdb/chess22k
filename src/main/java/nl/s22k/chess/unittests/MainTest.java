package nl.s22k.chess.unittests;

import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.ChessBoardUtil;
import nl.s22k.chess.Statistics;
import nl.s22k.chess.engine.MainEngine;
import nl.s22k.chess.move.MagicUtil;
import nl.s22k.chess.search.NegamaxUtil;

public class MainTest {

	public static void main(String[] args) {

		MagicUtil.init();
		NegamaxUtil.chessEngine = new MainEngine();

		Statistics.reset();

		ChessBoard cb = ChessBoardUtil.getNewCB("r2qr1k1/2p2ppp/p3bn2/2bpN1B1/8/2NQ4/PPP2PPP/3RR1K1 b - - 3 14 ");
		// ChessBoard cb = ChessBoardUtil.getNewCB();

		// losing capture!!
		// ChessBoard cb = ChessBoardUtil.getNewCB("2b5/8/1r1Bp1p1/p2pP1P1/k1pP4/1pP5/1R3P2/7K b - - 0 1 ");

		// /* Fixed depth */
		// int depth = 6;
		// NegamaxUtil.start(cb, depth);

		/* time-managed */
		long timer = 300000L;
		NegamaxUtil.start(cb, timer);

	}

}
