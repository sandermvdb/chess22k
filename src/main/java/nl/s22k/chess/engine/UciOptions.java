package nl.s22k.chess.engine;

import nl.s22k.chess.ChessBoardInstances;
import nl.s22k.chess.search.SearchUtil;
import nl.s22k.chess.search.ThreadData;

public class UciOptions {

	public static int threadCount = 1;
	public static boolean ponder = true;

	public static void setThreadCount(int threadCount) {
		if (threadCount != UciOptions.threadCount) {
			UciOptions.threadCount = threadCount;
			ChessBoardInstances.init(threadCount);
			ThreadData.initInstances(threadCount);
			SearchUtil.setThreadCount(threadCount);
		}
	}

	public static void setPonder(boolean ponder) {
		UciOptions.ponder = ponder;
	}

}
