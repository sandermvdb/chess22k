package nl.s22k.chess.search;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.ChessBoardInstances;
import nl.s22k.chess.ChessBoardUtil;
import nl.s22k.chess.engine.UciOptions;

public class SearchUtil {

	private static ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(UciOptions.threadCount);

	public static void setThreadCount(final int threadCount) {
		executor.shutdownNow();
		executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(threadCount);
	}

	public static void start(final ChessBoard cb) {
		NegamaxUtil.isRunning = true;
		cb.moveCount = 0;

		if (UciOptions.threadCount == 1) {
			new SearchThread(0).call();
		} else {
			List<SearchThread> threads = new ArrayList<>();
			for (int i = 0; i < UciOptions.threadCount; i++) {
				if (i > 0) {
					ChessBoardUtil.copy(cb, ChessBoardInstances.get(i));
				}
				threads.add(new SearchThread(i));
			}
			try {
				executor.invokeAll(threads);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
