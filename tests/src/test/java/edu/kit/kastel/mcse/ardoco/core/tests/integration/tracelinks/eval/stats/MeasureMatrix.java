/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.integration.tracelinks.eval.stats;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.WordSimMeasure;

public class MeasureMatrix {

    private final List<WordSimMeasure> measureList;
    private final int[][] matrix;

    public MeasureMatrix(Collection<WordSimMeasure> measures) {
        this.measureList = new ArrayList<>(measures);
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
