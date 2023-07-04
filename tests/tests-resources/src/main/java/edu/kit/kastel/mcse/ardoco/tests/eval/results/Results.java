package edu.kit.kastel.mcse.ardoco.tests.eval.results;

import java.text.MessageFormat;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.DiaTexTraceLink;
import edu.kit.kastel.mcse.ardoco.core.api.text.Text;
import edu.kit.kastel.mcse.ardoco.core.tests.eval.results.ExpectedResults;
import edu.kit.kastel.mcse.ardoco.tests.eval.DiagramProject;

public record Results(double precision, double recall, double f1, double accuracy, double phiCoefficient, double specificity,
                      SortedSet<DiaTexTraceLink> truePositives, SortedSet<DiaTexTraceLink> falsePositives, SortedSet<DiaTexTraceLink> falseNegatives, int TN,
                      ExpectedResults expectedResults) {

    public static Results create(DiagramProject project, Text text, Set<DiaTexTraceLink> traceLinks, ExpectedResults expected) {
        var goldStandardTraceLinks = project.getDiagramTextTraceLinksFromGoldstandard().stream().peek(t -> t.setText(text)).sorted().toList();

        var totalSentences = text.getSentences().size();
        var totalDiagramElements = project.getDiagramsFromGoldstandard().stream().flatMap(d -> d.getBoxes().stream()).toList().size();
        var total = totalSentences * totalDiagramElements;
        var tpLinks = new TreeSet<>(
                goldStandardTraceLinks.stream().filter(g -> traceLinks.stream().anyMatch(t -> t.equalEndpoints(g))).collect(Collectors.toSet()));
        var TP = tpLinks.size();
        var fpLinks = new TreeSet<>(
                traceLinks.stream().filter(t -> goldStandardTraceLinks.stream().noneMatch(g -> g.equalEndpoints(t))).collect(Collectors.toSet()));
        var FP = fpLinks.size();
        var fnLinks = new TreeSet<>(
                goldStandardTraceLinks.stream().filter(g -> traceLinks.stream().noneMatch(t -> t.equalEndpoints(g))).collect(Collectors.toSet()));
        var FN = fnLinks.size();
        var TN = total - TP - FP - FN;
        var P = TP / (double) (TP + FP);
        var R = TP / (double) (TP + FN);
        var acc = (TP + TN) / (double) (TP + TN + FP + FN);
        var spec = TN / (double) (TN + FP);
        var f1 = 2 * TP / (double) (2 * TP + FP + FN);
        var radiant = (TP + FP) * (TP + FN) * (TN + FP) * ((long) TN + FN);
        var phiCoefficient = (TP * TN - FP * FN) / Math.sqrt(radiant);

        return new Results(P, R, f1, acc, phiCoefficient, spec, tpLinks, fpLinks, fnLinks, TN, expected);
    }

    public static Results create(DiagramProject project, Text text, Set<DiaTexTraceLink> traceLinks) {
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
        var dPrecision = precision() - expectedResults().precision();
        var dRecall = recall() - expectedResults().recall();
        var dF1 = f1() - expectedResults().f1();
        var dAcc = accuracy() - expectedResults().accuracy();
        var dPhi = phiCoefficient() - expectedResults().phiCoefficient();
        var dSpec = specificity() - expectedResults().specificity();
        return MessageFormat.format("TP:{0}, FP:{1}, TN:{2}, FN:{3}, P:{4}({5}), R:{6}({7}), F1:{8}({9}), Acc:{10}({11}), φ:{12}({13}), Spec:{14}({15})", TP(),
                FP(), TN(), FN(), precision(), dPrecision, recall(), dRecall, f1(), dF1, accuracy(), dAcc, phiCoefficient(), dPhi, specificity(), dSpec);
    }

    public boolean asExpected() {
        if (expectedResults().precision() > precision())
            return false;
        if (expectedResults().recall() > recall())
            return false;
        if (expectedResults().f1() > f1())
            return false;
        if (expectedResults().accuracy() > accuracy())
            return false;
        if (expectedResults().phiCoefficient() > phiCoefficient())
            return false;
        if (expectedResults().specificity() > specificity())
            return false;
        return true;
    }
}
