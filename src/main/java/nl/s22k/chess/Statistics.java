package nl.s22k.chess;

import nl.s22k.chess.eval.EvalCache;
import nl.s22k.chess.eval.PawnEvalCache;
import nl.s22k.chess.move.TreeMove;
import nl.s22k.chess.search.TTUtil;

public class Statistics {

	public static final boolean ENABLED = false;

	public static long startTime;
	public static long evalNodes, abNodes, seeNodes;
	public static long ttHits, ttMisses;
	public static int checkCount, staleMateCount, mateCount;
	public static int depth, maxDepth;
	public static TreeMove bestMove;
	public static int epCount, castleCount, promotionCount;
	public static int pawnEvalCacheHits, pawnEvalCacheMisses;
	public static int bestMoveTT, bestMoveKiller1, bestMoveKiller2, bestMoveOther, bestMovePromotion, bestMoveWinningCapture, bestMoveLosingCapture;
	public static int repetitions, repetitionTests;
	public static int extensions;
	public static int nullMoveHit, nullMoveFail;
	public static long lmrMoveHit, lmrMoveFail;
	public static long pvsMoveHit, pvsMoveFail;
	public static int evalCacheHits, evalCacheMisses;
	public static int iidCount;
	public static long moveCount;
	public static long movesGenerated;
	public static int drawByMaterialCount;
	public static int badBishopEndgameCount;
	public static int qChecks;

	public static int calculateNps() {
		return (int) (moveCount / (System.currentTimeMillis() - startTime)) * 1000;
	}

	public static void reset() {
		qChecks = 0;
		badBishopEndgameCount = 0;
		drawByMaterialCount = 0;
		pawnEvalCacheMisses = 0;
		pawnEvalCacheHits = 0;
		movesGenerated = 0;
		moveCount = 0;
		bestMove = null;
		startTime = System.currentTimeMillis() - 1;
		castleCount = 0;
		epCount = 0;
		evalNodes = 0;
		ttHits = 0;
		ttMisses = 0;
		checkCount = 0;
		staleMateCount = 0;
		mateCount = 0;
		depth = 0;
		maxDepth = 0;
		abNodes = 0;
		promotionCount = 0;
		seeNodes = 0;
		repetitions = 0;
		nullMoveHit = 0;
		nullMoveFail = 0;
		lmrMoveHit = 0;
		lmrMoveFail = 0;
		pvsMoveHit = 0;
		pvsMoveFail = 0;
		bestMoveTT = 0;
		bestMoveKiller1 = 0;
		bestMoveKiller2 = 0;
		bestMoveOther = 0;
		bestMovePromotion = 0;
		bestMoveWinningCapture = 0;
		bestMoveLosingCapture = 0;
		extensions = 0;
		repetitionTests = 0;
		evalCacheHits = 0;
		evalCacheMisses = 0;
		iidCount = 0;
	}

	public static void print() {
		if (!Statistics.ENABLED) {
			return;
		}
		System.out.println("Time        " + (System.currentTimeMillis() - startTime) + "ms");
		if (bestMove != null) {
			System.out.println("Bestmove    " + bestMove.toString());
			System.out.println("Score       " + bestMove.score);
		}
		System.out.println("NPS         " + calculateNps() / 1000 + "k");
		System.out.println("AB-nodes    " + abNodes);
		System.out.println("See-nodes   " + seeNodes);
		System.out.println("Evaluated   " + evalNodes);
		System.out.println("Q-Checks    " + qChecks);
		System.out.println("Moves       " + moveCount + "/" + movesGenerated);

		printPercentage(ttHits, ttMisses, "TT          ");
		System.out.println("TT-usage    " + TTUtil.usageCounter * 100 / TTUtil.MAX_TABLE_ENTRIES + "%");

		printPercentage(evalCacheHits, evalCacheMisses, "Evalcache   ");
		System.out.println("Usage       " + EvalCache.usageCounter * 100 / EvalCache.MAX_TABLE_ENTRIES + "%");

		printPercentage(pawnEvalCacheHits, pawnEvalCacheMisses, "PEvalcache  ");
		System.out.println("Usage       " + PawnEvalCache.usageCounter * 100 / PawnEvalCache.MAX_TABLE_ENTRIES + "%");

		System.out.println("Depth       " + depth + "/" + maxDepth);
		System.out.println("TT-best      " + bestMoveTT);
		System.out.println("Promo-best   " + bestMovePromotion);
		System.out.println("Win-cap-best " + bestMoveWinningCapture);
		System.out.println("Killer1-best " + bestMoveKiller1);
		System.out.println("Killer2-best " + bestMoveKiller2);
		System.out.println("Other-best   " + bestMoveOther);
		System.out.println("Los-cap-best " + bestMoveLosingCapture);

		printPercentage(nullMoveHit, nullMoveFail, "Null-move    ");
		printPercentage(lmrMoveHit, lmrMoveFail, "LMR-move     ");
		printPercentage(pvsMoveHit, pvsMoveFail, "PVS-move     ");

		System.out.println("Check,SM,CM  " + checkCount + ", " + staleMateCount + ", " + mateCount);
		System.out.println("Repetitions  " + repetitions + "(" + repetitionTests + ")");
		System.out.println("Draw-by-mtrl " + drawByMaterialCount);
		System.out.println("Bad bishop   " + badBishopEndgameCount);
		System.out.println("Extensions   " + extensions);
		System.out.println("IID          " + iidCount);
	}

	private static void printPercentage(long hitCount, long failCount, String message) {
		if (hitCount != 0 && failCount != 0) {
			System.out.println(message + hitCount + "/" + (failCount + hitCount) + " (" + hitCount * 100 / (hitCount + failCount) + "%)");
		}
	}

}
