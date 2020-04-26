package nl.s22k.chess.maintests;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.ChessBoardInstances;
import nl.s22k.chess.ChessBoardUtil;
import nl.s22k.chess.ChessConstants;
import nl.s22k.chess.eval.EvalUtil;
import nl.s22k.chess.move.MoveGenerator;
import nl.s22k.chess.search.ThreadData;

public class PerftMultiThreaded {

	private static int[] runs = new int[] { 1, 1, 2, 4, 8, 16, 32 };
	private static final int DEPTH = 5;

	public static void main(String[] args) {
		ChessBoardInstances.init(32);
		ThreadData.initInstances(32);

		for (int run : runs) {
			ExecutorService executor = Executors.newFixedThreadPool(run);
			long start = System.currentTimeMillis();
			System.out.println("Starting " + run + " thread(s)");
			for (int i = 0; i < run; i++) {
				executor.execute(new PerftThread(i));
			}
			executor.shutdown();
			try {
				executor.awaitTermination(1000, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println(System.currentTimeMillis() - start);
		}
	}

	public static int perft(final ChessBoard cb, final ThreadData threadData, final int depth) {

		threadData.startPly();
		MoveGenerator.generateMoves(threadData, cb);
		MoveGenerator.generateAttacks(threadData, cb);

		if (depth == 0) {
			threadData.endPly();
			return 1;
		}
		int counter = 0;
		while (threadData.hasNext()) {
			final int move = threadData.next();
			if (!cb.isLegal(move)) {
				continue;
			}
			cb.doMove(move);
			EvalUtil.calculateScore(cb, threadData);
			counter += perft(cb, threadData, depth - 1);
			cb.undoMove(move);
		}

		threadData.endPly();
		return counter;

	}

	public static class PerftThread extends Thread {

		private int threadNumber;

		public PerftThread(final int threadNumber) {
			this.threadNumber = threadNumber;
		}

		public void run() {
			final ThreadData threadData = ThreadData.getInstance(threadNumber);
			final ChessBoard cb = ChessBoardInstances.get(threadNumber);
			ChessBoardUtil.setFen(ChessConstants.FEN_START, cb);
			perft(cb, threadData, DEPTH);
		};
	}

}
