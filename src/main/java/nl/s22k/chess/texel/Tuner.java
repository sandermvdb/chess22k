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
import nl.s22k.chess.engine.EngineConstants;
import nl.s22k.chess.eval.EvalConstants;
import nl.s22k.chess.move.MagicUtil;
import nl.s22k.chess.move.MoveGenerator;
import nl.s22k.chess.move.MoveList;

public class Tuner {

	private static int numberOfThreads = 6;
	private static ErrorCalculator[] workers = new ErrorCalculator[numberOfThreads];
	private static ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

	private static double orgError;
	private static double bestError;

	private static List<TuningObject> getTuningObjects() {
		List<TuningObject> tuningObjects = new ArrayList<TuningObject>();

		// tuningObjects.add(new TuningObject(EvalConstants.PHASE, 1, "Phase", false, false, 0, 1));

		tuningObjects.add(new TuningObject(EvalConstants.INDIVIDUAL_SCORES, 2, "Individual score", false, false));
		// tuningObjects.add(new TuningObject(EvalConstants.MATERIAL_SCORES, 5, "Material", false, false, 0, 1, 6));
		// tuningObjects.add(new TuningObject(EvalConstants.PINNED_PIECE_SCORES, 4, "Pinned pieces", false, false, 0));
		// tuningObjects.add(new TuningObject(EvalConstants.DISCOVERED_PIECE_SCORES, 4, "Disco pieces", false, false,
		// 0));
		// tuningObjects.add(new TuningObject(EvalConstants.KNIGHT_OUTPOST, 4, "Knight outpost", false, false, 0, 1));
		// tuningObjects.add(new TuningObject(EvalConstants.BISHOP_OUTPOST, 4, "Bishop outpost", false, false, 0, 1));
		// tuningObjects.add(new TuningObject(EvalConstants.NIGHT_PAWN_BONUS, 4, "Night pawn", false, false));
		// tuningObjects.add(new TuningObject(EvalConstants.HANGING_PIECES, 4, "Hanging pieces", false, false, 0));
		// tuningObjects.add(new TuningObject(EvalConstants.HANGING_PIECES_2, 4, "Hanging pieces 2", false, false, 0));
		// tuningObjects.add(new TuningObject(EvalConstants.ROOK_TRAPPED, 4, "Rook trapped", false, false));
		//
		// /* pawns */
		// tuningObjects.add(new TuningObject(EvalConstants.PASSED_PAWN_SCORE_EG, 5, "Passed-p eg", false, false, 0,
		// 7));
		// tuningObjects.add(new TuningObject(EvalConstants.PASSED_PAWN_MULTIPLIERS, 1, "Passed-p multiplier", false,
		// false));
		// tuningObjects.add(new TuningObject(EvalConstants.PASSED_PAWN_CANDIDATE, 5, "Passed-p candidates", false,
		// false, 0, 6, 7));
		// tuningObjects.add(new TuningObject(EvalConstants.PAWN_SHIELD_BONUS[0], 4, "Pawn shield 0", false, false, 0,
		// 7));
		// tuningObjects.add(new TuningObject(EvalConstants.PAWN_SHIELD_BONUS[1], 4, "Pawn shield 1", false, false, 0,
		// 7));
		// tuningObjects.add(new TuningObject(EvalConstants.PAWN_SHIELD_BONUS[2], 4, "Pawn shield 2", false, false, 0,
		// 7));
		// tuningObjects.add(new TuningObject(EvalConstants.BACKWARD_PAWN_ATTACKED, 4, "B-pawn attacked", false,
		// false));
		// tuningObjects.add(new TuningObject(EvalConstants.PAWN_BLOCKAGE, 4, "Pawn blockage", false, false, 0, 1));
		// tuningObjects.add(new TuningObject(EvalConstants.PAWN_CONNECTED, 4, "Pawn connected", false, false, 0, 1));
		//
		// /* king-safety */
		// // tuningObjects.add(new TuningObject(EvalConstants.KING_SAFETY_SCORES, 10, 1500, "KS", false, false));
		// tuningObjects.add(new TuningObject(EvalConstants.KING_SAFETY_QUEEN_TROPISM, 1, "KS queen", false, true, 0,
		// 1));
		// tuningObjects.add(new TuningObject(EvalConstants.KING_SAFETY_COUNTER_RANKS, 1, "KS counter ranks", false,
		// true));
		// tuningObjects.add(new TuningObject(EvalConstants.KING_SAFETY_CHECK_NIGHT, 1, "KS check night", false, true));
		// tuningObjects.add(new TuningObject(EvalConstants.KING_SAFETY_CHECK_BISHOP, 1, "KS check bishop", false,
		// true));
		// tuningObjects.add(new TuningObject(EvalConstants.KING_SAFETY_CHECK_ROOK, 1, "KS check rook", false, true));
		// tuningObjects.add(new TuningObject(EvalConstants.KING_SAFETY_CHECK_QUEEN, 1, "KS check queen", false, true,
		// 0, 1, 2, 3));
		// tuningObjects.add(new TuningObject(EvalConstants.KING_SAFETY_NO_FRIENDS, 1, "KS no friends nearby", false,
		// false));
		// tuningObjects.add(new TuningObject(EvalConstants.KING_SAFETY_ATTACKS, 1, "KS attacks", false, false));
		// tuningObjects.add(new TuningObject(EvalConstants.KING_SAFETY_ATTACK_PATTERN_COUNTER, 1, 9, "KS pattern",
		// false, true));
		// tuningObjects.add(new TuningObject(EvalConstants.KING_SAFETY_COUNTERS, 1, 20, "KS counters", false, true));
		// tuningObjects.add(new TuningObject(EvalConstants.KING_SAFETY_DOUBLE_PAWN, 1, "KS double pawn", false, true));
		// tuningObjects.add(new TuningObject(EvalConstants.KING_SAFETY_DOUBLE_NIGHT, 1, "KS double night", false,
		// true));
		// tuningObjects.add(new TuningObject(EvalConstants.KING_SAFETY_DOUBLE_BISHOP, 1, "KS double bishop", false,
		// true));
		// tuningObjects.add(new TuningObject(EvalConstants.KING_SAFETY_DOUBLE_ROOK, 1, 9, "KS double rook", false,
		// true));
		//
		// /* mobility */
		// tuningObjects.add(new TuningObject(EvalConstants.MOBILITY_KNIGHT, 4, "Mobility knight", true, false));
		// tuningObjects.add(new TuningObject(EvalConstants.MOBILITY_KNIGHT_EG, 4, "Mobility knight eg", true, false));
		// tuningObjects.add(new TuningObject(EvalConstants.MOBILITY_BISHOP, 4, "Mobility bishop", true, false));
		// tuningObjects.add(new TuningObject(EvalConstants.MOBILITY_BISHOP_EG, 4, "Mobility bishop eg", true, false));
		// tuningObjects.add(new TuningObject(EvalConstants.MOBILITY_ROOK, 4, "Mobility rook", true, false));
		// tuningObjects.add(new TuningObject(EvalConstants.MOBILITY_ROOK_EG, 4, "Mobility rook eg", true, false));
		// tuningObjects.add(new TuningObject(EvalConstants.MOBILITY_QUEEN, 4, "Mobility queen", true, false));
		// tuningObjects.add(new TuningObject(EvalConstants.MOBILITY_QUEEN_EG, 4, "Mobility queen eg", true, false));
		// tuningObjects.add(new TuningObject(EvalConstants.MOBILITY_KING, 4, "Mobility king", true, false));
		// tuningObjects.add(new TuningObject(EvalConstants.MOBILITY_KING_EG, 4, "Mobility king eg", true, false));
		//
		// /* psqt */
		// tuningObjects.add(new PsqtTuningObject(EvalConstants.PSQT_SCORES[ChessConstants.PAWN], 5, "PSQT pawn", true,
		// 0, 1, 2, 3, 4, 5, 6, 7, 56, 57, 58, 59, 60,
		// 61, 62, 63));
		// tuningObjects.add(new PsqtTuningObject(EvalConstants.PSQT_EG_SCORES[ChessConstants.PAWN], 5, "PSQT pawn-eg",
		// true, 0, 1, 2, 3, 4, 5, 6, 7, 56, 57, 58,
		// 59, 60, 61, 62, 63));
		// tuningObjects.add(new PsqtTuningObject(EvalConstants.PSQT_SCORES[ChessConstants.NIGHT], 5, "PSQT knight",
		// true));
		// tuningObjects.add(new PsqtTuningObject(EvalConstants.PSQT_EG_SCORES[ChessConstants.NIGHT], 5,
		// "PSQT-knight-eg", true));
		// tuningObjects.add(new PsqtTuningObject(EvalConstants.PSQT_SCORES[ChessConstants.BISHOP], 5, "PSQT bishop",
		// true));
		// tuningObjects.add(new PsqtTuningObject(EvalConstants.PSQT_EG_SCORES[ChessConstants.BISHOP], 5,
		// "PSQT-bishop-eg", true));
		// tuningObjects.add(new PsqtTuningObject(EvalConstants.PSQT_SCORES[ChessConstants.ROOK], 5, "PSQT rook",
		// true));
		// tuningObjects.add(new PsqtTuningObject(EvalConstants.PSQT_EG_SCORES[ChessConstants.ROOK], 5, "PSQT rook-eg",
		// true));
		// tuningObjects.add(new PsqtTuningObject(EvalConstants.PSQT_SCORES[ChessConstants.QUEEN], 5, "PSQT queen",
		// true));
		// tuningObjects.add(new PsqtTuningObject(EvalConstants.PSQT_EG_SCORES[ChessConstants.QUEEN], 5,
		// "PSQT-queen-eg", true));
		// tuningObjects.add(new PsqtTuningObject(EvalConstants.PSQT_SCORES[ChessConstants.KING], 5, "PSQT king",
		// true));
		// tuningObjects.add(new PsqtTuningObject(EvalConstants.PSQT_EG_SCORES[ChessConstants.KING], 5, "PSQT king eg",
		// true));

		return tuningObjects;
	}

	public static void main(String[] args) {
		// setup
		EngineConstants.ENABLE_PAWN_EVAL_CACHE = false;
		EngineConstants.isTuningSession = true;
		MagicUtil.init();

		// read all fens, including score
		Map<String, Double> fens = loadFens("d:\\backup\\chess\\epds\\quiet-labeled.epd", true, false);
		System.out.println("Fens found : " + fens.size());

		// init workers
		ChessBoard.initTuningInstances(numberOfThreads);
		for (int i = 0; i < numberOfThreads; i++) {
			workers[i] = new ErrorCalculator(ChessBoard.getTuningInstance(i));
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
		List<TuningObject> tuningObjects = getTuningObjects();

		// tune
		printInfo(tuningObjects);
		localOptimize(tuningObjects);
		executor.shutdown();
		System.out.println(String.format("\nDone: %s -> %s\n", orgError, bestError));
		for (TuningObject tuningObject : tuningObjects) {
			tuningObject.printOrgValues();
			tuningObject.printNewValues();
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
				if (cb.checkingPieces == 0) {
					MoveList.startPly();
					MoveGenerator.generateAttacks(cb);
					MoveGenerator.generateMoves(cb);
					if (MoveList.hasNext()) {
						MoveList.skipMoves();
						fens.put(values[0], score);
					} else {
						stalemate++;
					}
					MoveList.endPly();
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

	private static void printInfo(List<TuningObject> tuningObjects) {
		Statistics.reset();
		System.out.println("\nNumber of threads: " + numberOfThreads);
		System.out.println("\nValues that are being tuned:");

		int totalValues = 0;
		for (TuningObject tuningObject : tuningObjects) {
			tuningObject.printOrgValues();
			totalValues += tuningObject.tunedValues;
		}
		System.out.println(String.format("\nInitial error: %s (%s ms)", calculateErrorMultiThreaded(), Statistics.getPassedTimeMs()));
		System.out.println("Total values to be tuned: " + totalValues + "\n");
	}

	private static void localOptimize(List<TuningObject> tuningObjects) {
		double bestError = calculateErrorMultiThreaded();
		orgError = bestError;
		boolean improved = true;
		while (improved) {
			improved = false;
			for (TuningObject tuningObject : tuningObjects) {
				for (int i = 0; i < tuningObject.numberOfParameters(); i++) {
					if (tuningObject.skip(i)) {
						continue;
					}
					if (tuningObject.isMaxReached(i)) {
						if (tuningObject.allScoresAboveZero && tuningObject.scoreIsZero(i)) {
							continue;
						}
						tuningObject.removeStep(i);
						double newError = calculateErrorMultiThreaded();
						newError = calculateErrorMultiThreaded();
						if (newError < bestError) {
							bestError = newError;
							System.out.println(String.format("%s: %f", tuningObject, bestError));
							improved = true;
						} else {
							tuningObject.addStep(i);
						}
					} else {
						tuningObject.addStep(i);
						double newError = calculateErrorMultiThreaded();
						if (newError < bestError) {
							bestError = newError;
							System.out.println(String.format("%s: %f", tuningObject, bestError));
							improved = true;
						} else {
							tuningObject.removeStep(i);
							if (tuningObject.allScoresAboveZero && tuningObject.scoreIsZero(i)) {
								continue;
							}
							tuningObject.removeStep(i);
							newError = calculateErrorMultiThreaded();
							if (newError < bestError) {
								bestError = newError;
								System.out.println(String.format("%s: %f", tuningObject, bestError));
								improved = true;
							} else {
								tuningObject.addStep(i);
							}
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
