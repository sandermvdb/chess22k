package nl.s22k.chess.maintests;

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

	public static int perft(final ChessBoard chessBoard, final int depth) {

		MoveList.startPly();
		MoveGenerator.generateMoves(chessBoard);
		MoveGenerator.generateAttacks(chessBoard);

		if (depth == 0) {
			if (!MoveList.hasNext()) {
				if (chessBoard.checkingPieces != 0) {
					Statistics.checkCount++;
					Statistics.mateCount++;
				} else {
					Statistics.staleMateCount++;
				}
			} else if (chessBoard.checkingPieces != 0) {
				Statistics.checkCount++;
			}
			MoveList.endPly();
			return 1;
		}
		int counter = 0;
		while (MoveList.hasNext()) {
			final int move = MoveList.next();
			if (depth == 1) {
				if (MoveUtil.isEP(move)) {
					Statistics.epCount++;
				} else if (MoveUtil.isCastling(move)) {
					Statistics.castleCount++;
				} else if (MoveUtil.isPromotion(move)) {
					// compensate rook and bishop promotions
					Statistics.promotionCount += 2;
					counter++;
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
		int counter = 0;
		while (MoveList.hasNext()) {
			final int move = MoveList.next();
			if (depth == 1) {
				if (MoveUtil.isEP(move)) {
					Statistics.epCount++;
				} else if (MoveUtil.isCastling(move)) {
					Statistics.castleCount++;
				} else if (MoveUtil.isPromotion(move)) {
					// compensate rook and bishop promotions
					// Statistics.promotionCount += 2;
					counter++;
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
		MagicUtil.init();
		String fen = "r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq -";

		ChessBoard chessBoard = ChessBoardUtil.getNewCB(fen);

		System.out.println(perft(chessBoard, 1) + " 48");
		System.out.println("Ct " + Statistics.castleCount + " 2");
		System.out.println("");
		Statistics.reset();

		chessBoard = ChessBoardUtil.getNewCB(fen);
		System.out.println(perft(chessBoard, 2) + " 2039");
		System.out.println("Ct " + Statistics.castleCount + " 91");
		System.out.println("Ck " + Statistics.checkCount + " 3");
		System.out.println("EP " + Statistics.epCount + " 1");
		System.out.println("");
		Statistics.reset();

		chessBoard = ChessBoardUtil.getNewCB(fen);
		System.out.println(perft(chessBoard, 3) + " 97862");
		System.out.println("Ct " + Statistics.castleCount + " 3162");
		System.out.println("Ck " + Statistics.checkCount + " 993");
		System.out.println("EP " + Statistics.epCount + " 45");
		System.out.println("Mt " + Statistics.mateCount + " 1");
		System.out.println("");
		Statistics.reset();

		chessBoard = ChessBoardUtil.getNewCB(fen);
		System.out.println(perft(chessBoard, 4) + " 4085603");
		System.out.println("Ct " + Statistics.castleCount + " 128013");
		System.out.println("EP " + Statistics.epCount + " 1929");
		System.out.println("Pr " + Statistics.promotionCount + " 15172");
		System.out.println("Ck " + Statistics.checkCount + " 25523");
		System.out.println("Mt " + Statistics.mateCount + " 43");
		Statistics.reset();

		// chessBoard = new ChessBoard(fen);
		// System.out.println(perft(chessBoard, 5) + " 193690690");
		// System.out.println("Ct" + Statistics.castleCount + " 4993637");
		// System.out.println("EP " + Statistics.epCount + " 73365");
		// System.out.println(Statistics.checkCount + " 3309887");
		// System.out.println(Statistics.mateCount + " 30171");

	}

	// @Test
	public static void main(String[] args) {

		// public void perftTest() {
		MagicUtil.init();
		ChessBoard chessBoard = ChessBoardUtil.getNewCB();

		System.out.println(perft(chessBoard, 1) + " 20");
		Statistics.reset();

		chessBoard = ChessBoardUtil.getNewCB();
		System.out.println(perft(chessBoard, 2) + " 400");
		Statistics.reset();

		chessBoard = ChessBoardUtil.getNewCB();
		System.out.println(perft(chessBoard, 3) + " 8902");
		System.out.println(Statistics.checkCount + " 12");
		Statistics.reset();

		chessBoard = ChessBoardUtil.getNewCB();
		System.out.println(perft(chessBoard, 4) + " 197281");
		System.out.println(Statistics.checkCount + " 469");
		System.out.println(Statistics.mateCount + " 8");
		Statistics.reset();

		chessBoard = ChessBoardUtil.getNewCB();
		System.out.println(perft(chessBoard, 5) + " 4865609");
		System.out.println(Statistics.checkCount + " 27351");
		System.out.println(Statistics.mateCount + " 347");
		System.out.println(System.currentTimeMillis() - Statistics.startTime);
		Statistics.reset();

		chessBoard = ChessBoardUtil.getNewCB();
		System.out.println(perft(chessBoard, 6) + " 119060324");
		System.out.println(Statistics.checkCount + " 809099");
		System.out.println(Statistics.mateCount + " 10828");
		System.out.println(System.currentTimeMillis() - Statistics.startTime);
	}

}
