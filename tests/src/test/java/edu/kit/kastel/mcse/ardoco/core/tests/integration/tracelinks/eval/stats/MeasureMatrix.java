package edu.kit.kastel.mcse.ardoco.core.tests.integration.tracelinks.eval.stats;

import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.WordSimMeasure;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MeasureMatrix {

	private final List<WordSimMeasure> measureList;
	private final int[][] matrix;

	public MeasureMatrix(List<WordSimMeasure> measures) {
		this.measureList = measures;
		this.matrix = new int[measures.size()][measures.size()];
	}

	public List<WordSimMeasure> getMeasures() {
		return measureList;
	}

	public void increment(WordSimMeasure row, WordSimMeasure column) {
		int rowIndex = measureList.indexOf(row);
		int colIndex = measureList.indexOf(column);

		matrix[rowIndex][colIndex]++;
	}

	public int get(WordSimMeasure row, WordSimMeasure column) {
		int rowIndex = measureList.indexOf(row);
		int colIndex = measureList.indexOf(column);

		return matrix[rowIndex][colIndex];
	}

}
