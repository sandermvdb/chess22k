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
import nl.s22k.chess.eval.PassedPawnEval;
import nl.s22k.chess.eval.PawnEvalCache;
import nl.s22k.chess.move.MagicUtil;
import nl.s22k.chess.move.MoveWrapper;
import nl.s22k.chess.move.TreeMove;
import nl.s22k.chess.search.HeuristicUtil;
import nl.s22k.chess.search.NegamaxUtil;
import nl.s22k.chess.search.RepetitionTable;
import nl.s22k.chess.search.TTUtil;
import nl.s22k.chess.search.TimeUtil;

public class MainEngine {

	private static ChessBoard cb;
	public static boolean lessOutput = false;
	public static boolean noOutput = false;
	public static String startFen = "";

	private static boolean previousPlyTookLong;
	private static long previousPlyStartTime = System.currentTimeMillis();

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
						if (Statistics.bestMove != null) {
							NegamaxUtil.stop = true;
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

	private static Object synchronizedObject = new Object();

	private static boolean calculating = false;

	public static void main(String[] args) {
		MagicUtil.init();
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
					System.out.println("option name Hash type spin default 128 min 1 max 4096");
					System.out.println("uciok");
				} else if (tokens[0].equals("isready")) {
					TTUtil.init(false);
					System.out.println("readyok");
				} else if (tokens[0].equals("ucinewgame")) {
					HeuristicUtil.clearTables();
					TTUtil.clearValues();
					PawnEvalCache.clearValues();
					EvalCache.clearValues();
				} else if (tokens[0].equals("position")) {
					position(tokens);
				} else if (tokens[0].equals("go")) {
					go(tokens, cb.moveCounter, cb.colorToMove);
				} else if (tokens[0].equals("eval")) {
					eval();
				} else if (tokens[0].equals("setoption")) {
					setOption(tokens[2], tokens[4]);
				} else if (tokens[0].equals("quit")) {
					sc.close();
					System.exit(0);
				} else if (tokens[0].equals("stop")) {
					NegamaxUtil.stop = true;
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
			switch (value) {
			case 1:
			case 2:
			case 4:
			case 8:
			case 16:
			case 32:
			case 64:
			case 128:
			case 256:
			case 512:
			case 1024:
			case 2048:
			case 4096:
				int power2Entries = (int) (Math.log(value) / Math.log(2) + 16);
				if (EngineConstants.POWER_2_TT_ENTRIES != power2Entries) {
					EngineConstants.POWER_2_TT_ENTRIES = power2Entries;
					TTUtil.init(true);
				}
				break;
			default:
				System.out.println("Hash-size must be between 1-4096 and a multiple of 2. Setting default size of 128mb");
				power2Entries = (int) (Math.log(128) / Math.log(2) + 16);
				if (EngineConstants.POWER_2_TT_ENTRIES != power2Entries) {
					EngineConstants.POWER_2_TT_ENTRIES = power2Entries;
					TTUtil.init(true);
				}
			}

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
		NegamaxUtil.maxDepth = EngineConstants.MAX_PLIES;
		TimeUtil.setInfiniteWindow();
		previousPlyTookLong = false;
		previousPlyStartTime = System.currentTimeMillis();

		TTUtil.init(false);
		final long ttValue = TTUtil.getTTValue(cb.zobristKey);
		TimeUtil.setTTHit(ttValue != 0 && TTUtil.getFlag(ttValue) == TTUtil.FLAG_EXACT);
		TreeMove bestMove = Statistics.bestMove;
		if (bestMove != null && bestMove.score < -50) {
			TimeUtil.setLosing(true);
		}

		// go
		// go infinite
		if (goCommandTokens.length != 1) {
			for (int i = 1; i < goCommandTokens.length; i++) {
				if (goCommandTokens[i].equals("infinite")) {
					// TODO are we clearing the values again?
					HeuristicUtil.clearTables();
					TTUtil.clearValues();
					PawnEvalCache.clearValues();
					EvalCache.clearValues();
					TimeUtil.setInfiniteWindow();
				} else if (goCommandTokens[i].equals("movetime")) {
					TimeUtil.setExactMoveTime(Integer.parseInt(goCommandTokens[i + 1]));
				} else if (goCommandTokens[i].equals("movestogo")) {
					TimeUtil.setMovesToGo(Integer.parseInt(goCommandTokens[i + 1]));
				} else if (goCommandTokens[i].equals("depth")) {
					NegamaxUtil.maxDepth = Integer.parseInt(goCommandTokens[i + 1]);
				} else if (goCommandTokens[i].equals("wtime")) {
					if (colorToMove == ChessConstants.WHITE) {
						TimeUtil.setTotalTimeLeft(Integer.parseInt(goCommandTokens[i + 1]));
					}
				} else if (goCommandTokens[i].equals("btime")) {
					if (colorToMove == ChessConstants.BLACK) {
						TimeUtil.setTotalTimeLeft(Integer.parseInt(goCommandTokens[i + 1]));
					}
				}
			}
		}

		TimeUtil.start();

		// less output if we use a short timecontrol
		lessOutput = TimeUtil.getMaxTimeMs() < 1000;

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
			RepetitionTable.addValue(cb.zobristKey);
		}
	}

	private static void eval() {
		EvalUtil.calculateMobilityScoresAndSetAttackBoards(cb);
		System.out.println(" Material imbalance: " + EvalUtil.getImbalances(cb));
		System.out.println("          Position : " + cb.psqtScore + "/" + cb.psqtScoreEg);
		System.out.println("          Mobility : " + cb.mobilityScore[EvalUtil.MG] + "/" + cb.mobilityScore[EvalUtil.EG]);
		System.out.println("              Pawn : " + EvalUtil.getPawnScores(cb));
		System.out.println("    Pawn-passed-eg : " + PassedPawnEval.calculatePassedPawnScores(cb));
		System.out.println("       King-safety : " + KingSafetyEval.calculateKingSafetyScores(cb));
		System.out.println("       Pawn shield : " + EvalUtil.calculatePawnShieldBonus(cb));
		System.out.println("           Threats : " + EvalUtil.calculateThreats(cb));
		System.out.println("             Other : " + EvalUtil.calculateOthers(cb));
		System.out.println("             Space : " + EvalUtil.calculateSpace(cb));
		System.out.println("-----------------------------");
		System.out.println("             Total : " + EvalUtil.getScore(cb));
	}

	private static void sendBestMove() {
		if (noOutput) {
			return;
		}
		if (!lessOutput) {
			Statistics.print();
		}
		TreeMove bestMove = Statistics.bestMove;
		System.out.println("bestmove " + new MoveWrapper(bestMove.move));
	}

	public static void sendInfo() {
		if (lessOutput || noOutput) {
			return;
		}
		System.out.println("info nodes " + Statistics.moveCount + " nps " + Statistics.calculateNps() + " hashfull "
				+ TTUtil.usageCounter * 1000 / (TTUtil.maxEntries * 2));
	}

	public static void sendMoveInfo(final int move, final int movesPerformed) {
		if (lessOutput || noOutput) {
			return;
		}
		if (previousPlyTookLong) {
			System.out.println("info currmove " + new MoveWrapper(move) + " currmovenumber " + movesPerformed);
		}
	}

	public static void sendPlyInfo() {
		if (noOutput) {
			return;
		}
		previousPlyTookLong = System.currentTimeMillis() - previousPlyStartTime > 750;

		// restart info thread
		infoThread.interrupt();

		// info depth 1 seldepth 2 score cp 50 pv d2d4 d7d5 e2e3 hashfull 0 nps 1000 nodes 22
		// info depth 4 seldepth 10 score cp 40 upperbound pv d2d4 d7d5 e2e3 hashfull 0 nps 30000 nodes 1422
		TreeMove bestMove = Statistics.bestMove;
		System.out.println("info depth " + Statistics.depth + " seldepth " + Statistics.maxDepth + " time " + Statistics.getPassedTimeMs() + " score cp "
				+ bestMove.score + bestMove.scoreType + "nps " + Statistics.calculateNps() + " nodes " + Statistics.moveCount + " hashfull "
				+ TTUtil.usageCounter * 1000 / (TTUtil.maxEntries * 2) + " pv " + bestMove);
		previousPlyStartTime = System.currentTimeMillis();
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
