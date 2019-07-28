package nl.s22k.chess.maintests;

import nl.s22k.chess.Assert;
import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.ChessBoardUtil;
import nl.s22k.chess.engine.EngineConstants;
import nl.s22k.chess.move.MoveGenerator;
import nl.s22k.chess.move.MoveWrapper;

public class Perft {

	private static MoveGenerator moveGen = new MoveGenerator();

	public static void main(String[] args) {
		if (!EngineConstants.GENERATE_BR_PROMOTIONS) {
			throw new RuntimeException("Generation of underpromotions must be enabled");
		}

		kiwipeteTest();
		EPTest();
		castlingTest();
		promotionTest();
		stalemateAndCheckmateTest();
		perft5();
	}

	public static int perft(final ChessBoard cb, final int depth) {

		moveGen.startPly();
		moveGen.generateMoves(cb);
		moveGen.generateAttacks(cb);

		if (depth == 0) {
			moveGen.endPly();
			return 1;
		}
		int counter = 0;
		while (moveGen.hasNext()) {
			final int move = moveGen.next();
			if (!cb.isLegal(move)) {
				continue;
			}
			cb.doMove(move);
			counter += perft(cb, depth - 1);
			cb.undoMove(move);
		}

		moveGen.endPly();
		return counter;

	}

	public static int divide(final ChessBoard cb, final int depth) {

		moveGen.startPly();
		moveGen.generateMoves(cb);
		moveGen.generateAttacks(cb);
		int counter = 0;
		while (moveGen.hasNext()) {
			final int move = moveGen.next();
			cb.doMove(move);
			final int divideCounter = perft(cb, depth - 1);
			counter += divideCounter;
			cb.undoMove(move);
			System.out.println(new MoveWrapper(move) + ": " + divideCounter);
		}

		return counter;

	}

	public static void perft5() {
		System.out.println("perft 5");
		ChessBoard chessBoard = ChessBoardUtil.getNewCB();
		Assert.isTrue(4865609 == perft(chessBoard, 5));
	}

	public static void kiwipeteTest() {
		System.out.println("Kiwi-pete");

		String fen = "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -";

		ChessBoard cb = ChessBoardUtil.getNewCB(fen);
		Assert.isTrue(4085603 == perft(cb, 4));

		// ChessBoard cb = ChessBoardUtil.getNewCB(fen);
		// System.out.println(perft(cb, 5) + " 193690690");

	}

	public static void EPTest() {

		System.out.println("EP");

		// Illegal ep move #1
		ChessBoard cb = ChessBoardUtil.getNewCB("3k4/3p4/8/K1P4r/8/8/8/8 b - - 0 1");
		Assert.isTrue(20757544 == perft(cb, 7));

		// Illegal ep move #2
		cb = ChessBoardUtil.getNewCB("8/8/4k3/8/2p5/8/B2P2K1/8 w - - 0 1");
		Assert.isTrue(14047573 == perft(cb, 7));

		// EP Capture Checks Opponent
		cb = ChessBoardUtil.getNewCB("8/8/1k6/2b5/2pP4/8/5K2/8 b - d3 0 1");
		Assert.isTrue(21190412 == perft(cb, 7));
	}

	public static void castlingTest() {

		System.out.println("Castling");

		// Short Castling Gives Check
		ChessBoard cb = ChessBoardUtil.getNewCB("5k2/8/8/8/8/8/8/4K2R w K - 0 1");
		Assert.isTrue(661072 == perft(cb, 6));

		// Long Castling Gives Check
		cb = ChessBoardUtil.getNewCB("3k4/8/8/8/8/8/8/R3K3 w Q - 0 1");
		Assert.isTrue(803711 == perft(cb, 6));

		// Castle Rights
		cb = ChessBoardUtil.getNewCB("r3k2r/1b4bq/8/8/8/8/7B/R3K2R w KQkq - 0 1");
		Assert.isTrue(1274206 == perft(cb, 4));

		// Castling Prevented
		cb = ChessBoardUtil.getNewCB("r3k2r/8/3Q4/8/8/5q2/8/R3K2R b KQkq - 0 1");
		Assert.isTrue(1720476 == perft(cb, 4));
	}

	public static void promotionTest() {

		System.out.println("Promotion");

		// Promote out of Check
		ChessBoard cb = ChessBoardUtil.getNewCB("2K2r2/4P3/8/8/8/8/8/3k4 w - - 0 1");
		Assert.isTrue(60651209 == perft(cb, 7));

		// Promote to give check
		cb = ChessBoardUtil.getNewCB("4k3/1P6/8/8/8/8/K7/8 w - - 0 1");
		Assert.isTrue(3742283 == perft(cb, 7));

		// Under Promote to give check
		cb = ChessBoardUtil.getNewCB("8/P1k5/K7/8/8/8/8/8 w - - 0 1");
		Assert.isTrue(1555980 == perft(cb, 7));
	}

	public static void stalemateAndCheckmateTest() {

		System.out.println("Check-and stale-mate");

		// Discovered Check
		ChessBoard cb = ChessBoardUtil.getNewCB("8/8/1P2K3/8/2n5/1q6/8/5k2 b - - 0 1");
		Assert.isTrue(6334638 == perft(cb, 6));

		// Self Stalemate
		cb = ChessBoardUtil.getNewCB("K1k5/8/P7/8/8/8/8/8 w - - 0 1");
		Assert.isTrue(15453 == perft(cb, 7));

		// Stalemate & Checkmate
		cb = ChessBoardUtil.getNewCB("8/k1P5/8/1K6/8/8/8/8 w - - 0 1");
		Assert.isTrue(2518905 == perft(cb, 8));

		// Stalemate & Checkmate
		cb = ChessBoardUtil.getNewCB("8/8/2k5/5q2/5n2/8/5K2/8 b - - 0 1");
		Assert.isTrue(3114998 == perft(cb, 6));
	}

}
