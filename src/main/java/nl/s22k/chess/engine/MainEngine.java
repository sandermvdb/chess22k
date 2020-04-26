package nl.s22k.chess.engine;

import java.util.Arrays;
import java.util.Scanner;

import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.ChessBoardInstances;
import nl.s22k.chess.ChessBoardUtil;
import nl.s22k.chess.ChessConstants;
import nl.s22k.chess.Statistics;
import nl.s22k.chess.move.MoveWrapper;
import nl.s22k.chess.search.NegamaxUtil;
import nl.s22k.chess.search.SearchUtil;
import nl.s22k.chess.search.TTUtil;
import nl.s22k.chess.search.ThreadData;
import nl.s22k.chess.search.TimeUtil;

public class MainEngine {

	private static ChessBoard cb;
	private static ThreadData threadData;

	public static boolean pondering = false;
	private static boolean maxTimeExceeded = false;
	private static boolean calculating = false;

	public static int maxDepth = EngineConstants.MAX_PLIES;

	private static Object synchronizedObject = new Object();

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
						SearchUtil.start(cb);

						// calculation ready
						calculating = false;
						maxTimeThread.interrupt();
						infoThread.interrupt();

						UciOut.sendBestMove(threadData);
					} catch (Throwable t) {
						ErrorLogger.log(cb, t, true);
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
						} else if (threadData.getBestMove() != 0) {
							System.out.println("info string max time exceeded");
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

	public static Thread infoThread;
	static {
		infoThread = new Thread() {
			@Override
			public void run() {
				while (true) {
					try {
						Thread.sleep(2000);
						if (calculating) {
							UciOut.sendInfo();
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
		Thread.currentThread().setName("chess22k-main");
		cb = ChessBoardInstances.get(0);
		threadData = ThreadData.getInstance(0);
		searchThread.start();
		maxTimeThread.start();
		infoThread.start();
		MainEngine.start();
	}

	private static void start() {
		Scanner sc = new Scanner(System.in);
		try {
			while (sc.hasNextLine()) {
				readLine(sc.nextLine());
			}
		} catch (Throwable t) {
			ErrorLogger.log(cb, t, true);
		} finally {
			sc.close();
		}
	}

	public synchronized static void readLine(String line) {
		String[] tokens = line.split(" ");
		if (tokens[0].equals("uci")) {
			UciOut.sendUci();
		} else if (tokens[0].equals("isready")) {
			System.out.println("readyok");
		} else if (tokens[0].equals("ucinewgame")) {
			TTUtil.init(false);
			TTUtil.clearValues();
		} else if (tokens[0].equals("position")) {
			position(tokens);
		} else if (tokens[0].equals("go")) {
			go(tokens);
		} else if (tokens[0].equals("ponderhit")) {
			pondering = false;
			if (MainEngine.maxTimeExceeded) {
				NegamaxUtil.isRunning = false;
			}
		} else if (tokens[0].equals("eval")) {
			UciOut.eval(cb, threadData);
		} else if (tokens[0].equals("setoption")) {
			if (tokens.length > 4) {
				setOption(tokens[2], tokens[4]);
			}
		} else if (tokens[0].equals("quit")) {
			System.exit(0);
		} else if (tokens[0].equals("stop")) {
			NegamaxUtil.isRunning = false;
		} else {
			System.out.println("Unknown command: " + tokens[0]);
		}
	}

	private static void position(String[] tokens) {
		if (tokens[1].equals("startpos")) {
			ChessBoardUtil.setStartFen(cb);
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
			ChessBoardUtil.setFen(fen, cb);

			if (tokens.length == 6 || tokens.length == 7 || tokens.length == 8) {
				// position fen 4k3/8/8/8/8/3K4 b kq - 0 1
				doMoves(new String[] {});
			} else {
				// position fen 4k3/8/8/8/8/3K4 b kq - 0 1 moves f2f3 g1a3 ...
				doMoves(Arrays.copyOfRange(tokens, 9, tokens.length));
			}
		}
		ErrorLogger.startFen = cb.toString();
		TTUtil.halfMoveCounter = cb.moveCounter;
	}

	private static void setOption(String optionName, String optionValue) {
		// setoption name Hash value 128
		if (optionName.toLowerCase().equals("hash")) {
			int value = Integer.parseInt(optionValue);
			TTUtil.setSizeMB(value);
		} else if (optionName.toLowerCase().equals("threads")) {
			UciOptions.setThreadCount(Integer.parseInt(optionValue));
			cb = ChessBoardInstances.get(0);
			threadData = ThreadData.getInstance(0);
		} else if (optionName.toLowerCase().equals("ponder")) {
			UciOptions.setPonder(Boolean.parseBoolean(optionValue));
		} else {
			System.out.println("Unknown option: " + optionName);
		}
	}

	private static void go(String[] goCommandTokens) {
		// go movestogo 30 wtime 3600000 btime 3600000
		// go wtime 40847 btime 48019 winc 0 binc 0 movestogo 20

		Statistics.reset();
		TimeUtil.reset();
		TimeUtil.setMoveCount(cb.moveCounter);
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
				} else if (goCommandTokens[i].equals("ponder")) {
					pondering = true;
				} else if (goCommandTokens[i].equals("movetime")) {
					TimeUtil.setExactMoveTime(Integer.parseInt(goCommandTokens[i + 1]));
				} else if (goCommandTokens[i].equals("movestogo")) {
					TimeUtil.setMovesToGo(Integer.parseInt(goCommandTokens[i + 1]));
				} else if (goCommandTokens[i].equals("depth")) {
					maxDepth = Integer.parseInt(goCommandTokens[i + 1]);
				} else if (goCommandTokens[i].equals("wtime")) {
					if (cb.colorToMove == ChessConstants.WHITE) {
						TimeUtil.setTotalTimeLeft(Integer.parseInt(goCommandTokens[i + 1]));
					}
				} else if (goCommandTokens[i].equals("btime")) {
					if (cb.colorToMove == ChessConstants.BLACK) {
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

}
