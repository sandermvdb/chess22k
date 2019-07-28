package nl.s22k.chess.engine;

import java.util.Arrays;
import java.util.Scanner;

import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.ChessBoardUtil;
import nl.s22k.chess.ChessConstants;
import nl.s22k.chess.Statistics;
import nl.s22k.chess.eval.EvalCache;
import nl.s22k.chess.eval.EvalUtil;
import nl.s22k.chess.eval.KingSafetyEval;
import nl.s22k.chess.eval.MaterialCache;
import nl.s22k.chess.eval.PassedPawnEval;
import nl.s22k.chess.eval.PawnEvalCache;
import nl.s22k.chess.move.MoveGenerator;
import nl.s22k.chess.move.MoveWrapper;
import nl.s22k.chess.move.PV;
import nl.s22k.chess.search.NegamaxUtil;
import nl.s22k.chess.search.TTUtil;
import nl.s22k.chess.search.TimeUtil;

public class MainEngine {

	private static ChessBoard cb;
	public static boolean noOutput = false;
	public static String startFen = "";

	private static boolean ponder = true;
	private static boolean maxTimeExceeded = false;

	public static int maxDepth = EngineConstants.MAX_PLIES;
	public static int nrOfThreads = 1;
	public static boolean pondering = false;

	private static Object synchronizedObject = new Object();
	private static boolean calculating = false;

	private static Thread searchThread;
	static {
		searchThread = new Thread() {
			@Override
			public void run() {
				while (true) {
					try {
						synchronized (synchronizedObject) {
							// to prevent spurious wakeups
							while (!calculating) {
								synchronizedObject.wait();
							}
						}

						maxTimeExceeded = false;
						NegamaxUtil.start(cb);

						// calculation ready
						calculating = false;
						maxTimeThread.interrupt();
						infoThread.interrupt();

						sendBestMove();
					} catch (Throwable t) {
						ErrorLogger.log(cb, t);
					}
				}
			}
		};
		searchThread.setName("chess22k-search");
		searchThread.setDaemon(true);
	}

	private static Thread maxTimeThread;
	static {
		maxTimeThread = new Thread() {
			@Override
			public void run() {
				while (true) {
					try {
						synchronized (synchronizedObject) {
							// to prevent spurious wakeups
							while (!calculating) {
								synchronizedObject.wait();
							}
						}
						Thread.sleep(TimeUtil.getMaxTimeMs());
						if (pondering) {
							maxTimeExceeded = true;
						} else if (PV.getBestMove() != 0) {
							NegamaxUtil.isRunning = false;
						}

					} catch (InterruptedException e) {
						// do nothing
					}
				}
			}
		};
		maxTimeThread.setName("chess22k-max-timer");
		maxTimeThread.setDaemon(true);
	}

	private static Thread infoThread;
	static {
		infoThread = new Thread() {
			@Override
			public void run() {
				while (true) {
					try {
						Thread.sleep(2000);
						if (calculating) {
							sendInfo();
						}
					} catch (InterruptedException e) {
						// do nothing
					}
				}
			}
		};
		infoThread.setName("chess22k-info");
		infoThread.setDaemon(true);
		infoThread.setPriority(Thread.MIN_PRIORITY);
	}

	public static void main(String[] args) {
		cb = ChessBoardUtil.getNewCB();
		searchThread.start();
		maxTimeThread.start();
		infoThread.start();
		MainEngine.start();
	}

	private static void start() {
		try {
			Scanner sc = new Scanner(System.in);

			while (sc.hasNextLine()) {
				String[] tokens = sc.nextLine().split(" ");
				if (tokens[0].equals("uci")) {
					System.out.println("id name chess22k " + getVersion());
					System.out.println("id author Sander MvdB");
					System.out.println("option name Hash type spin default 128 min 1 max 16384");
					System.out.println("option name Threads type spin default 1 min 1 max " + EngineConstants.MAX_THREADS);
					if (EngineConstants.ENABLE_PONDERING) {
						System.out.println("option name Ponder type check default false");
					}
					System.out.println("uciok");
				} else if (tokens[0].equals("isready")) {
					TTUtil.init(false);
					System.out.println("readyok");
				} else if (tokens[0].equals("ucinewgame")) {
					TTUtil.clearValues();
					PawnEvalCache.clearValues();
					MaterialCache.clearValues();
					EvalCache.clearValues();
				} else if (tokens[0].equals("position")) {
					position(tokens);
				} else if (tokens[0].equals("go")) {
					go(tokens, cb.moveCounter, cb.colorToMove);
				} else if (tokens[0].equals("ponderhit")) {
					pondering = false;
					if (maxTimeExceeded) {
						NegamaxUtil.isRunning = false;
					}
				} else if (tokens[0].equals("eval")) {
					eval();
				} else if (tokens[0].equals("setoption")) {
					setOption(tokens[2], tokens[4]);
				} else if (tokens[0].equals("quit")) {
					sc.close();
					System.exit(0);
				} else if (tokens[0].equals("stop")) {
					NegamaxUtil.isRunning = false;
				} else {
					System.out.println("Unknown command: " + tokens[0]);
				}
			}
		} catch (Throwable t) {
			ErrorLogger.log(cb, t);
		}

	}

	private static void position(String[] tokens) {
		if (tokens[1].equals("startpos")) {
			cb = ChessBoardUtil.getNewCB();
			if (tokens.length == 2) {
				// position startpos
				doMoves(new String[] {});
			} else {
				// position startpos moves f2f3 g1a3 ...
				doMoves(Arrays.copyOfRange(tokens, 3, tokens.length));
			}
		} else {
			// position fen 4k3/8/8/8/8/3K4 b kq - 0 1 moves f2f3 g1a3 ...
			String fen = tokens[2] + " " + tokens[3] + " " + tokens[4] + " " + tokens[5];
			if (tokens.length > 6) {
				fen += " " + tokens[6];
				fen += " " + tokens[7];
			}
			cb = ChessBoardUtil.getNewCB(fen);

			if (tokens.length == 6 || tokens.length == 7 || tokens.length == 8) {
				// position fen 4k3/8/8/8/8/3K4 b kq - 0 1
				doMoves(new String[] {});
			} else {
				// position fen 4k3/8/8/8/8/3K4 b kq - 0 1 moves f2f3 g1a3 ...
				doMoves(Arrays.copyOfRange(tokens, 9, tokens.length));
			}
		}
		TTUtil.halfMoveCounter = cb.moveCounter;
		startFen = cb.toString();
	}

	private static void setOption(String optionName, String optionValue) {
		// setoption name Hash value 128
		if (optionName.toLowerCase().equals("hash")) {
			int value = Integer.parseInt(optionValue);
			TTUtil.setSizeMB(value);
		} else if (optionName.toLowerCase().equals("threads")) {
			nrOfThreads = Integer.parseInt(optionValue);
			ChessBoard.initInstances(nrOfThreads);
			MoveGenerator.initInstances(nrOfThreads);
		} else if (optionName.toLowerCase().equals("ponder")) {
			ponder = Boolean.parseBoolean(optionValue);
		} else {
			System.out.println("Unknown option: " + optionName);
		}
	}

	private static void go(String[] goCommandTokens, int moveCount, int colorToMove) {
		// go movestogo 30 wtime 3600000 btime 3600000
		// go wtime 40847 btime 48019 winc 0 binc 0 movestogo 20

		Statistics.reset();
		TimeUtil.reset();
		TimeUtil.setMoveCount(moveCount);
		maxDepth = EngineConstants.MAX_PLIES;
		pondering = false;

		TTUtil.init(false);
		final long ttValue = TTUtil.getValue(cb.zobristKey);
		if (ttValue != 0 && TTUtil.getFlag(ttValue) == TTUtil.FLAG_EXACT) {
			TimeUtil.setTTHit();
		}

		// go
		// go infinite
		// go ponder
		if (goCommandTokens.length != 1) {
			for (int i = 1; i < goCommandTokens.length; i++) {
				if (goCommandTokens[i].equals("infinite")) {
					// TODO are we clearing the values again?
					TTUtil.clearValues();
					PawnEvalCache.clearValues();
					EvalCache.clearValues();
					MaterialCache.clearValues();
				} else if (goCommandTokens[i].equals("ponder")) {
					pondering = true;
				} else if (goCommandTokens[i].equals("movetime")) {
					TimeUtil.setExactMoveTime(Integer.parseInt(goCommandTokens[i + 1]));
				} else if (goCommandTokens[i].equals("movestogo")) {
					TimeUtil.setMovesToGo(Integer.parseInt(goCommandTokens[i + 1]));
				} else if (goCommandTokens[i].equals("depth")) {
					maxDepth = Integer.parseInt(goCommandTokens[i + 1]);
				} else if (goCommandTokens[i].equals("wtime")) {
					if (colorToMove == ChessConstants.WHITE) {
						TimeUtil.setTotalTimeLeft(Integer.parseInt(goCommandTokens[i + 1]));
					}
				} else if (goCommandTokens[i].equals("btime")) {
					if (colorToMove == ChessConstants.BLACK) {
						TimeUtil.setTotalTimeLeft(Integer.parseInt(goCommandTokens[i + 1]));
					}
				} else if (goCommandTokens[i].equals("winc") || goCommandTokens[i].equals("binc")) {
					TimeUtil.setIncrement(Integer.parseInt(goCommandTokens[i + 1]));
				}
			}
		}

		TimeUtil.start();

		calculating = true;
		synchronized (synchronizedObject) {
			synchronizedObject.notifyAll();
		}
	}

	private static void doMoves(String[] moveTokens) {
		// apply moves
		for (String moveToken : moveTokens) {
			MoveWrapper move = new MoveWrapper(moveToken, cb);
			cb.doMove(move.move);
		}
	}

	private static void eval() {
		final int mobilityScore = EvalUtil.calculateMobilityScoresAndSetAttacks(cb);
		System.out.println(" Material imbalance: " + EvalUtil.getImbalances(cb));
		System.out.println("          Position : " + getMgEgString(cb.psqtScore));
		System.out.println("          Mobility : " + getMgEgString(mobilityScore));
		System.out.println("              Pawn : " + EvalUtil.getPawnScores(cb));
		System.out.println("       Pawn-passed : " + getMgEgString(PassedPawnEval.calculateScores(cb)));
		System.out.println("       Pawn shield : " + getMgEgString(EvalUtil.calculatePawnShieldBonus(cb)));
		System.out.println("       King-safety : " + KingSafetyEval.calculateScores(cb));
		System.out.println("           Threats : " + getMgEgString(EvalUtil.calculateThreats(cb)));
		System.out.println("             Other : " + EvalUtil.calculateOthers(cb));
		System.out.println("             Space : " + EvalUtil.calculateSpace(cb));
		System.out.println("-----------------------------");
		System.out.println("             Total : " + ChessConstants.COLOR_FACTOR[cb.colorToMove] * EvalUtil.getScore(cb));
	}

	private static String getMgEgString(int mgEgScore) {
		return EvalUtil.getMgScore(mgEgScore) + "/" + EvalUtil.getEgScore(mgEgScore);
	}

	private static void sendBestMove() {
		if (noOutput) {
			return;
		}

		Statistics.print();
		if (ponder && PV.getPonderMove() != 0) {
			System.out.println("bestmove " + new MoveWrapper(PV.getBestMove()) + " ponder " + new MoveWrapper(PV.getPonderMove()));
		} else {
			System.out.println("bestmove " + new MoveWrapper(PV.getBestMove()));
		}
	}

	public static void sendInfo() {
		if (noOutput) {
			return;
		}
		ChessBoard.calculateTotalMoveCount();
		System.out.println("info nodes " + ChessBoard.totalMoveCount + " nps " + calculateNps() + " hashfull " + TTUtil.getUsagePercentage());
	}

	public static void sendPlyInfo() {
		if (noOutput) {
			return;
		}

		// restart info thread
		infoThread.interrupt();

		ChessBoard.calculateTotalMoveCount();

		// info depth 1 seldepth 2 score cp 50 pv d2d4 d7d5 e2e3 hashfull 0 nps 1000 nodes 22
		// info depth 4 seldepth 10 score cp 40 upperbound pv d2d4 d7d5 e2e3 hashfull 0 nps 30000 nodes 1422
		System.out.println("info depth " + Statistics.depth + " time " + TimeUtil.getPassedTimeMs() + " score cp " + PV.getScore() + PV.getScoreType() + "nps "
				+ calculateNps() + " nodes " + ChessBoard.totalMoveCount + " hashfull " + TTUtil.getUsagePercentage() + " pv " + PV.asString());
	}

	public static long calculateNps() {
		return ChessBoard.totalMoveCount * 1000 / Math.max(TimeUtil.getPassedTimeMs(), 1);
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
