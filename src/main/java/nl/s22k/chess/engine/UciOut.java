package nl.s22k.chess.engine;

import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.ChessBoardUtil;
import nl.s22k.chess.ChessConstants;
import nl.s22k.chess.Statistics;
import nl.s22k.chess.eval.EvalUtil;
import nl.s22k.chess.eval.KingSafetyEval;
import nl.s22k.chess.eval.PassedPawnEval;
import nl.s22k.chess.move.MoveWrapper;
import nl.s22k.chess.move.PVUtil;
import nl.s22k.chess.search.TTUtil;
import nl.s22k.chess.search.ThreadData;
import nl.s22k.chess.search.TimeUtil;

public class UciOut {

	public static boolean noOutput = false;

	public static void sendUci() {
		System.out.println("id name chess22k " + getVersion());
		System.out.println("id author Sander MvdB");
		System.out.println("option name Hash type spin default 128 min 1 max 16384");
		System.out.println("option name Threads type spin default 1 min 1 max " + EngineConstants.MAX_THREADS);
		System.out.println("option name Ponder type check default false");
		System.out.println("uciok");
	}

	public static void sendBestMove(final ThreadData threadData) {
		if (noOutput) {
			return;
		}

		Statistics.print();
		if (UciOptions.ponder && threadData.getPonderMove() != 0) {
			System.out.println("bestmove " + new MoveWrapper(threadData.getBestMove()) + " ponder " + new MoveWrapper(threadData.getPonderMove()));
		} else {
			System.out.println("bestmove " + new MoveWrapper(threadData.getBestMove()));
		}
	}

	public static long calculateNps(long totalMoveCount) {
		return totalMoveCount * 1000 / Math.max(TimeUtil.getPassedTimeMs(), 1);
	}

	public static void sendInfo() {
		if (noOutput) {
			return;
		}
		long totalMoveCount = ChessBoardUtil.calculateTotalMoveCount();
		System.out.println("info nodes " + totalMoveCount + " nps " + calculateNps(totalMoveCount) + " hashfull " + TTUtil.getUsagePercentage());
	}

	public static void sendPlyInfo(final ThreadData threadData) {
		if (noOutput) {
			return;
		}

		// restart info thread
		MainEngine.infoThread.interrupt();

		long totalMoveCount = ChessBoardUtil.calculateTotalMoveCount();

		// info depth 1 seldepth 2 score cp 50 pv d2d4 d7d5 e2e3 hashfull 0 nps 1000 nodes 22
		// info depth 4 seldepth 10 score cp 40 upperbound pv d2d4 d7d5 e2e3 hashfull 0 nps 30000 nodes 1422
		System.out.println("info depth " + threadData.depth + " time " + TimeUtil.getPassedTimeMs() + " score cp " + threadData.bestScore + threadData.scoreType
				+ "nps " + calculateNps(totalMoveCount) + " nodes " + totalMoveCount + " hashfull " + TTUtil.getUsagePercentage() + " pv "
				+ PVUtil.asString(threadData.pv));
	}

	public static void eval(final ChessBoard cb, final ThreadData threadData) {
		final int mobilityScore = EvalUtil.calculateMobilityScoresAndSetAttacks(cb);
		System.out.println(" Material imbalance: " + EvalUtil.getImbalances(cb, threadData.materialCache));
		System.out.println("          Position : " + getMgEgString(cb.psqtScore));
		System.out.println("          Mobility : " + getMgEgString(mobilityScore));
		System.out.println(" Pawn : " + EvalUtil.getPawnScores(cb, threadData.pawnCache));
		System.out.println("       Pawn-passed : " + getMgEgString(PassedPawnEval.calculateScores(cb)));
		System.out.println("       Pawn shield : " + getMgEgString(EvalUtil.calculatePawnShieldBonus(cb)));
		System.out.println("       King-safety : " + KingSafetyEval.calculateScores(cb));
		System.out.println("           Threats : " + getMgEgString(EvalUtil.calculateThreats(cb)));
		System.out.println("             Other : " + EvalUtil.calculateOthers(cb));
		System.out.println("             Space : " + EvalUtil.calculateSpace(cb));
		System.out.println("-----------------------------");
		System.out.println(" Total : " + ChessConstants.COLOR_FACTOR[cb.colorToMove] * EvalUtil.getScore(cb, threadData));
	}

	private static String getMgEgString(int mgEgScore) {
		return EvalUtil.getMgScore(mgEgScore) + "/" + EvalUtil.getEgScore(mgEgScore);
	}

	public static String getVersion() {
		String version = null;
		Package pkg = new MainEngine().getClass().getPackage();
		if (pkg != null) {
			version = pkg.getImplementationVersion();
			if (version == null) {
				version = pkg.getSpecificationVersion();
			}
		}
		version = version == null ? "" : version.trim();
		return version.isEmpty() ? "v?" : version;
	}

}
