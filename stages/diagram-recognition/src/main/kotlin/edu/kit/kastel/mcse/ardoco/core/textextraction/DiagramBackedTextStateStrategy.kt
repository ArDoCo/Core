package edu.kit.kastel.mcse.ardoco.core.textextraction

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Box
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Diagram
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramRecognitionState
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramUtil
import edu.kit.kastel.mcse.ardoco.core.api.text.Word
import edu.kit.kastel.mcse.ardoco.core.api.textextraction.MappingKind
import edu.kit.kastel.mcse.ardoco.core.api.textextraction.NounMapping
import edu.kit.kastel.mcse.ardoco.core.common.tuple.Pair
import edu.kit.kastel.mcse.ardoco.core.common.util.CommonTextToolsConfig
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper
import edu.kit.kastel.mcse.ardoco.core.common.util.SimilarityUtils
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.WordSimUtils
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Claimant
import org.eclipse.collections.api.factory.Lists
import org.eclipse.collections.api.factory.SortedSets
import org.eclipse.collections.api.list.ImmutableList
import org.slf4j.LoggerFactory
import java.util.Optional

/**
 * Extends the [OriginalTextStateStrategy] by further dividing similar mappings by their similarity to the available
 * [DiagramElements][edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramElement]. For example, consider that both a package diagram and the text
 * contain "routeplanner" and "routeplannerui". If a "routeplanner" noun mapping exists and we want to add "routeplannerui", a combined mapping would be created
 * due to similarity. By additionally comparing to the diagram elements, we find that they are related to different package diagram elements, and thus probably
 * shouldn't be contained by the same mapping.
 */
class DiagramBackedTextStateStrategy(textState: TextStateImpl) : OriginalTextStateStrategy(textState) {
    private val wordSimUtils: WordSimUtils
    private val similarityUtils: SimilarityUtils
    private var diagramRecognitionState: DiagramRecognitionState? = null
    private lateinit var boxes: List<Box>

    /**
     * Tries to add a mapping to the state using the existing parameters. Searches for similar mappings using the similarity metrics. Additionally, checks the
     * relationship between mappings and the available [DiagramElements][edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramElement] to further
     * subdivide them.
     */
    override fun addOrExtendNounMapping(
        word: Word,
        kind: MappingKind,
        claimant: Claimant,
        probability: Double,
        surfaceForms: ImmutableList<String>
    ): NounMapping {
        if (diagramRecognitionState == null) {
            diagramRecognitionState = DataRepositoryHelper.getDiagramRecognitionState(textState.dataRepository)
            logger.debug("Loaded DiagramRecognitionState")
        }
        if (!this::boxes.isInitialized) {
            boxes = diagramRecognitionState!!.getDiagrams().flatMap { d: Diagram -> d.getBoxes() }
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
        val relatedToWordUnboxed = getMostSimilar(boxes, word).orElse(null)
        for (existingNounMapping in getTextState().nounMappings) {
            if (similarityUtils.areNounMappingsSimilar(disposableNounMapping, existingNounMapping) &&
                isDiagramElementMostSimilar(
                    boxes, relatedToWordUnboxed,
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
                        relatedToWordUnboxed!!
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
                getMostSimilar(boxes, disposableNounMapping).orElse(null)!!
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
    protected fun isDiagramElementMostSimilar(
        diagramElements: List<Box>?,
        candidate: Box?,
        nounMapping: NounMapping
    ): Boolean {
        val relatedToWord = Optional.ofNullable(candidate)
        return if (nounMapping is DiagramBackedNounMappingImpl) {
            relatedToWord == nounMapping.getDiagramElement()
        } else {
            val nounMapDE = getMostSimilar(diagramElements, nounMapping)
            relatedToWord == nounMapDE
        }
    }

    /**
     * {@return the most similar diagram elements to the noun mapping}
     *
     * @param diagramElements the diagram elements to search
     * @param nounMapping     the mapping
     */
    protected fun getMostSimilar(
        diagramElements: List<Box>?,
        nounMapping: NounMapping?
    ): Optional<Box> {
        val nounMapPairs =
            diagramElements!!
                .map { box: Box ->
                    Pair(
                        DiagramUtil.calculateHighestSimilarity(wordSimUtils, nounMapping, box),
                        box
                    )
                }
                .filter { p: Pair<Double, Box> -> p.first >= CommonTextToolsConfig.DE_NM_SIMILARITY_THRESHOLD }
        return Optional.ofNullable(nounMapPairs.maxWithOrNull(diagramElementSimilarity)?.second)
    }

    /**
     * {@return the most similar diagram element to the provided word}
     *
     * @param diagramElements the diagram elements to search
     * @param word            the word
     */
    protected fun getMostSimilar(
        diagramElements: List<Box>?,
        word: Word?
    ): Optional<Box> {
        val wordPairs =
            diagramElements!!.stream()
                .map { box: Box ->
                    Pair(
                        DiagramUtil.calculateHighestSimilarity(wordSimUtils, word, box),
                        box
                    )
                }
                .filter { p: Pair<Double, Box> -> p.first >= CommonTextToolsConfig.DE_WORD_SIMILARITY_THRESHOLD }
                .toList()
        return Optional.ofNullable(wordPairs.maxWithOrNull(diagramElementSimilarity)?.second)
    }

    init {
        val pipelineMetaData = textState.metaData
        wordSimUtils = pipelineMetaData.wordSimUtils
        similarityUtils = pipelineMetaData.similarityUtils
    }

    companion object {
        private val logger = LoggerFactory.getLogger(DiagramBackedTextStateStrategy::class.java)

        /**
         * Used to compare the similarity diagram element pairs. Using [java.util.stream.Stream.max] with this returns the diagram element with
         * the highest similarity and shortest reference length.
         */
        private var diagramElementSimilarity =
            Comparator { p1: Pair<Double, Box>, p2: Pair<Double, Box> ->
                val comp = java.lang.Double.compare(p1.first, p2.first)
                if (comp == 0) {
                    // More "concise" diagram elements are preferable
                    return@Comparator p2.second.references.size.compareTo(p1.second.references.size)
                }
                comp
            }
    }
}
