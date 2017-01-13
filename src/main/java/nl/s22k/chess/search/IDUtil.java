package nl.s22k.chess.search;

import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.Statistics;

public class IDUtil {

	public static boolean isTimeLeft(ChessBoard chessBoard, long msecLeft) {
		if (chessBoard.moveCounter >= 80) {
			// end-game (80+)
			return msecLeft / 40 > (System.currentTimeMillis() - Statistics.startTime);
		}
		if (chessBoard.moveCounter >= 40) {
			// middle-game (40-80)
			return msecLeft / (100 - chessBoard.moveCounter) / 1.8 > (System.currentTimeMillis() - Statistics.startTime);
		}
		// start-game (0-40)
		return msecLeft / (100 - chessBoard.moveCounter) / 1 > (System.currentTimeMillis() - Statistics.startTime);
	}

}
