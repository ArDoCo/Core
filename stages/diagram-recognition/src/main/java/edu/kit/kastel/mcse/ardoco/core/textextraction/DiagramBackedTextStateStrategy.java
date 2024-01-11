/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.textextraction;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.factory.SortedSets;
import org.eclipse.collections.api.list.ImmutableList;
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
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.WordSimUtils;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Claimant;

/**
 * Extends the {@link OriginalTextStateStrategy} by further dividing similar mappings by their similarity to the available
 * {@link edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramElement DiagramElements}. For example, consider that both a package diagram and the text
 * contain "routeplanner" and "routeplannerui". If a "routeplanner" noun mapping exists and we want to add "routeplannerui", a combined mapping would be created
 * due to similarity. By additionally comparing to the diagram elements, we find that they are related to different package diagram elements, and thus probably
 * shouldn't be contained by the same mapping.
 */
public class DiagramBackedTextStateStrategy extends OriginalTextStateStrategy {
    private static final Logger logger = LoggerFactory.getLogger(DiagramBackedTextStateStrategy.class);
    private final WordSimUtils wordSimUtils;
    private final SimilarityUtils similarityUtils;
    private DiagramRecognitionState diagramRecognitionState;
    private List<Box> boxes;

    /**
     * Sole constructor.
     *
     * @param textState the text state this strategy works on
     */
    public DiagramBackedTextStateStrategy(TextStateImpl textState) {
        super(textState);
        var pipelineMetaData = textState.getMetaData();
        this.wordSimUtils = pipelineMetaData.getWordSimUtils();
        this.similarityUtils = pipelineMetaData.getSimilarityUtils();
    }

    /**
     * Tries to add a mapping to the state using the existing parameters. Searches for similar mappings using the similarity metrics. Additionally, checks the
     * relationship between mappings and the available {@link edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramElement DiagramElements} to further
     * subdivide them.
     */

    @Override
    public NounMapping addOrExtendNounMapping(Word word, MappingKind kind, Claimant claimant, double probability, ImmutableList<String> surfaceForms) {
        if (diagramRecognitionState == null) {
            diagramRecognitionState = DataRepositoryHelper.getDiagramRecognitionState(textState.getDataRepository());
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
            if (similarityUtils.areNounMappingsSimilar(disposableNounMapping, existingNounMapping) && isDiagramElementMostSimilar(boxes, relatedToWordUnboxed,
                    existingNounMapping)) {

                var mergedNounMapping = new DiagramBackedNounMappingImpl(mergeNounMappingsStateless(existingNounMapping, disposableNounMapping,
                        disposableNounMapping.getReferenceWords(), disposableNounMapping.getReference(), disposableNounMapping.getKind(), claimant,
                        disposableNounMapping.getProbability()), relatedToWordUnboxed);

                this.getTextState().removeNounMappingFromState(existingNounMapping, mergedNounMapping);
                this.getTextState().removeNounMappingFromState(disposableNounMapping, mergedNounMapping);
                this.getTextState().addNounMappingAddPhraseMapping(mergedNounMapping);

                return mergedNounMapping;
            }
        }

        var diagramNM = new DiagramBackedNounMappingImpl(disposableNounMapping, getMostSimilar(boxes, disposableNounMapping).orElse(null));

        getTextState().addNounMappingAddPhraseMapping(diagramNM);

        return diagramNM;
    }

    /**
     * {@return whether the diagram element is the most similar to the noun mapping}
     *
     * @param diagramElements the diagram elements to search, if the noun mappings relation to the diagram elements is unknown
     * @param candidate       candidate for the most similar diagram element we want to check
     * @param nounMapping     the noun mapping
     */
    protected boolean isDiagramElementMostSimilar(List<Box> diagramElements, Box candidate, NounMapping nounMapping) {
        var relatedToWord = Optional.ofNullable(candidate);

        if (nounMapping instanceof DiagramBackedNounMappingImpl diagramBackedNounMapping) {
            return Objects.equals(relatedToWord, diagramBackedNounMapping.getDiagramElement());
        } else {
            var nounMapDE = getMostSimilar(diagramElements, nounMapping);
            return Objects.equals(relatedToWord, nounMapDE);
        }
    }

    /**
     * {@return the most similar diagram elements to the noun mapping}
     *
     * @param diagramElements the diagram elements to search
     * @param nounMapping     the mapping
     */
    protected Optional<Box> getMostSimilar(List<Box> diagramElements, NounMapping nounMapping) {
        var nounMapPairs = diagramElements.stream()
                .map(box -> new Pair<>(DiagramUtil.calculateHighestSimilarity(wordSimUtils, nounMapping, box), box))
                .filter(p -> p.first() >= CommonTextToolsConfig.DE_NM_SIMILARITY_THRESHOLD)
                .toList();
        return nounMapPairs.stream().max(diagramElementSimilarity).map(Pair::second);
    }

    /**
     * {@return the most similar diagram element to the provided word}
     *
     * @param diagramElements the diagram elements to search
     * @param word            the word
     */
    protected Optional<Box> getMostSimilar(List<Box> diagramElements, Word word) {
        var wordPairs = diagramElements.stream()
                .map(box -> new Pair<>(DiagramUtil.calculateHighestSimilarity(wordSimUtils, word, box), box))
                .filter(p -> p.first() >= CommonTextToolsConfig.DE_WORD_SIMILARITY_THRESHOLD)
                .toList();
        return wordPairs.stream().max(diagramElementSimilarity).map(Pair::second);
    }

    /**
     * Used to compare the similarity diagram element pairs. Using {@link java.util.stream.Stream#max(Comparator)} with this returns the diagram element with
     * the highest similarity and shortest reference length.
     */
    protected static Comparator<Pair<Double, Box>> diagramElementSimilarity = (p1, p2) -> {
        var comp = Double.compare(p1.first(), p2.first());
        if (comp == 0) {
            //More "concise" diagram elements are preferable
            return Integer.compare(p2.second().getReferences().size(), p1.second().getReferences().size());
        }
        return comp;
    };
}
