package nl.s22k.chess.unittests;

import org.junit.BeforeClass;
import org.junit.Test;

import nl.s22k.chess.ChessConstants;
import nl.s22k.chess.move.MagicUtil;
import nl.s22k.chess.move.MoveList;
import nl.s22k.chess.move.MoveUtil;
import nl.s22k.chess.search.HeuristicUtil;

public class MoveTest {

	@BeforeClass
	public static void init() {
		MagicUtil.init();
	}

	@Test
	public void regularMove() {
		int move = MoveUtil.createMove(10, 5, 4);
		System.out.println(MoveUtil.getFromIndex(move) + " 10");
		System.out.println(MoveUtil.getToIndex(move) + " 5");
		System.out.println(MoveUtil.getSourcePieceIndex(move) + " 4");
		System.out.println(MoveUtil.getAttackedPieceIndex(move) + " 0");
		System.out.println(MoveUtil.isPromotion(move) + " false");
		System.out.println("");
	}

	@Test
	public void attackMove() {
		System.out.println("Attack-move");
		int move = MoveUtil.createAttackMove(10, 46, 4, 5);
		System.out.println(MoveUtil.getFromIndex(move) + " 10");
		System.out.println(MoveUtil.getToIndex(move) + " 46");
		System.out.println(MoveUtil.getSourcePieceIndex(move) + " 4");
		System.out.println(MoveUtil.getAttackedPieceIndex(move) + " 5");
		System.out.println(MoveUtil.isPromotion(move) + " false");
		move = MoveUtil.setSeeMove(move, 400);
		System.out.println(MoveUtil.getScore(move) + " 100");
		move = MoveUtil.setSeeMove(move, -400);
		System.out.println(MoveUtil.getScore(move) + " -100");

		System.out.println("");
	}

	@Test
	public void promotionMove() {
		int move = MoveUtil.createPromotionMove(MoveUtil.PROMOTION_Q, 10, 61);
		System.out.println(MoveUtil.getFromIndex(move) + " 10");
		System.out.println(MoveUtil.getToIndex(move) + " 61");
		System.out.println(MoveUtil.getSourcePieceIndex(move) + " 1");
		System.out.println(MoveUtil.getAttackedPieceIndex(move) + " 0");
		System.out.println(MoveUtil.isPromotion(move) + " true");
		System.out.println(MoveUtil.isPromotion(move) + " true");
		System.out.println("");
	}

	@Test
	public void promotionAttackMove() {
		int move = MoveUtil.createPromotionAttack(MoveUtil.PROMOTION_Q, 10, 61, ChessConstants.ROOK);
		System.out.println(MoveUtil.getFromIndex(move) + " 10");
		System.out.println(MoveUtil.getToIndex(move) + " 61");
		System.out.println(MoveUtil.getSourcePieceIndex(move) + " 1");
		System.out.println(MoveUtil.getAttackedPieceIndex(move) + " 4");
		System.out.println(MoveUtil.isPromotion(move) + " true");
		System.out.println("");
	}

	@Test
	public void epAttackMove() {
		int move = MoveUtil.createEPMove(36, 43);
		System.out.println(MoveUtil.getFromIndex(move) + " 36");
		System.out.println(MoveUtil.getToIndex(move) + " 43");
		System.out.println(MoveUtil.getFromToIndex(move) + " 2788");
		System.out.println(MoveUtil.getSourcePieceIndex(move) + " 1");
		System.out.println(MoveUtil.getAttackedPieceIndex(move) + " 1");
		System.out.println(MoveUtil.isPromotion(move) + " false");
		System.out.println((MoveUtil.getMoveType(move) == MoveUtil.EP) + " true");
		System.out.println("");
	}

	@Test
	public void hhTest() {
		System.out.println("HH-test");

		int fromIndex = 0;
		int toIndex = 1;

		int move = MoveUtil.createMove(fromIndex, toIndex, ChessConstants.PAWN);
		HeuristicUtil.HH_MOVES[ChessConstants.WHITE][MoveUtil.getFromToIndex(move)] = 2000000;
		HeuristicUtil.BF_MOVES[ChessConstants.WHITE][MoveUtil.getFromToIndex(move)] = 2;
		MoveList.addMove(move);

		System.out.println("HH-score: " + MoveUtil.getScore(move));
		System.out.println("HH-score: " + MoveUtil.getScore(MoveList.next()));
		System.out.println("");
	}

	@Test
	public void killerTest() {
		System.out.println("Killer-test");

		int fromIndex = 0;
		int toIndex = 1;

		int move = MoveUtil.createMove(fromIndex, toIndex, ChessConstants.PAWN);
		HeuristicUtil.addKillerMove(move, 0);
		MoveList.addMove(move);

		System.out.println("Killer-score: " + MoveUtil.getScore(move));
		move = MoveList.next();
		System.out.println("Killer-score: " + MoveUtil.getScore(move));
		System.out.println("");
	}

}
