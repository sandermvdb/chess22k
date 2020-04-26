package nl.s22k.chess.texel;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.ChessBoardInstances;
import nl.s22k.chess.ChessBoardUtil;
import nl.s22k.chess.eval.EvalConstants;
import nl.s22k.chess.move.MoveGenerator;
import nl.s22k.chess.search.ThreadData;
import nl.s22k.chess.search.TimeUtil;

public class Tuner {

	private static ChessBoard cb = ChessBoardInstances.get(0);
	private static ThreadData threadData = new ThreadData(0);

	private static int numberOfThreads = 16;
	private static ErrorCalculator[] workers = new ErrorCalculator[numberOfThreads];
	private static ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

	private static double orgError;
	private static double bestError;

	private static final int STEP = 4;

	public static List<Tuning> getTuningObjects() {
		List<Tuning> tunings = new ArrayList<Tuning>();

		// tunings.add(new Tuning(EvalConstants.PHASE, 1, "Phase", 0, 1));

		tunings.add(new Tuning(EvalConstants.OTHER_SCORES, STEP, "Other scores"));
		tunings.add(new Tuning(EvalConstants.THREATS_MG, STEP, "Threats mg"));
		tunings.add(new Tuning(EvalConstants.THREATS_EG, STEP, "Threats eg"));
		tunings.add(new Tuning(EvalConstants.PAWN_SCORES, STEP, "Pawn scores"));
		tunings.add(new Tuning(EvalConstants.IMBALANCE_SCORES, STEP, "Imbalance scores"));

		// tunings.add(new Tuning(EvalConstants.MATERIAL, STEP, "Material", 0, 1, 6));
		// tunings.add(new Tuning(EvalConstants.PINNED, STEP, "Pinned", 0));
		// tunings.add(new Tuning(EvalConstants.DISCOVERED, STEP, "Discovered", 0));
		// tunings.add(new Tuning(EvalConstants.DOUBLE_ATTACKED, STEP, "Double attacked"));
		// tunings.add(new Tuning(EvalConstants.NIGHT_PAWN, STEP, "Night pawn"));
		// tunings.add(new Tuning(EvalConstants.ROOK_PAWN, STEP, "Rook pawn"));
		// tunings.add(new Tuning(EvalConstants.BISHOP_PAWN, STEP, "Bishop pawn"));
		// tunings.add(new Tuning(EvalConstants.SPACE, STEP, "Space"));
		//
		// /* pawns */
		// tunings.add(new Tuning(EvalConstants.PASSED_SCORE_EG, STEP, "Passed score eg", 0));
		// tunings.add(new MultiTuning(EvalConstants.PASSED_MULTIPLIERS, "Passed multi"));
		// tunings.add(new MultiTuning(EvalConstants.PASSED_KING_MULTI, "Passed king multi"));
		// tunings.add(new Tuning(EvalConstants.PASSED_CANDIDATE, STEP, "Passed candidate", 0));
		// tunings.add(new TableTuning(EvalConstants.SHIELD_BONUS_MG, STEP, "Shield mg"));
		// tunings.add(new TableTuning(EvalConstants.SHIELD_BONUS_EG, STEP, "Shield eg"));
		// tunings.add(new Tuning(EvalConstants.PAWN_BLOCKAGE, STEP, "Pawn blockage", 0, 1));
		// tunings.add(new Tuning(EvalConstants.PAWN_CONNECTED, STEP, "Pawn connected", 0, 1));
		// tunings.add(new Tuning(EvalConstants.PAWN_NEIGHBOUR, STEP, "Pawn neighbour", 0, 1));
		//
		// /* king-safety */
		// tunings.add(new Tuning(EvalConstants.KS_SCORES, 10, "KS"));
		// tunings.add(new Tuning(EvalConstants.KS_QUEEN_TROPISM, 1, "KS queen", 0, 1));
		// tunings.add(new Tuning(EvalConstants.KS_CHECK_QUEEN, 1, "KS check q", 0, 1, 2, 3));
		// tunings.add(new Tuning(EvalConstants.KS_FRIENDS, 1, "KS friends"));
		// tunings.add(new Tuning(EvalConstants.KS_WEAK, 1, "KS weak"));
		// tunings.add(new Tuning(EvalConstants.KS_ATTACKS, 1, "KS attacks"));
		// tunings.add(new Tuning(EvalConstants.KS_NIGHT_DEFENDERS, 1, "KS night defenders"));
		// tunings.add(new Tuning(EvalConstants.KS_DOUBLE_ATTACKS, 1, "KS double attacks"));
		// tunings.add(new Tuning(EvalConstants.KS_ATTACK_PATTERN, 1, "KS pattern"));
		// tunings.add(new Tuning(EvalConstants.KS_OTHER, 1, "KS other"));
		//
		// /* mobility */
		// tunings.add(new Tuning(EvalConstants.MOBILITY_KNIGHT_MG, STEP, "Mobility n mg", true));
		// tunings.add(new Tuning(EvalConstants.MOBILITY_KNIGHT_EG, STEP, "Mobility n eg", true));
		// tunings.add(new Tuning(EvalConstants.MOBILITY_BISHOP_MG, STEP, "Mobility b mg", true));
		// tunings.add(new Tuning(EvalConstants.MOBILITY_BISHOP_EG, STEP, "Mobility b eg", true));
		// tunings.add(new Tuning(EvalConstants.MOBILITY_ROOK_MG, STEP, "Mobility r mg", true));
		// tunings.add(new Tuning(EvalConstants.MOBILITY_ROOK_EG, STEP, "Mobility r eg", true));
		// tunings.add(new Tuning(EvalConstants.MOBILITY_QUEEN_MG, STEP, "Mobility q mg", true));
		// tunings.add(new Tuning(EvalConstants.MOBILITY_QUEEN_EG, STEP, "Mobility q eg", true));
		// tunings.add(new Tuning(EvalConstants.MOBILITY_KING_MG, STEP, "Mobility k mg", true));
		// tunings.add(new Tuning(EvalConstants.MOBILITY_KING_EG, STEP, "Mobility k eg", true));
		//
		// /* psqt */
		// tunings.add(new PsqtTuning(EvalConstants.PSQT_MG[ChessConstants.PAWN], STEP, "PSQT p mg", true));
		// tunings.add(new PsqtTuning(EvalConstants.PSQT_EG[ChessConstants.PAWN], STEP, "PSQT p eg", true));
		// tunings.add(new PsqtTuning(EvalConstants.PSQT_MG[ChessConstants.NIGHT], STEP, "PSQT n mg"));
		// tunings.add(new PsqtTuning(EvalConstants.PSQT_EG[ChessConstants.NIGHT], STEP, "PSQT n eg"));
		// tunings.add(new PsqtTuning(EvalConstants.PSQT_MG[ChessConstants.BISHOP], STEP, "PSQT b mg"));
		// tunings.add(new PsqtTuning(EvalConstants.PSQT_EG[ChessConstants.BISHOP], STEP, "PSQT b eg"));
		// tunings.add(new PsqtTuning(EvalConstants.PSQT_MG[ChessConstants.ROOK], STEP, "PSQT r mg"));
		// tunings.add(new PsqtTuning(EvalConstants.PSQT_EG[ChessConstants.ROOK], STEP, "PSQT r eg"));
		// tunings.add(new PsqtTuning(EvalConstants.PSQT_MG[ChessConstants.QUEEN], STEP, "PSQT q mg"));
		// tunings.add(new PsqtTuning(EvalConstants.PSQT_EG[ChessConstants.QUEEN], STEP, "PSQT q eg"));
		// tunings.add(new PsqtTuning(EvalConstants.PSQT_MG[ChessConstants.KING], STEP, "PSQT k mg"));
		// tunings.add(new PsqtTuning(EvalConstants.PSQT_EG[ChessConstants.KING], STEP, "PSQT k eg"));

		return tunings;
	}

	public static void main(String[] args) {
		// read all fens, including score
		Map<String, Double> fens = loadFens("d:\\backup\\chess\\epds\\quiet-labeled.epd", true, false);
		System.out.println("Fens found : " + fens.size());

		// init workers
		ChessBoardInstances.init(numberOfThreads);
		ThreadData.initInstances(numberOfThreads);
		for (int i = 0; i < numberOfThreads; i++) {
			workers[i] = new ErrorCalculator(ChessBoardInstances.get(i), ThreadData.getInstance(i));
		}

		// add fens to workers
		int workerIndex = 0;
		Iterator<Entry<String, Double>> iterator = fens.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, Double> entry = iterator.next();
			workers[workerIndex].addFenWithScore(entry.getKey(), entry.getValue());
			workerIndex = workerIndex == numberOfThreads - 1 ? 0 : workerIndex + 1;
		}

		// get tuned values
		List<Tuning> tuningObjects = getTuningObjects();

		// tune
		printInfo(tuningObjects);
		localOptimize(tuningObjects);
		executor.shutdown();
		System.out.println(String.format("\nDone: %s -> %s\n", orgError, bestError));
	}

	public static void printAll(List<Tuning> tuningObjects) {
		for (Tuning tuningObject : tuningObjects) {
			if (tuningObject.isUpdated()) {
				tuningObject.printNewValues();
			} else {
				System.out.println(tuningObject.name + ": unchanged");
			}
		}
	}

	public static Map<String, Double> loadFens(String fileName, boolean containsResult, boolean includingCheck) {
		System.out.println("Loading " + fileName);

		Map<String, Double> fens = new HashMap<String, Double>();
		int checkCount = 0;
		int checkmate = 0;
		int stalemate = 0;

		try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
			String line = br.readLine();

			while (line != null) {

				double score = 0;
				String fenString;
				if (containsResult) {
					String scoreString = getScoreStringFromLine(line);
					fenString = getFenStringFromLine(line);
					if (scoreString.equals("\"1/2-1/2\";")) {
						score = 0.5;
					} else if (scoreString.equals("\"1-0\";")) {
						score = 1;
					} else if (scoreString.equals("\"0-1\";")) {
						score = 0;
					} else {
						throw new RuntimeException("Unknown result: " + scoreString);
					}
				} else {
					fenString = line;
				}

				ChessBoardUtil.setFen(fenString, cb);

				if (cb.checkingPieces == 0) {
					threadData.startPly();
					MoveGenerator.generateAttacks(threadData, cb);
					MoveGenerator.generateMoves(threadData, cb);
					if (threadData.hasNext()) {
						fens.put(fenString, score);
					} else {
						stalemate++;
					}
					threadData.endPly();
				} else {
					checkCount++;
					if (includingCheck) {
						threadData.startPly();
						MoveGenerator.generateAttacks(threadData, cb);
						MoveGenerator.generateMoves(threadData, cb);
						if (threadData.hasNext()) {
							fens.put(fenString, score);
						} else {
							checkmate++;
						}
						threadData.endPly();
					}
				}

				line = br.readLine();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		System.out.println("In check : " + checkCount);
		System.out.println("Checkmate : " + checkmate);
		System.out.println("Stalemate : " + stalemate);
		return fens;
	}

	private static String getFenStringFromLine(String line) {
		if (line.contains("c9")) {
			return line.split(" c9 ")[0];
		} else {
			return line.substring(0, line.indexOf("\""));
		}
	}

	private static String getScoreStringFromLine(String line) {
		if (line.contains("c9")) {
			return line.split(" c9 ")[1];
		} else {
			return line.substring(line.indexOf("\""));
		}
	}

	private static void printInfo(List<Tuning> tuningObjects) {
		TimeUtil.reset();
		System.out.println("\nNumber of threads: " + numberOfThreads);
		System.out.println("\nValues that are being tuned:");

		int totalValues = 0;
		for (Tuning tuningObject : tuningObjects) {
			if (tuningObject.showAverage) {
				System.out.println(tuningObject.name + " " + tuningObject.getAverage());
			} else {
				System.out.println(tuningObject.name);
			}
			totalValues += tuningObject.getNumberOfTunedValues();
		}
		System.out.println(String.format("\nInitial error: %s (%s ms)", calculateErrorMultiThreaded(), TimeUtil.getPassedTimeMs()));
		System.out.println("Total values to be tuned: " + totalValues + "\n");
	}

	private static void localOptimize(List<Tuning> tuningObjects) {
		double bestError = calculateErrorMultiThreaded();
		orgError = bestError;
		boolean improved = true;
		int run = 1;
		while (improved) {
			System.out.println("Run " + run++);
			improved = false;
			for (Tuning tuningObject : tuningObjects) {
				for (int i = 0; i < tuningObject.numberOfParameters(); i++) {
					if (tuningObject.skip(i)) {
						continue;
					}
					tuningObject.addStep(i);
					EvalConstants.initMgEg();
					threadData.clearCaches();
					double newError = calculateErrorMultiThreaded();
					if (newError < bestError - 0.00000001) {
						bestError = newError;
						System.out.println(String.format("%f - %s", bestError, tuningObject));
						improved = true;
					} else {
						tuningObject.removeStep(i);
						tuningObject.removeStep(i);
						EvalConstants.initMgEg();
						threadData.clearCaches();
						newError = calculateErrorMultiThreaded();
						if (newError < bestError - 0.00000001) {
							bestError = newError;
							System.out.println(String.format("%f - %s", bestError, tuningObject));
							improved = true;
						} else {
							tuningObject.addStep(i);
						}
					}
				}
			}
			printAll(tuningObjects);
		}
		Tuner.bestError = bestError;
	}

	private static double calculateErrorMultiThreaded() {
		List<Future<Double>> list = new ArrayList<Future<Double>>();
		for (int i = 0; i < numberOfThreads; i++) {
			Future<Double> submit = executor.submit(workers[i]);
			list.add(submit);
		}
		double totalError = 0;
		// now retrieve the result
		for (Future<Double> future : list) {
			try {
				totalError += future.get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
		return totalError / numberOfThreads;
	}

}
