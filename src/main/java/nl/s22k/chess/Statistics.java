package nl.s22k.chess;

import java.util.Arrays;
import java.util.stream.IntStream;

import nl.s22k.chess.engine.EngineConstants;
import nl.s22k.chess.eval.EvalCache;
import nl.s22k.chess.eval.PawnEvalCache;
import nl.s22k.chess.move.TreeMove;
import nl.s22k.chess.search.TTUtil;

public class Statistics {

	public static final boolean ENABLED = false;

	public static boolean panic = false;
	public static long startTime = System.nanoTime();
	public static long evalNodes, abNodes, seeNodes, pvNodes, cutNodes, allNodes;
	public static long ttHits, ttMisses;
	public static int staleMateCount, mateCount;
	public static int depth, maxDepth;
	public static TreeMove bestMove;
	public static int epCount, castleCount, promotionCount;
	public static long pawnEvalCacheHits, pawnEvalCacheMisses;
	public static int bestMoveTT, bestMoveTTLower, bestMoveTTUpper, bestMoveKiller1, bestMoveKiller2, bestMoveOther, bestMovePromotion, bestMoveWinningCapture,
			bestMoveLosingCapture;
	public static int repetitions, repetitionTests;
	public static int checkExtensions, endGameExtensions;
	public static int nullMoveHit, nullMoveMiss, nullMoveFailed;
	public static long lmrMoveHit, lmrMoveMiss, lmrFailed;
	public static long pvsMoveHit, pvsMoveMiss;
	public static long evalCacheHits, evalCacheMisses;
	public static int iidCount;
	public static long moveCount;
	public static long movesGenerated;
	public static int drawByMaterialCount;
	public static int mateThreat;
	public static final int[] razoringHit = new int[10];
	public static final int[] razorFailed = new int[10];
	public static final int[] futilityPruningHit = new int[10];
	public static final int[] futileFailed = new int[10];
	public static final int[] staticNullMoveFailed = new int[10];
	public static final int[] staticNullMovePruningHit = new int[10];
	public static int drawishByMaterialCount;

	public static long calculateNps() {
		return moveCount * 1000 / Math.max(getPassedTimeMs(), 1);
	}

	public static void reset() {
		Arrays.fill(razorFailed, 0);
		Arrays.fill(razoringHit, 0);
		Arrays.fill(futileFailed, 0);
		Arrays.fill(futilityPruningHit, 0);
		Arrays.fill(staticNullMoveFailed, 0);
		Arrays.fill(staticNullMovePruningHit, 0);
		lmrFailed = 0;
		nullMoveFailed = 0;
		pvNodes = 1; // so we never divide by zero
		cutNodes = 0;
		allNodes = 0;
		drawishByMaterialCount = 0;
		mateThreat = 0;
		drawByMaterialCount = 0;
		pawnEvalCacheMisses = 0;
		pawnEvalCacheHits = 0;
		movesGenerated = 0;
		moveCount = 0;
		bestMove = null;
		startTime = System.nanoTime();
		castleCount = 0;
		epCount = 0;
		evalNodes = 0;
		ttHits = 0;
		ttMisses = 0;
		staleMateCount = 0;
		mateCount = 0;
		depth = 0;
		maxDepth = 0;
		abNodes = 0;
		promotionCount = 0;
		seeNodes = 0;
		repetitions = 0;
		nullMoveHit = 0;
		nullMoveMiss = 0;
		lmrMoveHit = 0;
		lmrMoveMiss = 0;
		pvsMoveHit = 0;
		pvsMoveMiss = 0;
		bestMoveTT = 0;
		bestMoveTTLower = 0;
		bestMoveTTUpper = 0;
		bestMoveKiller1 = 0;
		bestMoveKiller2 = 0;
		bestMoveOther = 0;
		bestMovePromotion = 0;
		bestMoveWinningCapture = 0;
		bestMoveLosingCapture = 0;
		checkExtensions = 0;
		endGameExtensions = 0;
		repetitionTests = 0;
		evalCacheHits = 0;
		evalCacheMisses = 0;
		iidCount = 0;
		panic = false;
	}

	public static void print() {
		if (!Statistics.ENABLED) {
			return;
		}
		System.out.println("Time          " + getPassedTimeMs() + "ms");
		if (bestMove != null) {
			System.out.println("Bestmove      " + bestMove.toString());
			System.out.println("Score         " + bestMove.score);
		}
		System.out.println("NPS           " + calculateNps() / 1000 + "k");
		System.out.println("AB-nodes      " + abNodes);
		System.out.println("PV-nodes      " + pvNodes + " = 1/" + (pvNodes + cutNodes + allNodes) / pvNodes);
		System.out.println("Cut-nodes     " + cutNodes);
		System.out.println("All-nodes     " + allNodes);
		System.out.println("See-nodes     " + seeNodes);
		System.out.println("Evaluated     " + evalNodes);
		System.out.println("Moves         " + moveCount + "/" + movesGenerated);

		printPercentage(ttHits, ttMisses, "TT            ");
		if (TTUtil.maxEntries != 0) {
			System.out.println("TT-usage      " + TTUtil.usageCounter * 100 / (TTUtil.maxEntries * 2) + "%");
		}

		printPercentage(evalCacheHits, evalCacheMisses, "Evalcache     ");
		System.out.println("Usage         " + EvalCache.usageCounter * 100 / EvalCache.MAX_TABLE_ENTRIES + "%");

		printPercentage(pawnEvalCacheHits, pawnEvalCacheMisses, "PEvalcache    ");
		System.out.println("Usage         " + PawnEvalCache.usageCounter * 100 / PawnEvalCache.MAX_TABLE_ENTRIES + "%");

		System.out.println("Depth         " + depth + "/" + maxDepth);
		System.out.println("TT-best       " + bestMoveTT);
		System.out.println("TT-upper-best " + bestMoveTTUpper);
		System.out.println("TT-lower-best " + bestMoveTTLower);
		System.out.println("Promo-best    " + bestMovePromotion);
		System.out.println("Win-cap-best  " + bestMoveWinningCapture);
		System.out.println("Killer1-best  " + bestMoveKiller1);
		System.out.println("Killer2-best  " + bestMoveKiller2);
		System.out.println("Other-best    " + bestMoveOther);
		System.out.println("Los-cap-best  " + bestMoveLosingCapture);
		System.out.println("Checkmate     " + mateCount);
		System.out.println("Stalemate     " + staleMateCount);
		System.out.println("Repetitions   " + repetitions + "(" + repetitionTests + ")");
		System.out.println("Draw-by-mtrl  " + drawByMaterialCount);
		System.out.println("Drawish-mtrl  " + drawishByMaterialCount);
		System.out.println("Check ext.    " + checkExtensions);
		System.out.println("Endgame ext.  " + endGameExtensions);
		System.out.println("Mate-threat   " + mateThreat);
		System.out.println("IID           " + iidCount);
		System.out.println("Panic         " + panic);

		printPercentage(nullMoveHit, nullMoveMiss, "Null-move     ");
		if (EngineConstants.TEST_NULL_MOVE) {
			printPercentage(nullMoveFailed, nullMoveHit - nullMoveFailed, "Null failed   ");
		}
		printPercentage(lmrMoveHit, lmrMoveMiss, "LMR-move      ");
		if (EngineConstants.TEST_LMR) {
			printPercentage(lmrFailed, lmrMoveHit - lmrFailed, "LMR failed    ");
		}
		printPercentage(pvsMoveHit, pvsMoveMiss, "PVS-move      ");

		if (EngineConstants.TEST_RAZORING) {
			printDepthPromiles(razorFailed, razoringHit, "Razor failed  ");
		}
		if (EngineConstants.TEST_FUTILITY_PRUNING) {
			printDepthPromiles(futileFailed, futilityPruningHit, "Futile failed ");
		}
		if (EngineConstants.TEST_STATIC_NULLMOVE) {
			printDepthPromiles(staticNullMoveFailed, staticNullMovePruningHit, "S-null failed ");
		}
	}

	private static void printDepthPromiles(int[] failed, int[] total, String message) {
		printPromile(IntStream.of(failed).sum(), IntStream.of(total).sum() - IntStream.of(failed).sum(), message);
		for (int i = 0; i < failed.length; i++) {
			printPromile(failed[i], total[i] - failed[i], i + " ");
		}
	}

	private static void printPromile(long hitCount, long failCount, String message) {
		if (hitCount != 0 && failCount != 0) {
			System.out.println(message + hitCount + "/" + (failCount + hitCount) + " (" + hitCount * 1000 / (hitCount + failCount) + "‰)");
		}
	}

	private static void printPercentage(long hitCount, long failCount, String message) {
		if (hitCount != 0 && failCount != 0) {
			System.out.println(message + hitCount + "/" + (failCount + hitCount) + " (" + hitCount * 100 / (hitCount + failCount) + "%)");
		}
	}

	public static long getPassedTimeMs() {
		return (System.nanoTime() - startTime) / 1000000;
	}

}
