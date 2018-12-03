package nl.s22k.chess.unittests;

import java.util.Random;

import org.junit.BeforeClass;
import org.junit.Test;

import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.ChessBoardUtil;
import nl.s22k.chess.ChessConstants;
import nl.s22k.chess.move.MoveUtil;
import nl.s22k.chess.search.TTUtil;

public class TTTest {

	@BeforeClass
	public static void init() {
		TTUtil.init(true);
	}

	@Test
	public void testKey() {
		TTUtil.halfMoveCounter = 400;

		short score = 30000;
		int depth = 10;
		int flag = TTUtil.FLAG_LOWER;
		int move = MoveUtil.createMove(10, 20, 3);
		System.out.println("move: " + move);

		long value = TTUtil.createValue(score, move, flag, depth);

		System.out.println("score: " + TTUtil.getScore(value, 0));
		System.out.println("depth: " + TTUtil.getDepth(value));
		System.out.println("flag: " + TTUtil.getFlag(value));
		System.out.println("move: " + TTUtil.getMove(value));

		Random r = new Random();
		long zk = r.nextLong();
		TTUtil.addValue(zk, score, 1, depth, flag, move);

		long ttValue = TTUtil.getTTValue(zk);
		System.out.println("score: " + TTUtil.getScore(ttValue, 0));
		System.out.println("depth: " + TTUtil.getDepth(ttValue));
		System.out.println("flag: " + TTUtil.getFlag(ttValue));
		System.out.println("move: " + TTUtil.getMove(ttValue));
	}

	@Test
	public void getZKPawnMove() {
		ChessBoard cb = ChessBoardUtil.getNewCB();
		int move = MoveUtil.createMove(8, 24, ChessConstants.PAWN);
		cb.doMove(move);
	}

	@Test
	public void getBlackZKPawnMove() {
		ChessBoard cb = ChessBoardUtil.getNewCB();
		cb.colorToMove = ChessConstants.BLACK;
		ChessBoardUtil.init(cb);
		int move = MoveUtil.createMove(48, 40, ChessConstants.PAWN);
		cb.doMove(move);
		cb.undoMove(move);
	}

	@Test
	public void getBlackZKRookAttackMove() {
		ChessBoard cb = ChessBoardUtil.getNewCB();
		cb.colorToMove = ChessConstants.BLACK;
		ChessBoardUtil.init(cb);
		int move = MoveUtil.createAttackMove(63, 7, ChessConstants.ROOK, ChessConstants.ROOK);
		cb.doMove(move);
		cb.undoMove(move);
	}

	// @Test
	public void getZKKingMove() {
		ChessBoard cb = ChessBoardUtil.getNewCB();
		int move = MoveUtil.createMove(3, 19, ChessConstants.KING);
		cb.doMove(move);
	}

	// @Test
	public void getZKAttackMove() {
		ChessBoard cb = ChessBoardUtil.getNewCB();
		int move = MoveUtil.createAttackMove(3, 48, ChessConstants.KING, ChessConstants.PAWN);
		cb.doMove(move);
		cb.undoMove(move);
	}

	// @Test
	public void getZKPromotionMove() {
		ChessBoard cb = ChessBoardUtil.getNewCB();
		int move = MoveUtil.createAttackMove(8, 56, ChessConstants.PAWN, ChessConstants.ROOK);
		cb.doMove(move);
		cb.undoMove(move);
	}

	// @Test
	public void getZKCastlingMove() {
		ChessBoard cb = ChessBoardUtil.getNewCB();
		int move = MoveUtil.createCastlingMove(3, 1);
		cb.doMove(move);
		cb.undoMove(move);
	}

}
