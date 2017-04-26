package nl.s22k.chess.maintests;

import org.junit.BeforeClass;
import org.junit.Test;

import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.ChessBoardUtil;
import nl.s22k.chess.Statistics;
import nl.s22k.chess.move.MagicUtil;
import nl.s22k.chess.move.MoveGenerator;
import nl.s22k.chess.move.MoveList;
import nl.s22k.chess.move.MoveUtil;
import nl.s22k.chess.move.MoveWrapper;

public class Perft {

	@BeforeClass
	public static void init() {
		MagicUtil.init();
		System.out.println("Do not forget to enable bishop- and rook-promotions!");
	}

	public static int perft(final ChessBoard chessBoard, final int depth) {

		MoveList.startPly();
		MoveGenerator.generateMoves(chessBoard);
		MoveGenerator.generateAttacks(chessBoard);

		if (depth == 0) {
			if (!MoveList.hasNext()) {
				if (chessBoard.checkingPieces != 0) {
					Statistics.mateCount++;
				} else {
					Statistics.staleMateCount++;
				}
			}
			MoveList.endPly();
			return 1;
		}
		int counter = 0;
		while (MoveList.hasNext()) {
			final int move = MoveList.next();
			if (depth == 1) {
				switch (MoveUtil.getMoveType(move)) {
				case MoveUtil.CASTLING:
					Statistics.castleCount++;
					break;
				case MoveUtil.EP:
					Statistics.epCount++;
					break;
				case MoveUtil.PROMOTION_B:
				case MoveUtil.PROMOTION_N:
				case MoveUtil.PROMOTION_Q:
				case MoveUtil.PROMOTION_R:
					Statistics.promotionCount++;
				}
			}
			chessBoard.doMove(move);
			counter += perft(chessBoard, depth - 1);
			chessBoard.undoMove(move);
		}

		MoveList.endPly();
		return counter;

	}

	public static int divide(final ChessBoard chessBoard, final int depth) {

		MoveList.startPly();
		MoveGenerator.generateMoves(chessBoard);
		MoveGenerator.generateAttacks(chessBoard);
		int counter = 0;
		while (MoveList.hasNext()) {
			final int move = MoveList.next();
			if (depth == 1) {
				switch (MoveUtil.getMoveType(move)) {
				case MoveUtil.CASTLING:
					Statistics.castleCount++;
					break;
				case MoveUtil.EP:
					Statistics.epCount++;
					break;
				case MoveUtil.PROMOTION_B:
				case MoveUtil.PROMOTION_N:
				case MoveUtil.PROMOTION_Q:
				case MoveUtil.PROMOTION_R:
					Statistics.promotionCount++;
				}
			}
			chessBoard.doMove(move);
			final int divideCounter = perft(chessBoard, depth - 1);
			counter += divideCounter;
			chessBoard.undoMove(move);
			System.out.println(new MoveWrapper(move) + ": " + divideCounter);
		}

		return counter;

	}

	@Test
	public void kiwipeteTest() {
		System.out.println("Kiwi-pete");

		String fen = "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -";

		ChessBoard chessBoard = ChessBoardUtil.getNewCB(fen);

		chessBoard = ChessBoardUtil.getNewCB(fen);
		assert perft(chessBoard, 4) == 4085603;
		// System.out.println("Ct " + Statistics.castleCount + " 128013");
		// System.out.println("EP " + Statistics.epCount + " 1929");
		// System.out.println("Pr " + Statistics.promotionCount + " 15172");
		// System.out.println("Ck " + Statistics.checkCount + " 25523");
		// System.out.println("Mt " + Statistics.mateCount + " 43");
		// Statistics.reset();

		// chessBoard = ChessBoardUtil.getNewCB(fen);
		// System.out.println(perft(chessBoard, 5) + " 193690690");
		// System.out.println("Ct" + Statistics.castleCount + " 4993637");
		// System.out.println("EP " + Statistics.epCount + " 73365");
		// System.out.println(Statistics.checkCount + " 3309887");
		// System.out.println(Statistics.mateCount + " 30171");

	}

	@Test
	public void EPTest() {

		System.out.println("EP");

		// Illegal ep move #1
		ChessBoard chessBoard = ChessBoardUtil.getNewCB("3k4/3p4/8/K1P4r/8/8/8/8 b - - 0 1");
		assert perft(chessBoard, 6) == 1134888;
		Statistics.reset();

		// Illegal ep move #2
		chessBoard = ChessBoardUtil.getNewCB("8/8/4k3/8/2p5/8/B2P2K1/8 w - - 0 1");
		assert perft(chessBoard, 6) == 1015133;
		Statistics.reset();

		// EP Capture Checks Opponent
		chessBoard = ChessBoardUtil.getNewCB("8/8/1k6/2b5/2pP4/8/5K2/8 b - d3 0 1");
		assert perft(chessBoard, 5) == 206379;
		Statistics.reset();
	}

	@Test
	public void castlingTest() {

		System.out.println("Castling");

		// Short Castling Gives Check
		ChessBoard chessBoard = ChessBoardUtil.getNewCB("5k2/8/8/8/8/8/8/4K2R w K - 0 1");
		assert perft(chessBoard, 6) == 661072;
		Statistics.reset();

		// Long Castling Gives Check
		chessBoard = ChessBoardUtil.getNewCB("3k4/8/8/8/8/8/8/R3K3 w Q - 0 1");
		assert perft(chessBoard, 6) == 803711;
		Statistics.reset();

		// Castle Rights
		chessBoard = ChessBoardUtil.getNewCB("r3k2r/1b4bq/8/8/8/8/7B/R3K2R w KQkq - 0 1");
		assert perft(chessBoard, 4) == 1274206;
		Statistics.reset();

		// Castling Prevented
		chessBoard = ChessBoardUtil.getNewCB("r3k2r/8/3Q4/8/8/5q2/8/R3K2R b KQkq - 0 1");
		assert perft(chessBoard, 4) == 1720476;
		Statistics.reset();
	}

	@Test
	public void promotionTest() {

		System.out.println("Promotion");

		// Promote out of Check
		ChessBoard chessBoard = ChessBoardUtil.getNewCB("2K2r2/4P3/8/8/8/8/8/3k4 w - - 0 1");
		assert perft(chessBoard, 6) == 3821001;
		Statistics.reset();

		// Promote to give check
		chessBoard = ChessBoardUtil.getNewCB("4k3/1P6/8/8/8/8/K7/8 w - - 0 1");
		assert perft(chessBoard, 6) == 217342;
		Statistics.reset();

		// Under Promote to give check
		chessBoard = ChessBoardUtil.getNewCB("8/P1k5/K7/8/8/8/8/8 w - - 0 1");
		assert perft(chessBoard, 6) == 92683;
		Statistics.reset();
	}

	@Test
	public void stalemateAndCheckmateTest() {

		System.out.println("Check-and stale-mate");

		// Discovered Check
		ChessBoard chessBoard = ChessBoardUtil.getNewCB("8/8/1P2K3/8/2n5/1q6/8/5k2 b - - 0 1");
		assert perft(chessBoard, 5) == 1004658;
		Statistics.reset();

		// Self Stalemate
		chessBoard = ChessBoardUtil.getNewCB("K1k5/8/P7/8/8/8/8/8 w - - 0 1");
		assert perft(chessBoard, 5) == 382;
		Statistics.reset();

		// Stalemate & Checkmate
		chessBoard = ChessBoardUtil.getNewCB("8/k1P5/8/1K6/8/8/8/8 w - - 0 1");
		assert perft(chessBoard, 7) == 567584;
		Statistics.reset();

		// Stalemate & Checkmate
		chessBoard = ChessBoardUtil.getNewCB("8/8/2k5/5q2/5n2/8/5K2/8 b - - 0 1");
		assert perft(chessBoard, 4) == 23527;
		Statistics.reset();

	}

}
