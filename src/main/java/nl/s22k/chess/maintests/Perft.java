package nl.s22k.chess.maintests;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import nl.s22k.chess.Assert;
import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.ChessBoardInstances;
import nl.s22k.chess.ChessBoardUtil;
import nl.s22k.chess.ChessConstants;
import nl.s22k.chess.engine.EngineConstants;
import nl.s22k.chess.move.MoveGenerator;
import nl.s22k.chess.move.MoveWrapper;
import nl.s22k.chess.search.ThreadData;

public class Perft {

	private static ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(8);

	public static void main(String[] args) {
		if (!EngineConstants.GENERATE_BR_PROMOTIONS) {
			System.out.println("Generation of underpromotions is disabled!");
		}

		ChessBoardInstances.init(8);
		ThreadData.initInstances(8);

		int threadNr = 0;
		PerftWorker kiwi = new PerftWorker("Kiwi-pete", threadNr++);
		kiwi.addPosition("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -", 4085603, 4);

		PerftWorker ep = new PerftWorker("EP", threadNr++);
		ep.addPosition("3k4/3p4/8/K1P4r/8/8/8/8 b - - 0 1", 20757544, 7);
		ep.addPosition("8/8/4k3/8/2p5/8/B2P2K1/8 w - - 0 1", 14047573, 7);
		ep.addPosition("8/8/1k6/2b5/2pP4/8/5K2/8 b - d3 0 1", 21190412, 7);

		PerftWorker castling = new PerftWorker("Castling", threadNr++);
		castling.addPosition("5k2/8/8/8/8/8/8/4K2R w K - 0 1", 661072, 6);
		castling.addPosition("3k4/8/8/8/8/8/8/R3K3 w Q - 0 1", 803711, 6);
		castling.addPosition("r3k2r/1b4bq/8/8/8/8/7B/R3K2R w KQkq - 0 1", 1274206, 4);
		castling.addPosition("r3k2r/8/3Q4/8/8/5q2/8/R3K2R b KQkq - 0 1", 1720476, 4);

		PerftWorker promotion = new PerftWorker("Promotion", threadNr++);
		promotion.addPosition("2K2r2/4P3/8/8/8/8/8/3k4 w - - 0 1", 60651209, 7);
		promotion.addPosition("4k3/1P6/8/8/8/8/K7/8 w - - 0 1", 3742283, 7);
		promotion.addPosition("8/P1k5/K7/8/8/8/8/8 w - - 0 1", 1555980, 7);

		PerftWorker mate = new PerftWorker("Stalemate and Checkmate", threadNr++);
		mate.addPosition("8/8/1P2K3/8/2n5/1q6/8/5k2 b - - 0 1", 6334638, 6);
		mate.addPosition("K1k5/8/P7/8/8/8/8/8 w - - 0 1", 15453, 7);
		mate.addPosition("8/k1P5/8/1K6/8/8/8/8 w - - 0 1", 2518905, 8);
		mate.addPosition("8/8/2k5/5q2/5n2/8/5K2/8 b - - 0 1", 3114998, 6);

		PerftWorker perft = new PerftWorker("Perft", threadNr++);
		perft.addPosition(ChessConstants.FEN_START, 4865609, 5);

		executor.execute(kiwi);
		executor.execute(ep);
		executor.execute(castling);
		executor.execute(promotion);
		executor.execute(mate);
		executor.execute(perft);

		executor.shutdown();
		try {
			executor.awaitTermination(1, TimeUnit.DAYS);
		} catch (InterruptedException e) {
		}
		System.out.println();
		System.out.println("Done");
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
			counter += perft(cb, threadData, depth - 1);
			cb.undoMove(move);
		}

		threadData.endPly();
		return counter;
	}

	public static int divide(final ChessBoard cb, final ThreadData threadData, final int depth) {

		threadData.startPly();
		MoveGenerator.generateMoves(threadData, cb);
		MoveGenerator.generateAttacks(threadData, cb);
		int counter = 0;
		while (threadData.hasNext()) {
			final int move = threadData.next();
			cb.doMove(move);
			final int divideCounter = perft(cb, threadData, depth - 1);
			counter += divideCounter;
			cb.undoMove(move);
			System.out.println(new MoveWrapper(move) + ": " + divideCounter);
		}

		return counter;
	}

	public static class PerftWorker extends Thread {

		private List<PerftPosition> positions = new ArrayList<>();
		private String name;
		private int threadNumber;

		public PerftWorker(String name, int threadNumber) {
			this.name = name;
			this.threadNumber = threadNumber;
		}

		@Override
		public void run() {
			System.out.println("Start " + name);

			ChessBoard cb = ChessBoardInstances.get(threadNumber);
			ThreadData threadData = ThreadData.getInstance(threadNumber);
			for (PerftPosition position : positions) {
				ChessBoardUtil.setFen(position.fen, cb);
				Assert.isTrue(position.moveCount == perft(cb, threadData, position.depth));
			}

			System.out.println("Done " + name);
		}

		public void addPosition(String position, int moveCount, int depth) {
			positions.add(new PerftPosition(position, moveCount, depth));
		}
	}

	public static class PerftPosition {
		String fen;
		int moveCount;
		int depth;

		public PerftPosition(String fen, int moveCount, int depth) {
			this.fen = fen;
			this.moveCount = moveCount;
			this.depth = depth;
		}
	}

}
