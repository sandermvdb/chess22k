package nl.s22k.chess.search;

import nl.s22k.chess.move.MoveWrapper;

public class SearchTestUtil {

	private static final int MARGIN = 500;

	public static void testTTValues(int score, int bestScore, int depth, int bestMove, int flag, long ttValue, int ply) {
		if (ttValue != 0 && TTUtil.getDepth(ttValue) == depth) {
			score = TTUtil.getScore(ttValue, ply);
			if (TTUtil.getFlag(ttValue) == TTUtil.FLAG_EXACT && flag == TTUtil.FLAG_EXACT) {
				if (score != bestScore) {
					System.out.println(String.format("exact-exact: TT-score %s != bestScore %s", score, bestScore));
				}
				int move = TTUtil.getMove(ttValue);
				if (move != bestMove) {
					throw new RuntimeException(String.format("Error: TT-move %s != bestMove %s", new MoveWrapper(move), new MoveWrapper(bestMove)));
				}
			} else if (TTUtil.getFlag(ttValue) == TTUtil.FLAG_LOWER && flag == TTUtil.FLAG_EXACT) {
				if (score - MARGIN > bestScore) {
					System.out.println(String.format("lower-exact: TT-score %s > bestScore %s", score, bestScore));
				}
			} else if (TTUtil.getFlag(ttValue) == TTUtil.FLAG_UPPER && flag == TTUtil.FLAG_EXACT) {
				if (score + MARGIN < bestScore) {
					System.out.println(String.format("upper-exact: TT-score %s < bestScore %s", score, bestScore));
				}
			} else if (TTUtil.getFlag(ttValue) == TTUtil.FLAG_EXACT && flag == TTUtil.FLAG_LOWER) {
				if (score + MARGIN < bestScore) {
					System.out.println(String.format("exact-lower: TT-score %s < bestScore %s", score, bestScore));
				}
			} else if (TTUtil.getFlag(ttValue) == TTUtil.FLAG_EXACT && flag == TTUtil.FLAG_UPPER) {
				if (score - MARGIN > bestScore) {
					System.out.println(String.format("exact-upper: TT-score %s > bestScore %s", score, bestScore));
				}
			} else if (TTUtil.getFlag(ttValue) == TTUtil.FLAG_UPPER && flag == TTUtil.FLAG_LOWER) {
				if (score + MARGIN < bestScore) {
					System.out.println(String.format("upper-lower: TT-score %s < bestScore %s", score, bestScore));
				}
			} else if (TTUtil.getFlag(ttValue) == TTUtil.FLAG_LOWER && flag == TTUtil.FLAG_UPPER) {
				if (score - MARGIN > bestScore) {
					System.out.println(String.format("lower-upper: TT-score %s > bestScore %s", score, bestScore));
				}
			}
		}

	}

}
