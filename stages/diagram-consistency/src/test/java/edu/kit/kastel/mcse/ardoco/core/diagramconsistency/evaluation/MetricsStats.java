/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.diagramconsistency.evaluation;

/**
 * Helps calculating statistics about the metrics.
 */
public class MetricsStats {
    private double minF1Score = Double.MAX_VALUE;
    private double maxF1Score = Double.MIN_VALUE;
    private double totalPrecision = 0;
    private double totalRecall = 0;
    private double totalF1Score = 0;
    private double count = 0;
    private double totalWeightedF1Score = 0;
    private double totalWeight = 0;

    /**
     * Consider the metrics for the statistics.
     *
     * @param metrics
     *                The metrics to add.
     * @param weight
     *                The weight of the metrics.
     */
    public void add(Metrics metrics, double weight) {
        this.minF1Score = Math.min(this.minF1Score, metrics.getF1Score());
        this.maxF1Score = Math.max(this.maxF1Score, metrics.getF1Score());
        this.totalPrecision += metrics.getPrecision();
        this.totalRecall += metrics.getRecall();
        this.totalF1Score += metrics.getF1Score();
        this.count++;
        this.totalWeightedF1Score += metrics.getF1Score() * weight;
        this.totalWeight += weight;
    }

    /**
     * The minimum F1 score.
     */
    public double getMinF1Score() {
        return this.minF1Score;
    }

    /**
     * The maximum F1 score.
     */
    public double getMaxF1Score() {
        return this.maxF1Score;
    }

    /**
     * The average precision.
     */
    public double getAveragePrecision() {
        return this.totalPrecision / this.count;
    }

    /**
     * The average recall.
     */
    public double getAverageRecall() {
        return this.totalRecall / this.count;
    }

    /**
     * The average F1 score.
     */
    public double getAverageF1Score() {
        return this.totalF1Score / this.count;
    }

    /**
     * The weighted average F1 score.
     */
    public double getWeightedAverageF1Score() {
        return this.totalWeightedF1Score / this.totalWeight;
    }

    /**
     * The number of metrics considered.
     */
    public int getCount() {
        return (int) this.count;
    }
}
