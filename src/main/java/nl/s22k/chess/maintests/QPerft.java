package nl.s22k.chess.maintests;

import org.junit.BeforeClass;
import org.junit.Test;

import nl.s22k.chess.Assert;
import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.ChessBoardUtil;
import nl.s22k.chess.move.MagicUtil;
import nl.s22k.chess.move.MoveGenerator;
import nl.s22k.chess.move.MoveWrapper;

public class QPerft {

	private static MoveGenerator moveGen = new MoveGenerator();

	@BeforeClass
	public static void init() {
		MagicUtil.init();
		System.out.println("Do not forget to enable bishop- and rook-promotions!");
	}

	public static long qperft(final ChessBoard chessBoard, final int depth) {

		moveGen.startPly();
		moveGen.generateMoves(chessBoard);
		moveGen.generateAttacks(chessBoard);

		long counter = 0;
		if (depth == 1) {
			while (moveGen.hasNext()) {
				if (chessBoard.isLegal(moveGen.next())) {
					counter++;
				}
			}
			moveGen.endPly();
			return counter;
		}

		while (moveGen.hasNext()) {
			final int move = moveGen.next();
			if (!chessBoard.isLegal(move)) {
				continue;
			}
			chessBoard.doMove(move);
			counter += qperft(chessBoard, depth - 1);
			chessBoard.undoMove(move);
		}

		moveGen.endPly();
		return counter;

	}

	public static long qdivide(final ChessBoard chessBoard, final int depth) {

		moveGen.startPly();
		moveGen.generateMoves(chessBoard);
		int counter = 0;
		while (moveGen.hasNext()) {
			final int move = moveGen.next();
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
		Assert.isTrue(20 == qperft(chessBoard, 1));
	}

	@Test
	public void testPerft2() {
		ChessBoard chessBoard = ChessBoardUtil.getNewCB();
		Assert.isTrue(400 == qperft(chessBoard, 2));
	}

	@Test
	public void testPerft3() {
		ChessBoard chessBoard = ChessBoardUtil.getNewCB();
		Assert.isTrue(8902 == qperft(chessBoard, 3));
	}

	@Test
	public void testPerft4() {
		ChessBoard chessBoard = ChessBoardUtil.getNewCB();
		Assert.isTrue(197281 == qperft(chessBoard, 4));
	}

	@Test
	public void testPerft5() {
		ChessBoard chessBoard = ChessBoardUtil.getNewCB();
		Assert.isTrue(4865609 == qperft(chessBoard, 5));
	}

	@Test
	public void testPerft6() {
		ChessBoard chessBoard = ChessBoardUtil.getNewCB();
		Assert.isTrue(119060324 == qperft(chessBoard, 6));
	}

	@Test
	public void testPerft7() {
		ChessBoard chessBoard = ChessBoardUtil.getNewCB();
		Assert.isTrue(3195901860L == qperft(chessBoard, 7));
	}

	// @Test
	// public void testPerft8() {
	// ChessBoard chessBoard = ChessBoardUtil.getNewCB();
	// assertEquals(84998978956L, qperft(chessBoard, 8));
	// }

}
