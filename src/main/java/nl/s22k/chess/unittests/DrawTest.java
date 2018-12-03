package nl.s22k.chess.unittests;

import org.junit.Test;

import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.ChessBoardUtil;
import nl.s22k.chess.Statistics;
import nl.s22k.chess.search.NegamaxUtil;

public class DrawTest {

	@Test
	public void insufficientMaterialTest() {
		System.out.println("insufficientMaterialTest");
		Statistics.reset();
		ChessBoard cb = ChessBoardUtil.getNewCB("8/8/7B/8/8/5k2/5p2/5K2 b - - 19 85 ");
		NegamaxUtil.start(cb);
		Statistics.print();
	}

	@Test
	public void insufficientMaterialTest2() {
		System.out.println("insufficientMaterialTest2");
		Statistics.reset();
		ChessBoard cb = ChessBoardUtil.getNewCB("8/8/n3B3/8/5K2/8/2k5/8 b - - 99 1 ");
		NegamaxUtil.start(cb);
		Statistics.print();
	}

	@Test
	public void repetitionIsBestMoveTest() {
		System.out.println("repetitionIsBestMoveTest");
		Statistics.reset();
		ChessBoard cb = ChessBoardUtil.getNewCB("3k4/4R1R1/8/8/8/K5pp/6r1/7q w - - 100 1 ");
		NegamaxUtil.start(cb);
		Statistics.print();
	}

}
