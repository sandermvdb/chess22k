package nl.s22k.chess.texel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import nl.s22k.chess.ChessBoard;
import nl.s22k.chess.eval.MaterialCache;
import nl.s22k.chess.eval.PawnEvalCache;

public class EvalEvaluator {

	private static int numberOfThreads = 7;
	private static ErrorCalculator[] workers = new ErrorCalculator[numberOfThreads];
	private static ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

	public static void main(String[] args) {

		// read all fens, including score
		Map<String, Double> fens = Tuner.loadFens("d:\\backup\\chess\\epds\\quiet-labeled.epd", true, false);
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
		List<Tuning> tuningObjects = Tuner.getTuningObjects();

		// tune
		eval(tuningObjects);
		System.out.println("Done");
		executor.shutdown();
	}

	private static void eval(List<Tuning> tuningObjects) {
		final double bestError = calculateErrorMultiThreaded();
		System.out.println(String.format("%f - org", bestError));
		for (Tuning tuningObject : tuningObjects) {
			tuningObject.clearValues();
			PawnEvalCache.clearValues();
			MaterialCache.clearValues();
			final double newError = calculateErrorMultiThreaded();
			System.out.println(String.format("%f - %s", newError, tuningObject.name));
			tuningObject.restoreValues();
		}
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
