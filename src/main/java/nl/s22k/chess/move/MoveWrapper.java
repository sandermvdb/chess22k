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
	public boolean isEP = false;

	public MoveWrapper(int move) {
		this.move = move;

		fromIndex = MoveUtil.getFromIndex(move);
		fromFile = (char) (104 - fromIndex % 8);
		fromRank = fromIndex / 8 + 1;

		toIndex = MoveUtil.getToIndex(move);
		toFile = (char) (104 - toIndex % 8);
		toRank = toIndex / 8 + 1;

		score = MoveUtil.getScore(move);

		if (MoveUtil.isPromotion(move)) {
			if (MoveUtil.isNightPromotion(move)) {
				isNightPromotion = true;
			} else {
				isQueenPromotion = true;
			}
		} else {
			if (MoveUtil.isEP(move)) {
				isEP = true;
			}
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
				if (moveString.length() == 5 && moveString.substring(4, 5).equals("n")) {
					isNightPromotion = true;
					move = MoveUtil.createNightPromotionMove(fromIndex, toIndex);
				} else {
					move = MoveUtil.createPromotionMove(fromIndex, toIndex);
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
				if (moveString.length() == 5 && moveString.substring(4, 5).equals("n")) {
					isNightPromotion = true;
					move = MoveUtil.createNightPromotionAttack(fromIndex, toIndex, zkAttackIndex);
				} else {
					move = MoveUtil.createPromotionAttack(fromIndex, toIndex, zkAttackIndex);
				}
			} else {
				move = MoveUtil.createAttackMove(fromIndex, toIndex, zkSourceIndex, zkAttackIndex);
			}
		}
	}

	@Override
	public String toString() {
		String moveString = "" + fromFile + fromRank + toFile + toRank;
		if (isNightPromotion) {
			return moveString + "n";
		}
		if (isQueenPromotion) {
			return moveString + "q";
		}
		return moveString;
	}

	@Override
	public boolean equals(Object obj) {
		MoveWrapper compare = (MoveWrapper) obj;
		return compare.toString().equals(toString());
	}

}
