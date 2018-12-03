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
import nl.s22k.chess.ChessBoardUtil;
import nl.s22k.chess.Statistics;
import nl.s22k.chess.eval.EvalConstants;
import nl.s22k.chess.eval.MaterialCache;
import nl.s22k.chess.eval.PawnEvalCache;
import nl.s22k.chess.move.MoveGenerator;

public class Tuner {

	private static MoveGenerator moveGen = new MoveGenerator();

	private static int numberOfThreads = 7;
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
		// tunings.add(new Tuning(EvalConstants.PINNED_ATTACKED, STEP, "Pinned att", 0));
		// tunings.add(new Tuning(EvalConstants.DISCOVERED, STEP, "Discovered", 0));
		// tunings.add(new Tuning(EvalConstants.KNIGHT_OUTPOST, STEP, "Knight outpost", 0, 1));
		// tunings.add(new Tuning(EvalConstants.BISHOP_OUTPOST, STEP, "Bishop outpost", 0, 1));
		// tunings.add(new Tuning(EvalConstants.DOUBLE_ATTACKED, STEP, "Double attacked"));
		// tunings.add(new Tuning(EvalConstants.HANGING, STEP, "Hanging pieces", 0));
		// tunings.add(new Tuning(EvalConstants.HANGING_2, STEP, "Hanging pieces 2", 0));
		// tunings.add(new Tuning(EvalConstants.ROOK_TRAPPED, STEP, "Rook trapped"));
		// tunings.add(new Tuning(EvalConstants.ONLY_MAJOR_DEFENDERS, STEP, "Only major defenders", 0));
		// tunings.add(new Tuning(EvalConstants.NIGHT_PAWN, STEP, "Night pawn"));
		// tunings.add(new Tuning(EvalConstants.ROOK_PAWN, STEP, "Rook pawn"));
		// tunings.add(new Tuning(EvalConstants.BISHOP_PAWN, STEP, "Bishop pawn"));
		// tunings.add(new Tuning(EvalConstants.SPACE, 1, "Space", 0, 1, 2, 3, 4));
		//
		// /* pawns */
		// tunings.add(new Tuning(EvalConstants.PASSED_SCORE_MG, STEP, "Passed score mg", 0));
		// tunings.add(new Tuning(EvalConstants.PASSED_SCORE_EG, STEP, "Passed score eg", 0));
		// tunings.add(new MultiTuning(EvalConstants.PASSED_MULTIPLIERS, "Passed multi"));
		// tunings.add(new MultiTuning(EvalConstants.PASSED_KING_MULTI, "Passed king multi"));
		// tunings.add(new Tuning(EvalConstants.PASSED_CANDIDATE, STEP, "Passed candidate", 0));
		// tunings.add(new Tuning(EvalConstants.SHIELD_BONUS_MG[0], STEP, "Shield 0 mg", 0));
		// tunings.add(new Tuning(EvalConstants.SHIELD_BONUS_MG[1], STEP, "Shield 1 mg", 0));
		// tunings.add(new Tuning(EvalConstants.SHIELD_BONUS_MG[2], STEP, "Shield 2 mg", 0));
		// tunings.add(new Tuning(EvalConstants.SHIELD_BONUS_MG[3], STEP, "Shield 3 mg", 0));
		// tunings.add(new Tuning(EvalConstants.SHIELD_BONUS_EG[0], STEP, "Shield 0 eg", 0));
		// tunings.add(new Tuning(EvalConstants.SHIELD_BONUS_EG[1], STEP, "Shield 1 eg", 0));
		// tunings.add(new Tuning(EvalConstants.SHIELD_BONUS_EG[2], STEP, "Shield 2 eg", 0));
		// tunings.add(new Tuning(EvalConstants.SHIELD_BONUS_EG[3], STEP, "Shield 3 eg", 0));
		// tunings.add(new Tuning(EvalConstants.PAWN_BLOCKAGE, STEP, "Pawn blockage", 0, 1));
		// tunings.add(new Tuning(EvalConstants.PAWN_CONNECTED, STEP, "Pawn connected", 0, 1));
		// tunings.add(new Tuning(EvalConstants.PAWN_NEIGHBOUR, STEP, "Pawn neighbour", 0, 1));
		//
		// /* king-safety */
		// tunings.add(new Tuning(EvalConstants.KS_SCORES, 10, "KS"));
		// tunings.add(new Tuning(EvalConstants.KS_QUEEN_TROPISM, 1, "KS queen", 0, 1));
		// tunings.add(new Tuning(EvalConstants.KS_RANK, 1, "KS rank"));
		// tunings.add(new Tuning(EvalConstants.KS_CHECK, 1, "KS check", 0, 1));
		// tunings.add(new Tuning(EvalConstants.KS_CHECK_QUEEN, 1, "KS check q", 0, 1, 2, 3));
		// tunings.add(new Tuning(EvalConstants.KS_UCHECK, 1, "KS ucheck", 0, 1));
		// tunings.add(new Tuning(EvalConstants.KS_NO_FRIENDS, 1, "KS no friends"));
		// tunings.add(new Tuning(EvalConstants.KS_ATTACKS, 1, "KS attacks"));
		// tunings.add(new Tuning(EvalConstants.KS_DOUBLE_ATTACKS, 1, "KS double attacks"));
		// tunings.add(new Tuning(EvalConstants.KS_ATTACK_PATTERN, 1, "KS pattern"));
		// tunings.add(new Tuning(EvalConstants.KS_OTHER, 1, "KS other"));
		//
		// /* mobility */
		// tunings.add(new Tuning(EvalConstants.MOBILITY_KNIGHT_MG, STEP, "Mobility n", true));
		// tunings.add(new Tuning(EvalConstants.MOBILITY_KNIGHT_EG, STEP, "Mobility n eg", true));
		// tunings.add(new Tuning(EvalConstants.MOBILITY_BISHOP_MG, STEP, "Mobility b", true));
		// tunings.add(new Tuning(EvalConstants.MOBILITY_BISHOP_EG, STEP, "Mobility b eg", true));
		// tunings.add(new Tuning(EvalConstants.MOBILITY_ROOK_MG, STEP, "Mobility r", true));
		// tunings.add(new Tuning(EvalConstants.MOBILITY_ROOK_EG, STEP, "Mobility r eg", true));
		// tunings.add(new Tuning(EvalConstants.MOBILITY_QUEEN_MG, STEP, "Mobility q", true));
		// tunings.add(new Tuning(EvalConstants.MOBILITY_QUEEN_EG, STEP, "Mobility q eg", true));
		// tunings.add(new Tuning(EvalConstants.MOBILITY_KING_MG, STEP, "Mobility k", true));
		// tunings.add(new Tuning(EvalConstants.MOBILITY_KING_EG, STEP, "Mobility k eg", true));
		//
		// /* psqt */
		// tunings.add(new PsqtTuning(EvalConstants.PSQT_MG[ChessConstants.PAWN], STEP, "PSQT p", true));
		// tunings.add(new PsqtTuning(EvalConstants.PSQT_EG[ChessConstants.PAWN], STEP, "PSQT p eg", true));
		// tunings.add(new PsqtTuning(EvalConstants.PSQT_MG[ChessConstants.NIGHT], STEP, "PSQT n"));
		// tunings.add(new PsqtTuning(EvalConstants.PSQT_EG[ChessConstants.NIGHT], STEP, "PSQT n eg"));
		// tunings.add(new PsqtTuning(EvalConstants.PSQT_MG[ChessConstants.BISHOP], STEP, "PSQT b"));
		// tunings.add(new PsqtTuning(EvalConstants.PSQT_EG[ChessConstants.BISHOP], STEP, "PSQT b eg"));
		// tunings.add(new PsqtTuning(EvalConstants.PSQT_MG[ChessConstants.ROOK], STEP, "PSQT r"));
		// tunings.add(new PsqtTuning(EvalConstants.PSQT_EG[ChessConstants.ROOK], STEP, "PSQT r eg"));
		// tunings.add(new PsqtTuning(EvalConstants.PSQT_MG[ChessConstants.QUEEN], STEP, "PSQT q"));
		// tunings.add(new PsqtTuning(EvalConstants.PSQT_EG[ChessConstants.QUEEN], STEP, "PSQT q eg"));
		// tunings.add(new PsqtTuning(EvalConstants.PSQT_MG[ChessConstants.KING], STEP, "PSQT k"));
		// tunings.add(new PsqtTuning(EvalConstants.PSQT_EG[ChessConstants.KING], STEP, "PSQT k eg"));

		return tunings;
	}

	public static void main(String[] args) {
		// read all fens, including score
		Map<String, Double> fens = loadFens("d:\\backup\\chess\\epds\\quiet-labeled.epd", true, false);
		System.out.println("Fens found : " + fens.size());

		// init workers
		ChessBoard.initInstances(numberOfThreads);
		for (int i = 0; i < numberOfThreads; i++) {
			workers[i] = new ErrorCalculator(ChessBoard.getInstance(i));
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
		int stalemate = 0;

		try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
			String line = br.readLine();

			while (line != null) {

				String[] values = line.split(" c9 ");
				double score = 0;
				if (containsResult) {
					if (values[1].equals("\"1/2-1/2\";")) {
						score = 0.5;
					} else if (values[1].equals("\"1-0\";")) {
						score = 1;
					} else if (values[1].equals("\"0-1\";")) {
						score = 0;
					} else {
						throw new RuntimeException("Unknown result: " + values[1]);
					}
				}

				ChessBoard cb = ChessBoardUtil.getNewCB(values[0]);
				if (includingCheck || cb.checkingPieces == 0) {
					moveGen.startPly();
					moveGen.generateAttacks(cb);
					moveGen.generateMoves(cb);
					if (moveGen.hasNext()) {
						fens.put(values[0], score);
					} else {
						stalemate++;
					}
					moveGen.endPly();
				} else {
					checkCount++;
				}

				line = br.readLine();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		System.out.println("In check   : " + checkCount);
		System.out.println("Stalemate  : " + stalemate);
		return fens;
	}

	private static void printInfo(List<Tuning> tuningObjects) {
		Statistics.reset();
		System.out.println("\nNumber of threads: " + numberOfThreads);
		System.out.println("\nValues that are being tuned:");

		int totalValues = 0;
		for (Tuning tuningObject : tuningObjects) {
			if (tuningObject.showAverage) {
				System.out.println(tuningObject.name + " " + tuningObject.getAverage());
			} else {
				System.out.println(tuningObject.name);
			}
			totalValues += tuningObject.tunedValues;
		}
		System.out.println(String.format("\nInitial error: %s (%s ms)", calculateErrorMultiThreaded(), Statistics.getPassedTimeMs()));
		System.out.println("Total values to be tuned: " + totalValues + "\n");
	}

	private static void localOptimize(List<Tuning> tuningObjects) {
		double bestError = calculateErrorMultiThreaded();
		orgError = bestError;
		boolean improved = true;
		while (improved) {
			improved = false;
			for (Tuning tuningObject : tuningObjects) {
				for (int i = 0; i < tuningObject.numberOfParameters(); i++) {
					if (tuningObject.skip(i)) {
						continue;
					}
					tuningObject.addStep(i);
					EvalConstants.initMgEg();
					PawnEvalCache.clearValues();
					MaterialCache.clearValues();
					double newError = calculateErrorMultiThreaded();
					if (newError < bestError) {
						bestError = newError;
						System.out.println(String.format("%f - %s", bestError, tuningObject));
						improved = true;
					} else {
						tuningObject.removeStep(i);
						tuningObject.removeStep(i);
						EvalConstants.initMgEg();
						PawnEvalCache.clearValues();
						MaterialCache.clearValues();
						newError = calculateErrorMultiThreaded();
						if (newError < bestError) {
							bestError = newError;
							System.out.println(String.format("%f - %s", bestError, tuningObject));
							improved = true;
						} else {
							tuningObject.addStep(i);
						}
					}
				}
			}
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
