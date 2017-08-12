package nl.s22k.chess.maintests;

import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.ChessBoardUtil;
import nl.s22k.chess.Statistics;
import nl.s22k.chess.engine.MainEngine;
import nl.s22k.chess.move.MagicUtil;
import nl.s22k.chess.search.NegamaxUtil;
import nl.s22k.chess.search.TimeUtil;

public class NodeCounter {

	private static final int MAX_PLY = 14;
	private static final int NUMBER_OF_POSITIONS = 100;
	
	public static void main(String[] args){
		// setup
		MagicUtil.init();
		NegamaxUtil.maxDepth=MAX_PLY;
		TimeUtil.setInfiniteWindow();
		MainEngine.quiet = true;
		
		long totalNodesSearched = 0;
		
		for(int index = 0; index < NUMBER_OF_POSITIONS; index++){
			System.out.println(index);
			String epdString = BestMoveTest.WAC_EPDS[index];
			Statistics.reset();
			EPD epd = new EPD(epdString);
			ChessBoard cb = ChessBoardUtil.getNewCB(epd.getFen());
			NegamaxUtil.start(cb);
			totalNodesSearched += Statistics.moveCount;
		}
		System.out.println("Total   " + totalNodesSearched);
		System.out.println("Average " + totalNodesSearched / NUMBER_OF_POSITIONS);
		
	}
	
}
