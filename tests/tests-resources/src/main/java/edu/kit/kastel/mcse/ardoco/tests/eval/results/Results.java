package edu.kit.kastel.mcse.ardoco.tests.eval.results;

import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.DiaGSTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.DiaWordTraceLink;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.DiaTexTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.TraceType;
import edu.kit.kastel.mcse.ardoco.core.api.text.Text;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.ExpectedResults;
import edu.kit.kastel.mcse.ardoco.tests.eval.DiagramProject;

public record Results(BigDecimal precision, BigDecimal recall, BigDecimal f1, BigDecimal accuracy, BigDecimal phiCoefficient, BigDecimal specificity,
                      SortedSet<DiaWordTraceLink> truePositives, SortedSet<DiaWordTraceLink> falsePositives, SortedSet<DiaGSTraceLink> falseNegatives, int TN,
                      ExpectedResults expectedResults, SortedSet<DiaWordTraceLink> all) {

    public static Results create(DiagramProject project, Text text, Set<DiaWordTraceLink> wordTraceLinks, ExpectedResults expected) {
        var allGoldStandardTraceLinks = project.getDiagramTraceLinksAsMap(text.getSentences().toList());
        TreeSet<DiaGSTraceLink> goldStandard = new TreeSet<>(allGoldStandardTraceLinks.getOrDefault(TraceType.ENTITY, List.of()));

        var totalSentences = text.getSentences().size();
        var totalDiagramElements = project.getDiagrams().stream().flatMap(d -> d.getBoxes().stream()).toList().size();
        var total = totalSentences * totalDiagramElements;
        var traceLinks = new TreeSet<>(wordTraceLinks);
        var tpLinks = intersection(traceLinks, goldStandard);
        var TP = tpLinks.size();
        var fpLinks = difference(traceLinks, goldStandard);
        fpLinks.forEach(fp -> fp.addRelated(allGoldStandardTraceLinks.values().stream().flatMap(Collection::stream).filter(fp::equalEndpoints).toList()));
        var FP = fpLinks.size();
        var fnLinks = difference(goldStandard, traceLinks);
        var FN = fnLinks.size();
        var TN = total - TP - FP - FN;
        var P = TP / (double) (TP + FP);
        var R = TP / (double) (TP + FN);
        var acc = (TP + TN) / (double) (TP + TN + FP + FN);
        var spec = TN / (double) (TN + FP);
        var f1 = 2 * TP / (double) (2 * TP + FP + FN);
        var radiant = (TP + FP) * (TP + FN) * (TN + FP) * ((long) TN + FN);
        var phiCoefficient = (TP * TN - FP * FN) / Math.sqrt(radiant);

        return new Results(toBD(P), toBD(R), toBD(f1), toBD(acc), toBD(phiCoefficient), toBD(spec), tpLinks, fpLinks, fnLinks, TN, expected, traceLinks);
    }

    private static BigDecimal toBD(double a) {
        return BigDecimal.valueOf(a).setScale(3, RoundingMode.HALF_UP);
    }

    private static <T extends DiaTexTraceLink> TreeSet<T> intersection(Set<T> a, Set<? extends DiaTexTraceLink> b) {
        return a.stream().filter(fromA -> b.stream().anyMatch(fromB -> fromB.equalEndpoints(fromA))).collect(Collectors.toCollection(TreeSet::new));
    }

    private static <T extends DiaTexTraceLink> TreeSet<T> difference(Set<T> a, Set<? extends DiaTexTraceLink> b) {
        return a.stream().filter(fromA -> b.stream().noneMatch(fromB -> fromB.equalEndpoints(fromA))).collect(Collectors.toCollection(TreeSet::new));
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
        return MessageFormat.format("TP:{0}, FP:{1}, TN:{2}, FN:{3}, P:{4}({5}), R:{6}({7}), F1:{8}({9}), Acc:{10}({11}), φ:{12}({13}), Spec:{14}({15})", TP(),
                FP(), TN(), FN(), precision(), dPrecision, recall(), dRecall, f1(), dF1, accuracy(), dAcc, phiCoefficient(), dPhi, specificity(), dSpec);
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
}
