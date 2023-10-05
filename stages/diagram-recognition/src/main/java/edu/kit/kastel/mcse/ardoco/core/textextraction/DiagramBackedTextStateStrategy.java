package edu.kit.kastel.mcse.ardoco.core.textextraction;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.SortedSets;
import org.eclipse.collections.api.list.ImmutableList;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Box;
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramRecognitionState;
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramUtil;
import edu.kit.kastel.mcse.ardoco.core.api.text.Word;
import edu.kit.kastel.mcse.ardoco.core.api.textextraction.MappingKind;
import edu.kit.kastel.mcse.ardoco.core.api.textextraction.NounMapping;
import edu.kit.kastel.mcse.ardoco.core.common.tuple.Pair;
import edu.kit.kastel.mcse.ardoco.core.common.util.CommonTextToolsConfig;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.common.util.SimilarityUtils;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Claimant;

public class DiagramBackedTextStateStrategy extends OriginalTextStateStrategy {
    private static final Logger logger = LoggerFactory.getLogger(DiagramBackedTextStateStrategy.class);

    private final DataRepository dataRepository;
    private DiagramRecognitionState diagramRecognitionState;
    private List<Box> boxes;

    public DiagramBackedTextStateStrategy(TextStateImpl textState, DataRepository dataRepository) {
        super(textState);
        this.dataRepository = dataRepository;
    }

    @NotNull
    @Override
    public NounMapping addOrExtendNounMapping(Word word, MappingKind kind, Claimant claimant, double probability, ImmutableList<String> surfaceForms) {
        if (diagramRecognitionState == null) {
            diagramRecognitionState = DataRepositoryHelper.getDiagramRecognitionState(dataRepository);
            logger.debug("Loaded DiagramRecognitionState");
        }
        if (boxes == null) {
            boxes = diagramRecognitionState.getDiagrams().stream().flatMap(d -> d.getBoxes().stream()).toList();
            logger.debug("Loaded {} Boxes", boxes.size());
        }

        var disposableNounMapping = new NounMappingImpl(System.currentTimeMillis(), SortedSets.immutable.with(word), kind, claimant, probability,
                Lists.immutable.with(word), surfaceForms);

        var relatedToWordUnboxed = getMostSimilar(boxes, word).orElse(null);
        for (var existingNounMapping : getTextState().getNounMappings()) {
            if (SimilarityUtils.areNounMappingsSimilar(disposableNounMapping, existingNounMapping) && isRelationToDiagramElementsSimilar(boxes,
                    relatedToWordUnboxed, existingNounMapping)) {
                return mergeNounMappings(existingNounMapping, disposableNounMapping, disposableNounMapping.getReferenceWords(),
                        disposableNounMapping.getReference(), disposableNounMapping.getKind(), claimant, disposableNounMapping.getProbability(),
                        relatedToWordUnboxed);
            }
        }

        var diagramNM = new DiagramBackedNounMappingImpl(disposableNounMapping, getMostSimilar(boxes, disposableNounMapping).orElse(null));

        getTextState().addNounMappingAddPhraseMapping(diagramNM);

        return diagramNM;
    }

    public NounMappingImpl mergeNounMappings(NounMapping firstNounMapping, NounMapping secondNounMapping, ImmutableList<Word> referenceWords, String reference,
            MappingKind mappingKind, Claimant claimant, double probability, Box box) {
        var mergedNounMapping = new DiagramBackedNounMappingImpl(
                mergeNounMappingsStateless(firstNounMapping, secondNounMapping, referenceWords, reference, mappingKind, claimant, probability), box);

        this.getTextState().removeNounMappingFromState(firstNounMapping, mergedNounMapping);
        this.getTextState().removeNounMappingFromState(secondNounMapping, mergedNounMapping);
        this.getTextState().addNounMappingAddPhraseMapping(mergedNounMapping);

        return mergedNounMapping;
    }

    protected boolean isRelationToDiagramElementsSimilar(List<Box> diagramElements, Box relatedToWordUnboxed, NounMapping nounMapping) {
        var relatedToWord = Optional.ofNullable(relatedToWordUnboxed);

        if (nounMapping instanceof DiagramBackedNounMappingImpl diagramBackedNounMapping) {
            return Objects.equals(relatedToWord, diagramBackedNounMapping.getDiagramElement());
        } else {
            var nounMapDE = getMostSimilar(diagramElements, nounMapping);
            return Objects.equals(relatedToWord, nounMapDE);
        }
    }

    protected Optional<Box> getMostSimilar(List<Box> diagramElements, NounMapping nounMapping) {
        var nounMapPairs = diagramElements.stream()
                .map(box -> new Pair<>(DiagramUtil.calculateHighestSimilarity(nounMapping, box), box))
                .filter(p -> p.first() >= CommonTextToolsConfig.DE_NM_SIMILARITY_THRESHOLD)
                .toList();
        return nounMapPairs.stream().max(diagramElementSimilarity).map(Pair::second);
    }

    protected Optional<Box> getMostSimilar(List<Box> diagramElements, Word word) {
        var wordPairs = diagramElements.stream()
                .map(box -> new Pair<>(DiagramUtil.calculateHighestSimilarity(word, box), box))
                .filter(p -> p.first() >= CommonTextToolsConfig.DE_Word_SIMILARITY_THRESHOLD)
                .toList();
        return wordPairs.stream().max(diagramElementSimilarity).map(Pair::second);
    }

    protected static Comparator<Pair<Double, Box>> diagramElementSimilarity = (p1, p2) -> {
        var comp = Double.compare(p1.first(), p2.first());
        if (comp == 0) {
            //More "concise" diagram elements are preferable
            return Integer.compare(p2.second().getReferences().size(), p1.second().getReferences().size());
        }
        return comp;
    };
}
