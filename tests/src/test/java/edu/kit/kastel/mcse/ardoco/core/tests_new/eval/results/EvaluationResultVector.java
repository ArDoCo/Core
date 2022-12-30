package edu.kit.kastel.mcse.ardoco.core.tests_new.eval.results;

import org.eclipse.collections.api.factory.Lists;

public class EvaluationResultVector <T> {
    private double precision = 0.0;
    private double recall = 0.0;
    private double f1 = 0.0;
    private double accuracy = 0.0;
    private double phiCoefficient = 0.0;
    private double specificity = 0.0;
    private double phiCoefficientMax = 0.0;
    private double phiOverPhiMax = 0.0;

    public void add(EvaluationResults<T> results) {
        precision += results.precision();
        recall += results.recall();
        f1 += results.f1();
        accuracy += results.accuracy();
        phiCoefficient += results.phiCoefficient();
        specificity += results.specificity();
        phiCoefficient += results.phiCoefficient();
        phiCoefficientMax += results.phiCoefficientMax();
        phiOverPhiMax += results.phiOverPhiMax();
    }

    public void scale(double scale) {
        precision *= scale;
        recall *= scale;
        f1 *= scale;
        accuracy *= scale;
        phiCoefficient *= scale;
        specificity *= scale;
        phiCoefficient *= scale;
        phiCoefficientMax *= scale;
        phiOverPhiMax *= scale;
    }

    public void addWeighted(EvaluationResults<T> results, int weight) {
        precision += results.precision() * weight;
        recall += results.recall() * weight;
        f1 += results.f1() * weight;
        accuracy += results.accuracy() * weight;
        phiCoefficient += results.phiCoefficient() * weight;
        specificity += results.specificity() * weight;
        phiCoefficient += results.phiCoefficient() * weight;
        phiCoefficientMax += results.phiCoefficientMax() * weight;
        phiOverPhiMax += results.phiOverPhiMax() * weight;
    }

    public EvaluationResults<T> toEvaluationResults() {
        return new EvaluationResults<>(precision, recall, f1,
                Lists.immutable.empty(), 0, Lists.immutable.empty(), Lists.immutable.empty(),
                accuracy, phiCoefficient, specificity, phiCoefficientMax, phiOverPhiMax);
    }
}
