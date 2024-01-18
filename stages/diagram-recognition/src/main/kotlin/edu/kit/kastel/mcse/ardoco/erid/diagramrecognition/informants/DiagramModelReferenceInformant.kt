package edu.kit.kastel.mcse.ardoco.erid.diagramrecognition.informants

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Box
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Diagram
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.TextBox
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelInstance
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelStates
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper
import edu.kit.kastel.mcse.ardoco.core.common.util.DbPediaHelper
import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant
import org.eclipse.collections.api.block.procedure.Procedure
import org.eclipse.collections.api.list.ImmutableList
import org.eclipse.collections.impl.factory.Lists
import kotlin.collections.LinkedHashMap
import kotlin.collections.LinkedHashSet

/**
 * Sets the references of [DiagramElements][edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramElement]. A reference is supposed to represent
 * the elements as best as possible with as little unnecessary information as possible. Multiples reference can be calculated for a single element. The
 * relationship between diagram elements and model elements is used to further reduce the set of references.
 */
class DiagramModelReferenceInformant(dataRepository: DataRepository?) : Informant(DiagramModelReferenceInformant::class.java.getSimpleName(), dataRepository) {
    @Configurable
    private var textBoxSimilarityThreshold = 0.5

    /**
     * Iterates over all diagram elements and sets their reference.
     */
    public override fun process() {
        val optModelStates = dataRepository.getData(ModelStates.ID, ModelStates::class.java)
        if (optModelStates.isEmpty) {
            val txt = String.format("%s couldn't be found, skipping informant", ModelStates::class.java.getSimpleName())
            logger.warn(txt)
            return
        }
        val boxes: MutableList<Box> =
            Lists.mutable.fromStream(
                DataRepositoryHelper.getDiagramRecognitionState(dataRepository)
                    .getDiagrams()
                    .stream()
                    .flatMap { d: Diagram ->
                        d.getBoxes().stream()
                    }
            )
        boxes.forEach(
            Procedure { box: Box ->
                setReferences(
                    box,
                    optModelStates.orElseThrow()
                )
            }
        )
    }

    /**
     * Sets the references of each box. Individually calculates references for each text box first. Subsequently, calculates the most similar model instances
     * for each text box and their references. If a text box is similar to a model instance, we assume that the diagram element may be the informal
     * representation of the model instance. Thus, we remove all references except the references associated with the text box that is similar to the model
     * instances.
     *
     * @param box         the box
     * @param modelStates the model states
     */
    private fun setReferences(
        box: Box,
        modelStates: ModelStates
    ) {
        val modelIds = modelStates.modelIds()
        for (model in modelIds) {
            val modelState = modelStates.getModelExtractionState(model)
            val instances = modelState.getInstances()
            box.setReferences(listOf())
            val references = getReferencesPerTextBox(box)
            val similar = similarModelInstance(instances, references)
            similar.forEach { s -> logger.debug("{} similar to {}", box, s) }

            val isEmpty = similar.isEmpty()
            for ((key, value) in references) {
                if (isEmpty || similar.any { t: Triple<TextBox, Double, ModelInstance?> -> t.first == key }) {
                    value.forEach { reference -> box.addReference(reference) }
                }
            }
        }
    }

    /**
     * Tries to find the most similar model instance to all text boxes. If it exists, a triple is added to the list with the text box, the model instance and
     * the similarity between them.
     *
     * @param modelInstances the model instances to search in
     * @param references     the reference map
     * @return the list of triples, empty if no model instance is similar to any text box
     */
    private fun similarModelInstance(
        modelInstances: ImmutableList<ModelInstance>,
        references: Map<TextBox, Set<String>>
    ): List<Triple<TextBox, Double, ModelInstance?>> {
        val list: MutableList<Triple<TextBox, Double, ModelInstance?>> = Lists.mutable.of()
        for ((textBox, textBoxRefs) in references) {
            val pair = getMostSimilarModelInstance(modelInstances, textBox, textBoxRefs)
            if (pair != null) {
                list.add(Triple(textBox, pair.first, pair.second))
            }
        }
        return list
    }

    /**
     * Tries to find the most similar model instance to a particular text box. Compares both the entire text of the text box and the references to the instances
     * full name. The most similar instance and its similarity value are encapsulated in a pair tuple.
     *
     * @param modelInstances the model instances
     * @param textBox        the text box
     * @param references     the references associated with the text box
     * @return the pair of model instance and similarity or an empty optional if none exists.
     */
    private fun getMostSimilarModelInstance(
        modelInstances: ImmutableList<ModelInstance>,
        textBox: TextBox,
        references: Set<String>
    ): Pair<Double, ModelInstance?>? {
        var max = Double.MIN_VALUE
        val wordSimUtils = metaData.wordSimUtils
        var mostSimilarModelInstance: ModelInstance? = null
        for (instance in modelInstances) {
            if (wordSimUtils.areWordsSimilar(textBox.text, instance.getFullName()) ||
                references.stream()
                    .anyMatch { ref: String ->
                        wordSimUtils.areWordsSimilar(
                            ref.lowercase(),
                            instance.getFullName().lowercase()
                        )
                    }
            ) {
                val similarity = wordSimUtils.getSimilarity(textBox.text.lowercase(), instance.getFullName().lowercase())
                if (similarity > textBoxSimilarityThreshold && similarity > max) {
                    max = similarity
                    mostSimilarModelInstance = instance
                }
            }
        }
        return if (max > Double.MIN_VALUE) {
            max to mostSimilarModelInstance
        } else {
            null
        }
    }

    /**
     * {@return a map of references contained by the specified box} If a reference contains uppercase letters, its references take precedence over entirely
     * lowercase references.
     *
     * @param box the box
     */
    private fun getReferencesPerTextBox(box: Box): Map<TextBox, Set<String>> {
        val map = LinkedHashMap<TextBox, Set<String>>()
        val texts = box.texts
        for (textBox in texts) {
            map[textBox] = getReferences(textBox)
        }
        val atleastOneUpperCaseCharacterInTBox =
            map.entries
                .filter { (_, value): Map.Entry<TextBox, Set<String>> ->
                    value.stream().anyMatch { s: String -> s != s.lowercase() }
                }.associate { it.key to it.value }
        return if (atleastOneUpperCaseCharacterInTBox.isNotEmpty()) atleastOneUpperCaseCharacterInTBox else map
    }

    companion object {
        /**
         * Determines a set of possible references for a textBox. Tries to filter out technical terms using [DbPediaHelper].
         *
         * @param textBox the textBox
         * @return a set of possible names
         */
        private fun getReferences(textBox: TextBox): Set<String> {
            val names = LinkedHashSet<String>()
            val text = textBox.text
            if (!FILTER(text)) return names
            val splitAndDecameled = processText(text).stream().filter(FILTER).toList()
            val noBlank = splitAndDecameled.stream().map { s: String -> s.replace("\\s+".toRegex(), "") }.filter(FILTER).toList()
            names.addAll(splitAndDecameled)
            names.addAll(noBlank)
            val atleastOneUpperCaseChar = names.filter { s: String -> s != s.lowercase() }.toSet()
            return atleastOneUpperCaseChar.ifEmpty { names }
        }

        private val FILTER =
            { s: String? ->
                !DbPediaHelper.isWordMarkupLanguage(s) && !DbPediaHelper.isWordProgrammingLanguage(s) &&
                    !DbPediaHelper
                        .isWordSoftware(s)
            }

        /**
         * {@return a set of alternative texts extracted from the input text}. The text is processed with [.splitBracketsAndEnumerations] and
         * [.getDeCameledText].
         *
         * @param text the text
         */
        private fun processText(text: String): Set<String> {
            val words = LinkedHashSet<String>()
            val split = splitBracketsAndEnumerations(text)
            val deCameledSplit =
                split.stream().map { word: String ->
                    getDeCameledText(
                        word
                    )
                }.toList()
            words.addAll(split)
            words.addAll(deCameledSplit)
            words.remove("")
            return words
        }

        /**
         * Splits the string around brackets and commas. The results are trimmed. <span style=" white-space: nowrap;">Example: "Lorem (ipsum), Dolor, sit (Amet)" ->
         * {"Lorem","ipsum","Dolor","sit","Amet"}</span>
         *
         * @param text the text
         * @return a non-empty list of splits
         */
        private fun splitBracketsAndEnumerations(text: String): List<String> {
            return text.split("[,()]".toRegex()).dropLastWhile { it.isEmpty() }
                .map { obj: String -> obj.trim { it <= ' ' } }.toList()
        }

        /**
         * Decamels the word and returns it as words joined by space. <span style=" white-space: nowrap;">Example: "CamelCaseExample" -> "Camel Case Example",
         * "example" -> "example", etc.</span>
         *
         * @param word the word that should be decameled
         * @return the decameled word
         */
        private fun getDeCameledText(word: String): String {
            return (word.split("(?<!([A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])".toRegex()).dropLastWhile { it.isEmpty() }).joinToString(" ")
                .replace("\\s+".toRegex(), " ")
        }
    }
}
