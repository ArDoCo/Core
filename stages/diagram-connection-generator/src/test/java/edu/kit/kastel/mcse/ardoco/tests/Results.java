/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.tests;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.DiagramGoldStandardTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.DiagramTextTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.DiagramWordTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.text.Text;
import edu.kit.kastel.mcse.ardoco.core.data.GlobalConfiguration;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.ExpectedResults;
import edu.kit.kastel.mcse.ardoco.tests.eval.DiagramProject;

public record Results(DiagramProject project, SortedSet<DiagramWordTraceLink> truePositives, SortedSet<DiagramWordTraceLink> falsePositives,
                      SortedSet<DiagramGoldStandardTraceLink> falseNegatives, long TN, ExpectedResults expectedResults, SortedSet<DiagramWordTraceLink> all)
        implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(Results.class);

    public static Results create(GlobalConfiguration globalConfiguration, DiagramProject project, Text text, Set<DiagramWordTraceLink> wordTraceLinks,
            ExpectedResults expected) {
        var allGoldStandardTraceLinks = project.getDiagramTraceLinksAsMap(text.getSentences().toList());
        TreeSet<DiagramGoldStandardTraceLink> goldStandard = new TreeSet<>(allGoldStandardTraceLinks.entrySet()
                .stream()
                .filter(e -> e.getKey().isActualPositive())
                .flatMap(e -> e.getValue().stream())
                .toList());

        var totalSentences = text.getSentences().size();
        var totalDiagramElements = project.getDiagramsGoldStandard().stream().flatMap(d -> d.getBoxes().stream()).toList().size();
        var total = totalSentences * totalDiagramElements;
        var traceLinks = new TreeSet<>(wordTraceLinks);
        var tpLinks = intersection(globalConfiguration, traceLinks, goldStandard);
        var fpLinks = difference(globalConfiguration, traceLinks, goldStandard);
        fpLinks.forEach(fp -> fp.addRelated(allGoldStandardTraceLinks.values()
                .stream()
                .flatMap(Collection::stream)
                .filter(oth -> fp.similar(globalConfiguration, oth))
                .toList()));
        var fnLinks = difference(globalConfiguration, goldStandard, traceLinks);
        var TP = tpLinks.size();
        var FP = fpLinks.size();
        var FN = fnLinks.size();
        var TN = total - TP - FP - FN;

        return new Results(project, tpLinks, fpLinks, fnLinks, TN, expected, traceLinks);
    }

    private static BigDecimal toBD(double a) {
        if (Double.isNaN(a))
            return BigDecimal.valueOf(-1337);
        return BigDecimal.valueOf(a).setScale(2, RoundingMode.HALF_UP);
    }

    public static <T extends DiagramTextTraceLink> TreeSet<T> intersection(GlobalConfiguration globalConfiguration, Set<T> a,
            Set<? extends DiagramTextTraceLink> b) {
        return a.stream()
                .filter(fromA -> b.stream().anyMatch(fromB -> fromB.similar(globalConfiguration, fromA)))
                .collect(Collectors.toCollection(TreeSet::new));
    }

    public static <T extends DiagramTextTraceLink> TreeSet<T> difference(GlobalConfiguration globalConfiguration, Set<T> a,
            Set<? extends DiagramTextTraceLink> b) {
        return a.stream()
                .filter(fromA -> b.stream().noneMatch(fromB -> fromB.similar(globalConfiguration, fromA)))
                .collect(Collectors.toCollection(TreeSet::new));
    }

    public double precision() {
        return TP() / (double) (TP() + FP());
    }

    public double deltaPrecision() {
        return toBD(precision()).subtract(toBD(expectedResults().precision())).doubleValue();
    }

    public double recall() {
        return TP() / (double) (TP() + FN());
    }

    public double deltaRecall() {
        return toBD(recall()).subtract(toBD(expectedResults().recall())).doubleValue();
    }

    public double f1() {
        return 2 * TP() / (double) (2 * TP() + FP() + FN());
    }

    public double deltaF1() {
        return toBD(f1()).subtract(toBD(expectedResults().f1())).doubleValue();
    }

    public double accuracy() {
        return (TP() + TN()) / (double) (TP() + TN() + FP() + FN());
    }

    public double deltaAccuracy() {
        return toBD(accuracy()).subtract(toBD(expectedResults().accuracy())).doubleValue();
    }

    public double specificity() {
        return TN() / (double) (TN() + FP());
    }

    public double deltaSpecificity() {
        return toBD(specificity()).subtract(toBD(expectedResults().specificity())).doubleValue();
    }

    public double phiCoefficient() {
        return (TP() * TN() - FP() * FN()) / Math.sqrt(approachPositives() * approachNegatives() * goldStandardPositives() * goldStandardNegatives());
    }

    public double deltaPhiCoefficient() {
        return toBD(phiCoefficient()).subtract(toBD(expectedResults().phiCoefficient())).doubleValue();
    }

    public double phiNormalized() {
        var R1 = Math.sqrt(approachPositives() * goldStandardNegatives());
        var R2 = Math.sqrt(goldStandardPositives() * approachNegatives());
        var phiMax = goldStandardPositives() >= approachPositives() ? R1 / R2 : R2 / R1;
        return phiCoefficient() / phiMax;
    }

    public long TP() {
        return truePositives.size();
    }

    public long FP() {
        return falsePositives.size();
    }

    public long FN() {
        return falseNegatives.size();
    }

    /**
     * {@return Number of positives in the gold standard}
     */
    public long goldStandardPositives() {
        return TP() + FN();
    }

    /**
     * {@return Number of positives generated by the approach}
     */
    public long approachPositives() {
        return TP() + FP();
    }

    /**
     * {@return Number of negatives in the gold standard}
     */
    public long goldStandardNegatives() {
        return TN() + FP();
    }

    /**
     * {@return Number of negatives generated by the approach}
     */
    public long approachNegatives() {
        return TN() + FN();
    }

    public long totalPredictions() {
        return approachPositives() + approachNegatives();
    }

    @Override
    public String toString() {
        return String.format(Locale.US,
                "TP:%d, FP:%d, TN:%d, FN:%d, P:%.2f(%.2f), R:%.2f(%.2f), F1:%.2f(%.2f), Acc:%.2f(%.2f), Spec:%.2f(%.2f), φ:%.2f(%.2f), φN:%.2f", TP(), FP(),
                TN(), FN(), precision(), deltaPrecision(), recall(), deltaRecall(), f1(), deltaF1(), accuracy(), deltaAccuracy(), specificity(),
                deltaSpecificity(), phiCoefficient(), deltaPhiCoefficient(), phiNormalized());
    }

    public boolean asExpected() {
        var asExpected = true;
        if (toBD(expectedResults().precision()).compareTo(toBD(precision())) > 0)
            asExpected = false;
        if (toBD(expectedResults().recall()).compareTo(toBD(recall())) > 0)
            asExpected = false;
        if (toBD(expectedResults().f1()).compareTo(toBD(f1())) > 0)
            asExpected = false;
        if (toBD(expectedResults().accuracy()).compareTo(toBD(accuracy())) > 0)
            asExpected = false;
        if (toBD(expectedResults().phiCoefficient()).compareTo(toBD(phiCoefficient())) > 0)
            asExpected = false;
        if (toBD(expectedResults().specificity()).compareTo(toBD(specificity())) > 0)
            asExpected = false;
        if (asExpected) {
            logger.info("Results are as expected. " + toString());
        } else {
            logger.warn("Results are not as expected! " + toString());
        }
        return asExpected;
    }

    public Map<String, Double> mapOfMetrics() {
        var map = new LinkedHashMap<String, Double>();
        map.put("P", precision());
        map.put("R", recall());
        map.put("F1", f1());
        map.put("Acc", accuracy());
        map.put("Spec", specificity());
        map.put("Phi", phiCoefficient());
        map.put("PhiN", phiNormalized());
        return map;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj instanceof Results other) {
            return Objects.equals(this.TN(), other.TN()) && Objects.equals(this.truePositives(), other.truePositives()) && Objects.equals(this.falsePositives(),
                    other.falsePositives()) && Objects.equals(this.falseNegatives(), other.falseNegatives());
        }
        return false;
    }

    public boolean equalsByConfusionMatrix(Results other) {
        if (other == null)
            return false;
        if (this == other)
            return true;
        return Objects.equals(this.TN(), other.TN()) && Objects.equals(this.truePositives().size(), other.truePositives().size()) && Objects.equals(this
                .falsePositives()
                .size(), other.falsePositives().size()) && Objects.equals(this.falseNegatives().size(), other.falseNegatives().size());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.TN, this.truePositives(), this.falsePositives(), this.falseNegatives());
    }
}
