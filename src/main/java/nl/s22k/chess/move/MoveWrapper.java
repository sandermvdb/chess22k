package nl.s22k.chess.move;

import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.ChessConstants;
import nl.s22k.chess.Util;

public class MoveWrapper {

	public int fromRank;
	public char fromFile;

	/** 1 to 8 */
	public int toRank;

	/** a to h */
	public char toFile;

	public int fromIndex;
	public int toIndex;
	public int move;
	public int score;

	public boolean isNightPromotion = false;
	public boolean isQueenPromotion = false;
	public boolean isRookPromotion = false;
	public boolean isBishopPromotion = false;

	public boolean isEP = false;
	public boolean isCastling = false;

	public MoveWrapper(int move) {
		this.move = move;

		fromIndex = MoveUtil.getFromIndex(move);
		fromFile = (char) (104 - fromIndex % 8);
		fromRank = fromIndex / 8 + 1;

		toIndex = MoveUtil.getToIndex(move);
		toFile = (char) (104 - toIndex % 8);
		toRank = toIndex / 8 + 1;

		score = MoveUtil.getScore(move);

		switch (MoveUtil.getMoveType(move)) {
		case MoveUtil.NORMAL:
			break;
		case MoveUtil.CASTLING:
			isCastling = true;
			break;
		case MoveUtil.EP:
			isEP = true;
			break;
		case MoveUtil.PROMOTION_B:
			isBishopPromotion = true;
			break;
		case MoveUtil.PROMOTION_N:
			isNightPromotion = true;
			break;
		case MoveUtil.PROMOTION_Q:
			isQueenPromotion = true;
			break;
		case MoveUtil.PROMOTION_R:
			isRookPromotion = true;
			break;
		default:
			throw new RuntimeException("Unknown movetype: " + MoveUtil.getMoveType(move));
		}

	}

	public MoveWrapper(String moveString, ChessBoard cb) {

		fromFile = moveString.charAt(0);
		fromRank = Integer.parseInt(moveString.substring(1, 2));
		fromIndex = (fromRank - 1) * 8 + 104 - fromFile;

		toFile = moveString.charAt(2);
		toRank = Integer.parseInt(moveString.substring(3, 4));
		toIndex = (toRank - 1) * 8 + 104 - toFile;

		int zkSourceIndex = 0;
		int zkAttackIndex = 0;

		//@formatter:off
		zkSourceIndex = 
				  (cb.pieces[cb.colorToMove][ChessConstants.PAWN] & Util.POWER_LOOKUP[fromIndex]) != 0 ? ChessConstants.PAWN
				: (cb.pieces[cb.colorToMove][ChessConstants.BISHOP] & Util.POWER_LOOKUP[fromIndex]) != 0 ? ChessConstants.BISHOP
				: (cb.pieces[cb.colorToMove][ChessConstants.NIGHT] & Util.POWER_LOOKUP[fromIndex]) != 0 ? ChessConstants.NIGHT
				: (cb.pieces[cb.colorToMove][ChessConstants.KING] & Util.POWER_LOOKUP[fromIndex]) != 0 ? ChessConstants.KING
				: (cb.pieces[cb.colorToMove][ChessConstants.QUEEN] & Util.POWER_LOOKUP[fromIndex]) != 0 ? ChessConstants.QUEEN
				: (cb.pieces[cb.colorToMove][ChessConstants.ROOK] & Util.POWER_LOOKUP[fromIndex]) != 0 ? ChessConstants.ROOK 
				: -1;
		if (zkSourceIndex == -1) {
			throw new RuntimeException("Source piece not found at index " + fromIndex);
		}

		zkAttackIndex = 
				  (cb.pieces[cb.colorToMoveInverse][ChessConstants.PAWN] & Util.POWER_LOOKUP[toIndex]) != 0 ? ChessConstants.PAWN
				: (cb.pieces[cb.colorToMoveInverse][ChessConstants.BISHOP] & Util.POWER_LOOKUP[toIndex]) != 0 ? ChessConstants.BISHOP
				: (cb.pieces[cb.colorToMoveInverse][ChessConstants.NIGHT] & Util.POWER_LOOKUP[toIndex]) != 0 ? ChessConstants.NIGHT
				: (cb.pieces[cb.colorToMoveInverse][ChessConstants.KING] & Util.POWER_LOOKUP[toIndex]) != 0 ? ChessConstants.KING
				: (cb.pieces[cb.colorToMoveInverse][ChessConstants.QUEEN] & Util.POWER_LOOKUP[toIndex]) != 0 ? ChessConstants.QUEEN
				: (cb.pieces[cb.colorToMoveInverse][ChessConstants.ROOK] & Util.POWER_LOOKUP[toIndex]) != 0 ? ChessConstants.ROOK 
				: 0;
		//@formatter:on

		if (zkAttackIndex == 0) {
			if (zkSourceIndex == ChessConstants.PAWN && (toRank == 1 || toRank == 8)) {
				if (moveString.length() == 5) {
					if (moveString.substring(4, 5).equals("n")) {
						isNightPromotion = true;
						move = MoveUtil.createPromotionMove(MoveUtil.PROMOTION_N, fromIndex, toIndex);
					} else if (moveString.substring(4, 5).equals("r")) {
						isRookPromotion = true;
						move = MoveUtil.createPromotionMove(MoveUtil.PROMOTION_R, fromIndex, toIndex);
					} else if (moveString.substring(4, 5).equals("b")) {
						isBishopPromotion = true;
						move = MoveUtil.createPromotionMove(MoveUtil.PROMOTION_B, fromIndex, toIndex);
					} else if (moveString.substring(4, 5).equals("q")) {
						isQueenPromotion = true;
						move = MoveUtil.createPromotionMove(MoveUtil.PROMOTION_Q, fromIndex, toIndex);
					}
				} else {
					isQueenPromotion = true;
					move = MoveUtil.createPromotionMove(MoveUtil.PROMOTION_Q, fromIndex, toIndex);
				}
			} else {
				if (zkSourceIndex == ChessConstants.KING && (fromIndex - toIndex == 2 || fromIndex - toIndex == -2)) {
					// castling
					move = MoveUtil.createCastlingMove(fromIndex, toIndex);
				} else if (zkSourceIndex == ChessConstants.PAWN && toIndex % 8 != fromIndex % 8) {
					// ep
					move = MoveUtil.createEPMove(fromIndex, toIndex);
				} else {
					move = MoveUtil.createMove(fromIndex, toIndex, zkSourceIndex);
				}
			}
		} else {
			if (zkSourceIndex == ChessConstants.PAWN && (toRank == 1 || toRank == 8)) {
				if (moveString.length() == 5) {
					if (moveString.substring(4, 5).equals("n")) {
						isNightPromotion = true;
						move = MoveUtil.createPromotionAttack(MoveUtil.PROMOTION_N, fromIndex, toIndex, zkAttackIndex);
					} else if (moveString.substring(4, 5).equals("r")) {
						isRookPromotion = true;
						move = MoveUtil.createPromotionAttack(MoveUtil.PROMOTION_R, fromIndex, toIndex, zkAttackIndex);
					} else if (moveString.substring(4, 5).equals("b")) {
						isBishopPromotion = true;
						move = MoveUtil.createPromotionAttack(MoveUtil.PROMOTION_B, fromIndex, toIndex, zkAttackIndex);
					} else if (moveString.substring(4, 5).equals("q")) {
						isQueenPromotion = true;
						move = MoveUtil.createPromotionAttack(MoveUtil.PROMOTION_Q, fromIndex, toIndex, zkAttackIndex);
					}
				} else {
					move = MoveUtil.createPromotionAttack(MoveUtil.PROMOTION_Q, fromIndex, toIndex, zkAttackIndex);
				}
			} else {
				move = MoveUtil.createAttackMove(fromIndex, toIndex, zkSourceIndex, zkAttackIndex);
			}
		}
	}

	@Override
	public String toString() {
		String moveString = "" + fromFile + fromRank + toFile + toRank;
		if (isQueenPromotion) {
			return moveString + "q";
		} else if (isNightPromotion) {
			return moveString + "n";
		} else if (isRookPromotion) {
			return moveString + "r";
		} else if (isBishopPromotion) {
			return moveString + "b";
		}
		return moveString;
	}

	@Override
	public boolean equals(Object obj) {
		MoveWrapper compare = (MoveWrapper) obj;
		return compare.toString().equals(toString());
	}

}
