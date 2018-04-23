package nl.s22k.chess;

import java.util.Arrays;
import java.util.stream.IntStream;

import nl.s22k.chess.eval.EvalCache;
import nl.s22k.chess.eval.MaterialCache;
import nl.s22k.chess.eval.PawnEvalCache;
import nl.s22k.chess.move.TreeMove;
import nl.s22k.chess.search.TTUtil;

public class Statistics {

	public static final boolean ENABLED = false;

	public static boolean panic = false;
	public static long startTime = System.nanoTime();
	public static long evalNodes, abNodes, seeNodes, pvNodes, cutNodes, allNodes, qNodes, evaluatedInCheck;
	public static long ttHits, ttMisses;
	public static int staleMateCount, mateCount;
	public static int depth, maxDepth;
	public static TreeMove bestMove;
	public static int epCount, castleCount, promotionCount;
	public static long pawnEvalCacheHits, pawnEvalCacheMisses;
	public static int materialCacheMisses, materialCacheHits;
	public static int bestMoveTT, bestMoveTTLower, bestMoveTTUpper, bestMoveKiller1, bestMoveKiller2, bestMoveKillerEvasive1, bestMoveKillerEvasive2,
			bestMoveOther, bestMovePromotion, bestMoveWinningCapture, bestMoveLosingCapture;
	public static int repetitions, repetitionTests;
	public static int checkExtensions, endGameExtensions;
	public static int nullMoveHit, nullMoveMiss;
	public static long lmrMoveHit, lmrMoveMiss;
	public static long pvsMoveHit, pvsMoveMiss;
	public static long evalCacheHits, evalCacheMisses;
	public static int iidCount;
	public static long moveCount;
	public static long movesGenerated;
	public static int drawByMaterialCount;
	public static final int[] razored = new int[10];
	public static final int[] futile = new int[10];
	public static final int[] staticNullMoved = new int[10];
	public static final int[] lmped = new int[10];
	public static final int[] failHigh = new int[64];
	public static int drawishByMaterialCount;

	public static long calculateNps() {
		return moveCount * 1000 / Math.max(getPassedTimeMs(), 1);
	}

	public static void reset() {
		Arrays.fill(razored, 0);
		Arrays.fill(futile, 0);
		Arrays.fill(staticNullMoved, 0);
		Arrays.fill(lmped, 0);
		Arrays.fill(failHigh, 0);

		evaluatedInCheck = 0;
		qNodes = 0;
		pvNodes = 1; // so we never divide by zero
		cutNodes = 0;
		allNodes = 0;
		drawishByMaterialCount = 0;
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
		bestMoveKillerEvasive1 = 0;
		bestMoveKillerEvasive2 = 0;
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
		System.out.println("Depth         " + depth + "/" + maxDepth);
		System.out.println("AB-nodes      " + abNodes);
		System.out.println("PV-nodes      " + pvNodes + " = 1/" + (pvNodes + cutNodes + allNodes) / pvNodes);
		System.out.println("Cut-nodes     " + cutNodes);
		printPercentage("Cut 1         ", failHigh[0], cutNodes - failHigh[0]);
		printPercentage("Cut 2         ", failHigh[1], cutNodes - failHigh[1]);
		printPercentage("Cut 3         ", failHigh[2], cutNodes - failHigh[2]);
		System.out.println("All-nodes     " + allNodes);
		System.out.println("Q-nodes       " + qNodes);
		System.out.println("See-nodes     " + seeNodes);
		System.out.println("Evaluated     " + evalNodes);
		System.out.println("Eval in check " + evaluatedInCheck);
		System.out.println("Moves         " + moveCount + "/" + movesGenerated);
		System.out.println("IID           " + iidCount);
		System.out.println("Panic         " + panic);

		System.out.println("### Caches #######");
		printPercentage("TT            ", ttHits, ttMisses);
		if (TTUtil.maxEntries != 0) {
			System.out.println("usage         " + TTUtil.usageCounter * 100 / (TTUtil.maxEntries * 2) + "%");
		}
		printPercentage("Eval          ", evalCacheHits, evalCacheMisses);
		System.out.println("usage         " + EvalCache.usageCounter * 100 / EvalCache.MAX_TABLE_ENTRIES + "%");
		printPercentage("Pawn eval     ", pawnEvalCacheHits, pawnEvalCacheMisses);
		System.out.println("usage         " + PawnEvalCache.usageCounter * 100 / PawnEvalCache.MAX_TABLE_ENTRIES + "%");
		printPercentage("Material      ", materialCacheHits, materialCacheMisses);
		System.out.println("usage         " + PawnEvalCache.usageCounter * 100 / MaterialCache.MAX_TABLE_ENTRIES + "%");

		System.out.println("## Best moves #####");
		System.out.println("TT            " + bestMoveTT);
		System.out.println("TT-upper      " + bestMoveTTUpper);
		System.out.println("TT-lower      " + bestMoveTTLower);
		System.out.println("Win-cap       " + bestMoveWinningCapture);
		System.out.println("Los-cap       " + bestMoveLosingCapture);
		System.out.println("Promo         " + bestMovePromotion);
		System.out.println("Killer1       " + bestMoveKiller1);
		System.out.println("Killer2       " + bestMoveKiller2);
		System.out.println("Killer1 evasi " + bestMoveKillerEvasive1);
		System.out.println("Killer2 evasi " + bestMoveKillerEvasive2);
		System.out.println("Other         " + bestMoveOther);

		System.out.println("### Outcome #####");
		System.out.println("Checkmate     " + mateCount);
		System.out.println("Stalemate     " + staleMateCount);
		System.out.println("Repetitions   " + repetitions + "(" + repetitionTests + ")");
		System.out.println("Draw-by-mtrl  " + drawByMaterialCount);
		System.out.println("Drawish-mtrl  " + drawishByMaterialCount);

		System.out.println("### Extensions #####");
		System.out.println("Check         " + checkExtensions);
		System.out.println("Endgame       " + endGameExtensions);

		System.out.println("### Pruning #####");
		printPercentage("Null-move     ", nullMoveHit, nullMoveMiss);
		printPercentage("LMR           ", lmrMoveHit, lmrMoveMiss);
		printPercentage("PVS           ", pvsMoveHit, pvsMoveMiss);
		printDepthTotals("Static nmp    ", staticNullMoved, false);
		printDepthTotals("Razored       ", razored, false);
		printDepthTotals("Futile        ", futile, false);
		printDepthTotals("LMP           ", lmped, false);
	}

	private static void printDepthTotals(String message, int[] values, boolean printDetails) {
		System.out.println(message + IntStream.of(values).sum());
		if (printDetails) {
			for (int i = 0; i < values.length; i++) {
				if (values[i] != 0) {
					System.out.println(i + " " + values[i]);
				}
			}
		}
	}

	private static void printPercentage(String message, long hitCount, long failCount) {
		if (hitCount != 0 && failCount != 0) {
			System.out.println(message + hitCount + "/" + (failCount + hitCount) + " (" + hitCount * 100 / (hitCount + failCount) + "%)");
		}
	}

	public static long getPassedTimeMs() {
		return (System.nanoTime() - startTime) / 1000000;
	}

}
