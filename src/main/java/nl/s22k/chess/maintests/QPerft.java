package nl.s22k.chess.maintests;

import org.junit.BeforeClass;
import org.junit.Test;

import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.ChessBoardUtil;
import nl.s22k.chess.move.MagicUtil;
import nl.s22k.chess.move.MoveGenerator;
import nl.s22k.chess.move.MoveList;
import nl.s22k.chess.move.MoveWrapper;

public class QPerft {

	@BeforeClass
	public static void init() {
		MagicUtil.init();
		System.out.println("Do not forget to enable bishop- and rook-promotions!");
	}

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

	@Test
	public void testPerft1() {
		ChessBoard chessBoard = ChessBoardUtil.getNewCB();
		assert qperft(chessBoard, 1) == 20;
	}

	@Test
	public void testPerft2() {
		ChessBoard chessBoard = ChessBoardUtil.getNewCB();
		assert qperft(chessBoard, 2) == 400;
	}

	@Test
	public void testPerft3() {
		ChessBoard chessBoard = ChessBoardUtil.getNewCB();
		assert qperft(chessBoard, 3) == 8902;
	}

	@Test
	public void testPerft4() {
		ChessBoard chessBoard = ChessBoardUtil.getNewCB();
		assert qperft(chessBoard, 4) == 197281;
	}

	@Test
	public void testPerft5() {
		ChessBoard chessBoard = ChessBoardUtil.getNewCB();
		assert qperft(chessBoard, 5) == 4865609;
	}

	@Test
	public void testPerft6() {
		ChessBoard chessBoard = ChessBoardUtil.getNewCB();
		assert qperft(chessBoard, 6) == 119060324;
	}

	@Test
	public void testPerft7() {
		ChessBoard chessBoard = ChessBoardUtil.getNewCB();
		assert qperft(chessBoard, 7) == 3195901860L;
	}

	// @Test
	// public void testPerft8() {
	// ChessBoard chessBoard = ChessBoardUtil.getNewCB();
	// assert qperft(chessBoard, 8) == 84998978956L;
	// }

}
