package nl.s22k.chess.maintests;

import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.ChessBoardUtil;
import nl.s22k.chess.Statistics;
import nl.s22k.chess.move.MagicUtil;
import nl.s22k.chess.move.MoveGenerator;
import nl.s22k.chess.move.MoveList;
import nl.s22k.chess.move.MoveWrapper;

public class QPerft {

	public static long qperft(final ChessBoard chessBoard, final int depth) {

		MoveList.startPly();
		MoveGenerator.generateMoves(chessBoard);
		MoveGenerator.generateAttacks(chessBoard);

		if (depth == 1) {
			final int movesFound = MoveList.movesLeft();
			MoveList.endPly();
			return movesFound;
		}

		long counter = 0;
		while (MoveList.hasNext()) {
			final int move = MoveList.next();
			chessBoard.doMove(move);
			counter += qperft(chessBoard, depth - 1);
			chessBoard.undoMove(move);
		}

		MoveList.endPly();
		return counter;

	}

	public static long qdivide(final ChessBoard chessBoard, final int depth) {

		MoveList.startPly();
		MoveGenerator.generateMoves(chessBoard);
		int counter = 0;
		while (MoveList.hasNext()) {
			final int move = MoveList.next();
			if (depth == 1) {
				System.out.println(new MoveWrapper(move) + ": " + 1);
				counter++;
				continue;
			}
			chessBoard.doMove(move);
			final long divideCounter = qperft(chessBoard, depth - 1);
			counter += divideCounter;
			chessBoard.undoMove(move);
			System.out.println(new MoveWrapper(move) + ": " + divideCounter);
		}

		return counter;

	}

	public static void main(String[] args) {

		MagicUtil.init();
		ChessBoard chessBoard = ChessBoardUtil.getNewCB();

		System.out.println(qperft(chessBoard, 1) + " 20");
		Statistics.reset();

		chessBoard = ChessBoardUtil.getNewCB();
		System.out.println(qperft(chessBoard, 2) + " 400");
		Statistics.reset();

		chessBoard = ChessBoardUtil.getNewCB();
		System.out.println(qperft(chessBoard, 3) + " 8902");
		Statistics.reset();

		chessBoard = ChessBoardUtil.getNewCB();
		System.out.println(qperft(chessBoard, 4) + " 197281");
		Statistics.reset();

		chessBoard = ChessBoardUtil.getNewCB();
		System.out.println(qperft(chessBoard, 5) + " 4865609");
		System.out.println(System.currentTimeMillis() - Statistics.startTime);
		Statistics.reset();

		chessBoard = ChessBoardUtil.getNewCB();
		System.out.println(qperft(chessBoard, 6) + " 119060324");
		System.out.println(System.currentTimeMillis() - Statistics.startTime);

		chessBoard = ChessBoardUtil.getNewCB();
		System.out.println(qperft(chessBoard, 7) + " 3195901860");
		System.out.println(System.currentTimeMillis() - Statistics.startTime);
	}

}
