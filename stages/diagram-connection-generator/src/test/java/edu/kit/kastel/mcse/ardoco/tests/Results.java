package edu.kit.kastel.mcse.ardoco.tests;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.DiaGSTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.DiaTexTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.DiaWordTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.TraceType;
import edu.kit.kastel.mcse.ardoco.core.api.text.Text;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.ExpectedResults;
import edu.kit.kastel.mcse.ardoco.tests.eval.DiagramProject;

public record Results(DiagramProject project, BigDecimal precision, BigDecimal recall, BigDecimal f1, BigDecimal accuracy, BigDecimal phiCoefficient,
                      BigDecimal phiNormalized, BigDecimal specificity, SortedSet<DiaWordTraceLink> truePositives, SortedSet<DiaWordTraceLink> falsePositives,
                      SortedSet<DiaGSTraceLink> falseNegatives, int TN, ExpectedResults expectedResults, SortedSet<DiaWordTraceLink> all, double[] rawMetrics)
        implements Serializable {

    public static Results create(DiagramProject project, ExpectedResults expected, SortedSet<DiaWordTraceLink> all, SortedSet<DiaWordTraceLink> tpLinks,
            SortedSet<DiaWordTraceLink> fpLinks, SortedSet<DiaGSTraceLink> fnLinks, int TN) {
        var TP = tpLinks.size();
        var FP = fpLinks.size();
        var FN = fnLinks.size();

        var P = TP / (double) (TP + FP);
        var R = TP / (double) (TP + FN);
        var acc = (TP + TN) / (double) (TP + TN + FP + FN);
        var spec = TN / (double) (TN + FP);
        var F1 = 2 * TP / (double) (2 * TP + FP + FN);
        long approachPositives = TP + FP; //P1
        long goldStandardPositives = TP + FN; //P2
        long approachNegatives = TN + FN; //Q1
        long goldStandardNegatives = TN + FP; //Q2
        var R1 = Math.sqrt(approachPositives * goldStandardNegatives);
        var R2 = Math.sqrt(goldStandardPositives * approachNegatives);
        var radiant = approachPositives * approachNegatives * goldStandardPositives * goldStandardNegatives;
        var phiCoefficient = (TP * TN - FP * FN) / Math.sqrt(radiant);
        var phiMax = goldStandardPositives >= approachPositives ? R1 / R2 : R2 / R1;
        var phiNormalized = phiCoefficient / phiMax;

        return new Results(project, toBD(P), toBD(R), toBD(F1), toBD(acc), toBD(phiCoefficient), toBD(phiNormalized), toBD(spec), tpLinks, fpLinks, fnLinks, TN,
                expected, all, new double[] { P, R, F1, acc, spec, phiCoefficient, phiNormalized });
    }

    public static Results create(DiagramProject project, Text text, Set<DiaWordTraceLink> wordTraceLinks, ExpectedResults expected) {
        var allGoldStandardTraceLinks = project.getDiagramTraceLinksAsMap(text.getSentences().toList());
        TreeSet<DiaGSTraceLink> goldStandard = new TreeSet<>(allGoldStandardTraceLinks.getOrDefault(TraceType.ENTITY, List.of()));
        goldStandard.addAll(allGoldStandardTraceLinks.getOrDefault(TraceType.ENTITY_COREFERENCE, List.of()));

        var totalSentences = text.getSentences().size();
        var totalDiagramElements = project.getDiagrams().stream().flatMap(d -> d.getBoxes().stream()).toList().size();
        var total = totalSentences * totalDiagramElements;
        var traceLinks = new TreeSet<>(wordTraceLinks);
        var tpLinks = intersection(traceLinks, goldStandard);
        var fpLinks = difference(traceLinks, goldStandard);
        fpLinks.forEach(fp -> fp.addRelated(
                allGoldStandardTraceLinks.values().stream().flatMap(Collection::stream).filter(fp::equalDEAndSentence).toList()));
        var fnLinks = difference(goldStandard, traceLinks);
        var TP = tpLinks.size();
        var FP = fpLinks.size();
        var FN = fnLinks.size();
        var TN = total - TP - FP - FN;

        return create(project, expected, traceLinks, tpLinks, fpLinks, fnLinks, TN);
    }

    private static BigDecimal toBD(double a) {
        if (Double.isNaN(a))
            return BigDecimal.valueOf(-1337);
        return BigDecimal.valueOf(a).setScale(2, RoundingMode.HALF_UP);
    }

    public static <T extends DiaTexTraceLink> TreeSet<T> intersection(Set<T> a, Set<? extends DiaTexTraceLink> b) {
        return a.stream()
                .filter(fromA -> b.stream().anyMatch(fromB -> fromB.equalDEAndSentence(fromA)))
                .collect(Collectors.toCollection(TreeSet::new));
    }

    public static <T extends DiaTexTraceLink> TreeSet<T> difference(Set<T> a, Set<? extends DiaTexTraceLink> b) {
        return a.stream()
                .filter(fromA -> b.stream().noneMatch(fromB -> fromB.equalDEAndSentence(fromA)))
                .collect(Collectors.toCollection(TreeSet::new));
    }

    public static Results create(DiagramProject project, Text text, Set<DiaWordTraceLink> traceLinks) {
        return create(project, text, traceLinks, new ExpectedResults(0, 0, 0));
    }

    public int TP() {
        return truePositives.size();
    }

    public int FP() {
        return falsePositives.size();
    }

    public int FN() {
        return falseNegatives.size();
    }

    /**
     * {@return Number of positives in the gold standard}
     */
    public int GS_P() {
        return TP() + FN();
    }

    /**
     * {@return Number of positives generated by the approach}
     */
    public int A_P() {
        return TP() + FP();
    }

    /**
     * {@return Number of negatives in the gold standard}
     */
    public int GS_N() {
        return TN() + FP();
    }

    /**
     * {@return Number of negatives generated by the approach}
     */
    public int A_N() {
        return TN() + FN();
    }

    public int total() {
        return TP() + FP() + TN() + FN();
    }

    @Override
    public String toString() {
        var dPrecision = precision().subtract(toBD(expectedResults().precision()));
        var dRecall = recall().subtract(toBD(expectedResults().recall()));
        var dF1 = f1().subtract(toBD(expectedResults().f1()));
        var dAcc = accuracy().subtract(toBD(expectedResults().accuracy()));
        var dPhi = phiCoefficient().subtract(toBD(expectedResults().phiCoefficient()));
        var dSpec = specificity().subtract(toBD(expectedResults().specificity()));
        return MessageFormat.format(
                "TP:{0}, FP:{1}, TN:{2}, FN:{3}, P:{4}({5}), R:{6}({7}), F1:{8}({9}), Acc:{10}({11}), φ:{12}({13}), φN:{14}, Spec:{15}({16})", TP(), FP(), TN(),
                FN(), precision(), dPrecision, recall(), dRecall, f1(), dF1, accuracy(), dAcc, phiCoefficient(), dPhi, phiNormalized(), specificity(), dSpec);
    }

    public String toTableRow() {
        return String.format("%s & %s & %s & %s & %s & %s & %s", precision(), recall(), f1(), accuracy(), specificity(), phiCoefficient(), phiNormalized());
    }

    public boolean asExpected() {
        if (toBD(expectedResults().precision()).compareTo(precision()) > 0)
            return false;
        if (toBD(expectedResults().recall()).compareTo(recall()) > 0)
            return false;
        if (toBD(expectedResults().f1()).compareTo(f1()) > 0)
            return false;
        if (toBD(expectedResults().accuracy()).compareTo(accuracy()) > 0)
            return false;
        if (toBD(expectedResults().phiCoefficient()).compareTo(phiCoefficient()) > 0)
            return false;
        if (toBD(expectedResults().specificity()).compareTo(specificity()) > 0)
            return false;
        return true;
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
        return Objects.equals(this.TN(), other.TN()) && Objects.equals(this.truePositives().size(), other.truePositives().size()) && Objects.equals(
                this.falsePositives().size(), other.falsePositives().size()) && Objects.equals(this.falseNegatives().size(), other.falseNegatives().size());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.TN, this.truePositives(), this.falsePositives(), this.falseNegatives());
    }
}
