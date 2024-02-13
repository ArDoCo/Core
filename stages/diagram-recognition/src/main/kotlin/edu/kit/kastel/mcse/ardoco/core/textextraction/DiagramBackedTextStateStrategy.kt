package edu.kit.kastel.mcse.ardoco.core.textextraction

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Box
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Diagram
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramElement
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramUtil
import edu.kit.kastel.mcse.ardoco.core.api.text.Word
import edu.kit.kastel.mcse.ardoco.core.api.textextraction.MappingKind
import edu.kit.kastel.mcse.ardoco.core.api.textextraction.NounMapping
import edu.kit.kastel.mcse.ardoco.core.common.util.CommonTextToolsConfig
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper
import edu.kit.kastel.mcse.ardoco.core.common.util.SimilarityUtils
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.WordSimUtils
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Claimant
import org.eclipse.collections.api.factory.Lists
import org.eclipse.collections.api.factory.SortedSets
import org.eclipse.collections.api.list.ImmutableList
import org.slf4j.LoggerFactory

/**
 * Extends the [OriginalTextStateStrategy] by further dividing similar mappings by their similarity to the available
 * [DiagramElements][DiagramElement]. For example, consider that both a package diagram and the text
 * contain "routeplanner" and "routeplannerui". If a "routeplanner" noun mapping exists and we want to add "routeplannerui", a combined mapping would be created
 * due to similarity. By additionally comparing to the diagram elements, we find that they are related to different package diagram elements, and thus probably
 * shouldn't be contained by the same mapping.
 */
class DiagramBackedTextStateStrategy(
    private val dataRepository: DataRepository
) : OriginalTextStateStrategy(dataRepository.globalConfiguration) {
    private val wordSimUtils: WordSimUtils = globalConfiguration.wordSimUtils
    private val similarityUtils: SimilarityUtils = globalConfiguration.similarityUtils
    private lateinit var boxes: List<Box>

    /**
     * Tries to add a mapping to the state using the existing parameters. Searches for similar mappings using the similarity metrics. Additionally, checks the
     * relationship between mappings and the available [DiagramElements][DiagramElement] to further
     * subdivide them.
     */
    override fun addOrExtendNounMapping(
        word: Word,
        kind: MappingKind,
        claimant: Claimant,
        probability: Double,
        surfaceForms: ImmutableList<String>
    ): NounMapping {
        if (!this::boxes.isInitialized) {
            val diagramRecognitionState = DataRepositoryHelper.getDiagramRecognitionState(dataRepository)
            boxes = diagramRecognitionState.getDiagrams().flatMap { d: Diagram -> d.getBoxes() }
            logger.debug("Loaded {} Boxes", boxes.size)
        }
        val disposableNounMapping =
            NounMappingImpl(
                System.currentTimeMillis(),
                SortedSets.immutable.with(word),
                kind,
                claimant,
                probability,
                Lists.immutable.with(word),
                surfaceForms
            )
        val relatedToWordUnboxed = getMostSimilar(boxes, word)
        for (existingNounMapping in getTextState().nounMappings) {
            if (similarityUtils.areNounMappingsSimilar(disposableNounMapping, existingNounMapping) &&
                isDiagramElementMostSimilar(
                    boxes,
                    relatedToWordUnboxed,
                    existingNounMapping
                )
            ) {
                val mergedNounMapping =
                    DiagramBackedNounMappingImpl(
                        mergeNounMappingsStateless(
                            existingNounMapping,
                            disposableNounMapping,
                            disposableNounMapping.referenceWords,
                            disposableNounMapping.reference,
                            disposableNounMapping.getKind(),
                            claimant,
                            disposableNounMapping.probability
                        ),
                        relatedToWordUnboxed
                    )
                getTextState().removeNounMappingFromState(existingNounMapping, mergedNounMapping)
                getTextState().removeNounMappingFromState(disposableNounMapping, mergedNounMapping)
                getTextState().addNounMappingAddPhraseMapping(mergedNounMapping)
                return mergedNounMapping
            }
        }
        val diagramNM =
            DiagramBackedNounMappingImpl(
                disposableNounMapping,
                getMostSimilar(boxes, disposableNounMapping)
            )
        getTextState().addNounMappingAddPhraseMapping(diagramNM)
        return diagramNM
    }

    /**
     * {@return whether the diagram element is the most similar to the noun mapping}
     *
     * @param diagramElements the diagram elements to search, if the noun mappings relation to the diagram elements is unknown
     * @param candidate       candidate for the most similar diagram element we want to check
     * @param nounMapping     the noun mapping
     */
    private fun isDiagramElementMostSimilar(
        diagramElements: List<Box>,
        candidate: Box?,
        nounMapping: NounMapping
    ): Boolean {
        return if (nounMapping is DiagramBackedNounMappingImpl) {
            candidate == nounMapping.getDiagramElement()
        } else {
            val nounMapDE = getMostSimilar(diagramElements, nounMapping)
            candidate == nounMapDE
        }
    }

    /**
     * {@return the most similar diagram elements to the noun mapping}
     *
     * @param diagramElements the diagram elements to search
     * @param nounMapping     the mapping
     */
    private fun getMostSimilar(
        diagramElements: List<Box>,
        nounMapping: NounMapping
    ): Box? {
        val nounMapPairs =
            diagramElements
                .map { box: Box -> DiagramUtil.calculateHighestSimilarity(wordSimUtils, nounMapping, box) to box }
                .filter { p -> p.first >= CommonTextToolsConfig.DE_NM_SIMILARITY_THRESHOLD }
        return nounMapPairs.maxWithOrNull(diagramElementSimilarity)?.second
    }

    /**
     * {@return the most similar diagram element to the provided word}
     *
     * @param diagramElements the diagram elements to search
     * @param word            the word
     */
    private fun getMostSimilar(
        diagramElements: List<Box>,
        word: Word
    ): Box? {
        val wordPairs =
            diagramElements
                .map { box -> DiagramUtil.calculateHighestSimilarity(wordSimUtils, word, box) to box }
                .filter { p -> p.first >= CommonTextToolsConfig.DE_WORD_SIMILARITY_THRESHOLD }
        return wordPairs.maxWithOrNull(diagramElementSimilarity)?.second
    }

    companion object {
        private val logger = LoggerFactory.getLogger(DiagramBackedTextStateStrategy::class.java)

        /**
         * Used to compare the similarity diagram element pairs. Using [java.util.stream.Stream.max] with this returns the diagram element with
         * the highest similarity and shortest reference length.
         */
        private var diagramElementSimilarity =
            Comparator { p1: Pair<Double, Box>, p2: Pair<Double, Box> ->
                val comp = p1.first.compareTo(p2.first)
                if (comp == 0) {
                    // More "concise" diagram elements are preferable
                    return@Comparator p2.second.references.size.compareTo(p1.second.references.size)
                }
                comp
            }
    }
}
