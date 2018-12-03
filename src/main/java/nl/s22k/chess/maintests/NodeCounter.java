package nl.s22k.chess.maintests;

import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.ChessBoardUtil;
import nl.s22k.chess.Statistics;
import nl.s22k.chess.engine.EngineConstants;
import nl.s22k.chess.engine.MainEngine;
import nl.s22k.chess.search.NegamaxUtil;
import nl.s22k.chess.search.TimeUtil;

public class NodeCounter {

	private static final int MAX_PLY = 11;
	private static final int NUMBER_OF_POSITIONS = 100;

	public static void main(String[] args) {

		MainEngine.maxDepth = MAX_PLY;
		TimeUtil.setInfiniteWindow();
		MainEngine.noOutput = true;
		EngineConstants.POWER_2_TT_ENTRIES = 2;

		long totalNodesSearched = 0;

		for (int index = 0; index < NUMBER_OF_POSITIONS; index++) {
			System.out.println(index);
			String epdString = BestMoveTest.WAC_EPDS[index + 20];
			Statistics.reset();
			EPD epd = new EPD(epdString);
			ChessBoard cb = ChessBoardUtil.getNewCB(epd.getFen());
			NegamaxUtil.start(cb);
			totalNodesSearched += ChessBoard.getTotalMoveCount();
		}
		System.out.println("Total   " + totalNodesSearched);
		System.out.println("Average " + totalNodesSearched / NUMBER_OF_POSITIONS);

	}

}
