package edu.kit.kastel.mcse.ardoco.erid.diagramrecognition.informants;

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.BoundingBox;
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.TextBox;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;
import java.util.ArrayList;
import java.util.Comparator;
import org.eclipse.collections.impl.factory.Lists;

public class DiagramTextBoxCleanUpInformant extends Informant {
    public DiagramTextBoxCleanUpInformant(DataRepository dataRepository) {
        super(DiagramTextBoxCleanUpInformant.class.getSimpleName(), dataRepository);
    }

    @Override
    protected void process() {
        var diagrams = DataRepositoryHelper.getDiagramRecognitionState(dataRepository).getDiagrams();
        for (var diagram : diagrams) {
            var boxes = diagram.getBoxes();
            for (var box : boxes) {
                var texts = box.getTexts();
                /*var list2D = new ArrayList<ArrayList<TextBox>>();
                list2D =
                        new ArrayList<>(texts.stream().map(t1 -> new ArrayList<>(texts.stream().filter(t -> t.getBoundingBox().horizontal(t1
                        .getBoundingBox())).toList())).toList());
                list2D.removeIf(ArrayList::isEmpty);
                for (var list : list2D) {
                    list.sort(orderLeftToRight);
                }
                list2D.sort(orderTopToBottom);
                for (var list : list2D) {
                    list.stream().collect(Collectors.groupingBy());
                }*/
                var newTexts = new ArrayList<TextBox>();
                var map = Lists.mutable.withAll(texts).groupBy(TextBox::getDominatingColor);
                map.forEachKeyMutableList((c, list) -> {
                    var sorted = Lists.mutable.withAll(list);
                    sorted.sort(order);
                    var combinedText = sorted.stream().map(TextBox::getText).reduce((a, b) -> a + " " + b).orElse(null);
                    var combinedBB = sorted.stream().map(TextBox::getBoundingBox).reduce(BoundingBox::combine).orElse(null);
                    var confidence = sorted.stream().map(TextBox::getConfidence).max(Double::compare).orElse(null);
                    if (combinedText != null && combinedBB != null && confidence != null) {
                        newTexts.add(new TextBox(combinedBB, confidence, combinedText));
                    }
                });
                texts.forEach(box::removeTextBox);
                newTexts.forEach(box::addTextBox);
            }
        }
    }

    private transient final Comparator<TextBox> order = (a, b) -> {
        var comp = Integer.compare(a.getYCoordinate(), b.getYCoordinate());
        if (comp == 0) {
            return Integer.compare(a.getXCoordinate(), b.getXCoordinate());
        }
        return comp;
    };

    private transient final Comparator<TextBox> orderLeftToRight = Comparator.comparingInt(a -> a.getBoundingBox().minX());

    private transient final Comparator<ArrayList<TextBox>> orderTopToBottom = (a, b) -> {
        var first = a.get(0);
        var second = b.get(0);
        return Integer.compare(first.getYCoordinate(), second.getYCoordinate());
    };
}
