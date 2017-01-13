package nl.s22k.chess.unittests;

import org.junit.BeforeClass;
import org.junit.Test;

import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.ChessBoardUtil;
import nl.s22k.chess.Statistics;
import nl.s22k.chess.engine.MainEngine;
import nl.s22k.chess.move.MagicUtil;
import nl.s22k.chess.search.NegamaxUtil;

public class DrawTest {

	@BeforeClass
	public static void init() {
		MagicUtil.init();
		NegamaxUtil.chessEngine = new MainEngine();
	}

	@Test
	public void insufficientMaterialTest() {
		System.out.println("insufficientMaterialTest");
		Statistics.reset();
		ChessBoard cb = ChessBoardUtil.getNewCB("8/8/7B/8/8/5k2/5p2/5K2 b - - 19 85 ");
		NegamaxUtil.start(cb, 10);
	}

	@Test
	public void repetitionIsBestMoveTest() {
		System.out.println("repetitionIsBestMoveTest");
		Statistics.reset();
		ChessBoard cb = ChessBoardUtil.getNewCB("3k4/4R1R1/8/8/8/K5pp/6r1/7q w - - 100 1 ");
		NegamaxUtil.start(cb, 15);
		Statistics.print();
	}

}
