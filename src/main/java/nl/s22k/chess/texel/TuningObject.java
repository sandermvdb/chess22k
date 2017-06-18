package nl.s22k.chess.texel;

import java.util.Arrays;
import java.util.List;

public class TuningObject {

	private int[] values;
	public int[] orgValues;
	public int step;
	public String name;
	public List<Integer> skipValues;
	public int tunedValues;
	public boolean showAverage;
	public boolean allScoresAboveZero;

	public TuningObject(int[] values, int step, String name, boolean showAverage, boolean allScoresAboveZero, Integer... skipValues) {
		this.values = values;
		this.step = step;
		this.showAverage = showAverage;
		this.allScoresAboveZero = allScoresAboveZero;
		while (name.length() < 20) {
			name += " ";
		}
		this.name = name;
		this.skipValues = Arrays.asList(skipValues);
		tunedValues = values.length - this.skipValues.size();
		orgValues = new int[values.length];
		System.arraycopy(values, 0, orgValues, 0, values.length);
	}

	@Override
	public String toString() {
		if (showAverage) {
			int sum = 0;
			for (int i = 0; i < values.length; i++) {
				sum += values[i];
			}
			return name + ": " + Arrays.toString(values) + " (" + sum / values.length + ")";
		}
		return name + ": " + Arrays.toString(values);
	}

	public void addStep(int i) {
		values[i] += step;
	}

	public void removeStep(int i) {
		values[i] -= step;
	}

	public int numberOfParameters() {
		return values.length;
	}

	public boolean skip(int i) {
		return skipValues.contains(i);
	}

	public void printOrgValue() {
		if (showAverage) {
			int sum = 0;
			for (int i = 0; i < values.length; i++) {
				sum += values[i];
			}
			System.out.println(name + ": " + Arrays.toString(orgValues) + " (" + sum / orgValues.length + ")");
		} else {
			System.out.println(name + ": " + Arrays.toString(orgValues));
		}
	}

	public boolean scoreIsZero(int i) {
		return values[i] == 0;
	}

}
