package nl.s22k.chess.maintests;

import org.junit.BeforeClass;
import org.junit.Test;

import nl.s22k.chess.Assert;
import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.ChessBoardUtil;
import nl.s22k.chess.engine.EngineConstants;
import nl.s22k.chess.move.MagicUtil;
import nl.s22k.chess.move.MoveGenerator;
import nl.s22k.chess.move.MoveWrapper;

public class Perft {

	private static MoveGenerator moveGen = new MoveGenerator();

	@BeforeClass
	public static void init() {
		MagicUtil.init();
		if (!EngineConstants.GENERATE_BR_PROMOTIONS) {
			throw new RuntimeException("Generation of underpromotions must be enabled");
		}
	}

	public static int perft(final ChessBoard chessBoard, final int depth) {

		moveGen.startPly();
		moveGen.generateMoves(chessBoard);
		moveGen.generateAttacks(chessBoard);

		if (depth == 0) {
			moveGen.endPly();
			return 1;
		}
		int counter = 0;
		while (moveGen.hasNext()) {
			final int move = moveGen.next();
			if (!chessBoard.isLegal(move)) {
				continue;
			}
			chessBoard.doMove(move);
			counter += perft(chessBoard, depth - 1);
			chessBoard.undoMove(move);
		}

		moveGen.endPly();
		return counter;

	}

	public static int divide(final ChessBoard chessBoard, final int depth) {

		moveGen.startPly();
		moveGen.generateMoves(chessBoard);
		moveGen.generateAttacks(chessBoard);
		int counter = 0;
		while (moveGen.hasNext()) {
			final int move = moveGen.next();
			chessBoard.doMove(move);
			final int divideCounter = perft(chessBoard, depth - 1);
			counter += divideCounter;
			chessBoard.undoMove(move);
			System.out.println(new MoveWrapper(move) + ": " + divideCounter);
		}

		return counter;

	}

	@Test
	public void perft5() {
		ChessBoard chessBoard = ChessBoardUtil.getNewCB();
		Assert.isTrue(4865609 == perft(chessBoard, 5));
	}

	@Test
	public void kiwipeteTest() {
		System.out.println("Kiwi-pete");

		String fen = "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -";

		ChessBoard chessBoard = ChessBoardUtil.getNewCB(fen);
		Assert.isTrue(4085603 == perft(chessBoard, 4));

		// ChessBoard chessBoard = ChessBoardUtil.getNewCB(fen);
		// System.out.println(perft(chessBoard, 5) + " 193690690");

	}

	@Test
	public void EPTest() {

		System.out.println("EP");

		// Illegal ep move #1
		ChessBoard chessBoard = ChessBoardUtil.getNewCB("3k4/3p4/8/K1P4r/8/8/8/8 b - - 0 1");
		Assert.isTrue(20757544 == perft(chessBoard, 7));

		// Illegal ep move #2
		chessBoard = ChessBoardUtil.getNewCB("8/8/4k3/8/2p5/8/B2P2K1/8 w - - 0 1");
		Assert.isTrue(14047573 == perft(chessBoard, 7));

		// EP Capture Checks Opponent
		chessBoard = ChessBoardUtil.getNewCB("8/8/1k6/2b5/2pP4/8/5K2/8 b - d3 0 1");
		Assert.isTrue(21190412 == perft(chessBoard, 7));
	}

	@Test
	public void castlingTest() {

		System.out.println("Castling");

		// Short Castling Gives Check
		ChessBoard chessBoard = ChessBoardUtil.getNewCB("5k2/8/8/8/8/8/8/4K2R w K - 0 1");
		Assert.isTrue(661072 == perft(chessBoard, 6));

		// Long Castling Gives Check
		chessBoard = ChessBoardUtil.getNewCB("3k4/8/8/8/8/8/8/R3K3 w Q - 0 1");
		Assert.isTrue(803711 == perft(chessBoard, 6));

		// Castle Rights
		chessBoard = ChessBoardUtil.getNewCB("r3k2r/1b4bq/8/8/8/8/7B/R3K2R w KQkq - 0 1");
		Assert.isTrue(1274206 == perft(chessBoard, 4));

		// Castling Prevented
		chessBoard = ChessBoardUtil.getNewCB("r3k2r/8/3Q4/8/8/5q2/8/R3K2R b KQkq - 0 1");
		Assert.isTrue(1720476 == perft(chessBoard, 4));
	}

	@Test
	public void promotionTest() {

		System.out.println("Promotion");

		// Promote out of Check
		ChessBoard chessBoard = ChessBoardUtil.getNewCB("2K2r2/4P3/8/8/8/8/8/3k4 w - - 0 1");
		Assert.isTrue(60651209 == perft(chessBoard, 7));

		// Promote to give check
		chessBoard = ChessBoardUtil.getNewCB("4k3/1P6/8/8/8/8/K7/8 w - - 0 1");
		Assert.isTrue(3742283 == perft(chessBoard, 7));

		// Under Promote to give check
		chessBoard = ChessBoardUtil.getNewCB("8/P1k5/K7/8/8/8/8/8 w - - 0 1");
		Assert.isTrue(1555980 == perft(chessBoard, 7));
	}

	@Test
	public void stalemateAndCheckmateTest() {

		System.out.println("Check-and stale-mate");

		// Discovered Check
		ChessBoard chessBoard = ChessBoardUtil.getNewCB("8/8/1P2K3/8/2n5/1q6/8/5k2 b - - 0 1");
		Assert.isTrue(6334638 == perft(chessBoard, 6));

		// Self Stalemate
		chessBoard = ChessBoardUtil.getNewCB("K1k5/8/P7/8/8/8/8/8 w - - 0 1");
		Assert.isTrue(15453 == perft(chessBoard, 7));

		// Stalemate & Checkmate
		chessBoard = ChessBoardUtil.getNewCB("8/k1P5/8/1K6/8/8/8/8 w - - 0 1");
		Assert.isTrue(2518905 == perft(chessBoard, 8));

		// Stalemate & Checkmate
		chessBoard = ChessBoardUtil.getNewCB("8/8/2k5/5q2/5n2/8/5K2/8 b - - 0 1");
		Assert.isTrue(3114998 == perft(chessBoard, 6));
	}

}
