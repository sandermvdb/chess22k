package nl.s22k.chess.maintests;

import nl.s22k.chess.Assert;
import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.ChessBoardInstances;
import nl.s22k.chess.ChessBoardUtil;
import nl.s22k.chess.engine.EngineConstants;
import nl.s22k.chess.move.MoveGenerator;
import nl.s22k.chess.move.MoveWrapper;
import nl.s22k.chess.search.ThreadData;

public class QPerft {

	private static ThreadData threadData = ThreadData.getInstance(0);
	private static ChessBoard cb = ChessBoardInstances.get(0);

	public static void main(String args[]) {
		if (!EngineConstants.GENERATE_BR_PROMOTIONS) {
			throw new RuntimeException("Generation of underpromotions must be enabled");
		}

		testPerft1();
		testPerft2();
		testPerft3();
		testPerft4();
		testPerft5();
		long now = System.currentTimeMillis();
		testPerft6();
		System.out.println((System.currentTimeMillis() - now) + " rofchade = 800");
		testPerft7();
		// testPerft8();
	}

	public static long qperft(final ChessBoard cb, final int depth) {

		threadData.startPly();
		MoveGenerator.generateMoves(threadData, cb);
		MoveGenerator.generateAttacks(threadData, cb);

		long counter = 0;
		if (depth == 1) {
			while (threadData.hasNext()) {
				if (cb.isLegal(threadData.next())) {
					counter++;
				}
			}
			threadData.endPly();
			return counter;
		}

		while (threadData.hasNext()) {
			final int move = threadData.next();
			if (!cb.isLegal(move)) {
				continue;
			}
			cb.doMove(move);
			counter += qperft(cb, depth - 1);
			cb.undoMove(move);
		}

		threadData.endPly();
		return counter;

	}

	public static long qdivide(final ChessBoard cb, final int depth) {

		threadData.startPly();
		MoveGenerator.generateMoves(threadData, cb);
		int counter = 0;
		while (threadData.hasNext()) {
			final int move = threadData.next();
			if (depth == 1) {
				System.out.println(new MoveWrapper(move) + ": " + 1);
				counter++;
				continue;
			}
			cb.doMove(move);
			final long divideCounter = qperft(cb, depth - 1);
			counter += divideCounter;
			cb.undoMove(move);
			System.out.println(new MoveWrapper(move) + ": " + divideCounter);
		}

		return counter;

	}

	public static void testPerft1() {
		System.out.println(1);
		ChessBoardUtil.setStartFen(cb);
		Assert.isTrue(20 == qperft(cb, 1));
	}

	public static void testPerft2() {
		System.out.println(2);
		ChessBoardUtil.setStartFen(cb);
		Assert.isTrue(400 == qperft(cb, 2));
	}

	public static void testPerft3() {
		System.out.println(3);
		ChessBoardUtil.setStartFen(cb);
		Assert.isTrue(8902 == qperft(cb, 3));
	}

	public static void testPerft4() {
		System.out.println(4);
		ChessBoardUtil.setStartFen(cb);
		Assert.isTrue(197281 == qperft(cb, 4));
	}

	public static void testPerft5() {
		System.out.println(5);
		ChessBoardUtil.setStartFen(cb);
		Assert.isTrue(4865609 == qperft(cb, 5));
	}

	public static void testPerft6() {
		System.out.println(6);
		ChessBoardUtil.setStartFen(cb);
		Assert.isTrue(119060324 == qperft(cb, 6));
	}

	public static void testPerft7() {
		System.out.println(7);
		ChessBoardUtil.setStartFen(cb);
		Assert.isTrue(3195901860L == qperft(cb, 7));
	}

	public static void testPerft8() {
		System.out.println(8);
		ChessBoardUtil.setStartFen(cb);
		Assert.isTrue(84998978956L == qperft(cb, 8));
	}

}
