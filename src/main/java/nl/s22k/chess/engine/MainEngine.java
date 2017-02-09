package nl.s22k.chess.engine;

import java.util.Arrays;
import java.util.Scanner;

import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.ChessBoardUtil;
import nl.s22k.chess.ChessConstants;
import nl.s22k.chess.Statistics;
import nl.s22k.chess.eval.EvalCache;
import nl.s22k.chess.eval.EvalUtil;
import nl.s22k.chess.eval.PawnEvalCache;
import nl.s22k.chess.move.MagicUtil;
import nl.s22k.chess.move.MoveWrapper;
import nl.s22k.chess.move.TreeMove;
import nl.s22k.chess.search.HeuristicUtil;
import nl.s22k.chess.search.NegamaxUtil;
import nl.s22k.chess.search.RepetitionTable;
import nl.s22k.chess.search.TTUtil;

public class MainEngine {

	private ChessBoard chessBoard;
	public boolean quiet = false;

	public static void main(String[] args) {

		MagicUtil.init();

		MainEngine engine = new MainEngine();
		NegamaxUtil.chessEngine = engine;
		engine.start();

	}

	public void start() {
		try {
			Scanner sc = new Scanner(System.in);

			while (sc.hasNextLine()) {
				String[] tokens = sc.nextLine().split(" ");
				if (tokens[0].equals("uci")) {
					System.out.println("id name chess22k " + getVersion());
					System.out.println("id author Sander MvdB");
					System.out.println("uciok");
				} else if (tokens[0].equals("isready")) {
					System.out.println("readyok");
				} else if (tokens[0].equals("ucinewgame")) {
					HeuristicUtil.clearTables();
					TTUtil.clearValues();
					PawnEvalCache.clearValues();
					EvalCache.clearValues();
				} else if (tokens[0].equals("position")) {
					if (tokens[1].equals("startpos")) {
						chessBoard = ChessBoardUtil.getNewCB();
						if (tokens.length == 2) {
							// position startpos
							position(new String[] {});
						} else {
							// position startpos moves f2f3 g1a3 ...
							position(Arrays.copyOfRange(tokens, 3, tokens.length));
						}
					} else {
						// position fen 4k3/8/8/8/8/3K4 b kq - 0 1 moves f2f3 g1a3 ...
						String fen = tokens[2] + " " + tokens[3] + " " + tokens[4] + " " + tokens[5] + " " + tokens[6] + " " + tokens[7];
						chessBoard = ChessBoardUtil.getNewCB(fen);
						if (tokens.length == 8) {
							// position fen 4k3/8/8/8/8/3K4 b kq - 0 1
							position(new String[] {});
						} else {
							// position fen 4k3/8/8/8/8/3K4 b kq - 0 1 moves f2f3 g1a3 ...
							position(Arrays.copyOfRange(tokens, 9, tokens.length));
						}
					}
				} else if (tokens[0].equals("go")) {
					if (tokens.length == 1 || tokens[1].equals("infinite")) {
						// go infinite
						go(Long.MAX_VALUE);
					} else if (tokens[1].equals("depth")) {
						// go infinite
						go(Integer.parseInt(tokens[2]));
					} else {
						// go wtime 120000 btime 120000 winc 0 binc 0
						if (chessBoard.colorToMove == ChessConstants.WHITE) {
							go(Long.parseLong(tokens[2]));
						} else {
							go(Long.parseLong(tokens[4]));
						}
					}
				} else if (tokens[0].equals("eval")) {
					eval();
				} else if (tokens[0].equals("stop")) {
					NegamaxUtil.stop = true;
				} else if (tokens[0].equals("quit")) {
					sc.close();
					System.exit(0);
				} else {
					System.out.println("Unknown command: " + tokens[0]);
				}
			}
		} catch (Throwable t) {
			ErrorLogger.log(chessBoard, t);
		}

	}

	public void position(String[] moveTokens) {
		// apply moves
		for (String moveToken : moveTokens) {
			MoveWrapper move = new MoveWrapper(moveToken, chessBoard);
			chessBoard.doMove(move.move);
			RepetitionTable.addValue(chessBoard.zobristKey);
		}
	}

	public void go(long msecLeft) {
		Thread t = new Thread() {
			@Override
			public void run() {
				Statistics.reset();
				try {
					NegamaxUtil.start(chessBoard, msecLeft);
				} catch (Throwable t) {
					ErrorLogger.log(chessBoard, t);
					System.exit(1);
				}
			}

		};
		t.start();
	}

	public void go(int depth) {
		Thread t = new Thread() {
			@Override
			public void run() {
				Statistics.reset();
				try {
					NegamaxUtil.start(chessBoard, depth);
				} catch (Throwable t) {
					ErrorLogger.log(chessBoard, t);
					System.exit(1);
				}
			}
		};
		t.start();
	}

	public void eval() {
		System.out.println("Material (no pawn)	: " + EvalUtil.calculateMaterialScores(chessBoard));
		System.out.println("Position   			: " + EvalUtil.calculatePositionScores(chessBoard));
		System.out.println("Mobility    		: " + EvalUtil.calculateMobilityWithKingDefenseScores(chessBoard));
		System.out.println("King-safety 		: " + EvalUtil.calculateKingSafetyScores(chessBoard));
		System.out.println("Pawn        		: " + EvalUtil.calculatePawnScores(chessBoard));
		System.out.println("Bonus       		: " + EvalUtil.calculateBonusses(chessBoard));
		System.out.println("Penalties   		: " + EvalUtil.calculatePenalties(chessBoard));
		System.out.println("Total       		: " + EvalUtil.calculateScore(chessBoard));
	}

	public void sendBestMove() {
		if (!quiet) {
			Statistics.print();
			TreeMove bestMove = Statistics.bestMove;
			System.out.println("bestmove " + new MoveWrapper(bestMove.move));
		}
	}

	public void sendPlyInfo() {
		if (quiet) {
			return;
		}
			TreeMove bestMove = Statistics.bestMove;

			// TODO hash-usage
			// info depth 1 seldepth 2 score cp 50 pv d2d4 d7d5 e2e3 hashfull 0 nps 1000 nodes 22
			// info depth 4 seldepth 10 score cp 40 upperbound pv d2d4 d7d5 e2e3 hashfull 0 nps 30000 nodes 1422
			System.out.println("info depth " + Statistics.depth + " seldepth " + Statistics.maxDepth + " score cp " + bestMove.score + bestMove.scoreType
					+ "pv " + bestMove + " hashfull " + 10 + " nps " + Statistics.calculateNps() + " nodes " + Statistics.moveCount);
	}

	private String getVersion() {
		String version = null;
		Package pkg = getClass().getPackage();
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
