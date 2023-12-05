/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.erid.diagramrecognition.informants;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.impl.factory.Lists;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Box;
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.TextBox;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelInstance;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelStates;
import edu.kit.kastel.mcse.ardoco.core.common.tuple.Pair;
import edu.kit.kastel.mcse.ardoco.core.common.tuple.Triple;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.common.util.DbPediaHelper;
import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;

/**
 * Sets the references of {@link edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramElement DiagramElements}. A reference is supposed to represent
 * the elements as best as possible with as little unnecessary information as possible. Multiples reference can be calculated for a single element. The
 * relationship between diagram elements and model elements is used to further reduce the set of references.
 */
public class DiagramModelReferenceInformant extends Informant {
    private final static Logger logger = LoggerFactory.getLogger(DiagramModelReferenceInformant.class);

    @Configurable
    private double textBoxSimilarityThreshold = 0.5;

    /**
     * Creates a new informant that acts on the specified data repository
     *
     * @param dataRepository the data repository
     */
    public DiagramModelReferenceInformant(DataRepository dataRepository) {
        super(DiagramModelReferenceInformant.class.getSimpleName(), dataRepository);
    }

    /**
     * Iterates over all diagram elements and sets their reference.
     */
    @Override
    public void process() {
        var optModelStates = dataRepository.getData(ModelStates.ID, ModelStates.class);
        if (optModelStates.isEmpty()) {
            logger.warn(String.format("%s couldn't be found, skipping informant", ModelStates.class.getSimpleName()));
            return;
        }

        var boxes = Lists.mutable.fromStream(DataRepositoryHelper.getDiagramRecognitionState(dataRepository)
                .getDiagrams()
                .stream()
                .flatMap(d -> d.getBoxes().stream()));
        boxes.forEach(box -> setReferences(box, optModelStates.orElseThrow()));
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
    private void setReferences(Box box, ModelStates modelStates) {
        var modelIds = modelStates.modelIds();

        for (var model : modelIds) {
            var modelState = modelStates.getModelExtractionState(model);
            var instances = modelState.getInstances();
            box.setReferences(List.of());
            var references = getReferencesPerTextBox(box);
            var similar = similarModelInstance(instances, references);
            similar.forEach(s -> logger.debug(box + " similar to " + s));
            var isEmpty = similar.isEmpty();
            for (var ref : references.entrySet()) {
                if (isEmpty || similar.stream().anyMatch(t -> t.first().equals(ref.getKey()))) {
                    ref.getValue().forEach(box::addReference);
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
    private List<Triple<TextBox, Double, ModelInstance>> similarModelInstance(ImmutableList<ModelInstance> modelInstances,
            Map<TextBox, Set<String>> references) {
        var list = Lists.mutable.<Triple<TextBox, Double, ModelInstance>>of();
        for (var entry : references.entrySet()) {
            var textBox = entry.getKey();
            var textBoxRefs = entry.getValue();
            var optPair = getMostSimilarModelInstance(modelInstances, textBox, textBoxRefs);
            if (optPair.isPresent()) {
                var pair = optPair.orElseThrow();
                list.add(new Triple<>(textBox, pair.first(), pair.second()));
            }
        }
        return list;
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
    private Optional<Pair<Double, ModelInstance>> getMostSimilarModelInstance(ImmutableList<ModelInstance> modelInstances, TextBox textBox,
            Set<String> references) {
        var max = Double.MIN_VALUE;
        var wordSimUtils = getMetaData().getWordSimUtils();
        ModelInstance mostSimilarModelInstance = null;
        for (var instance : modelInstances) {
            if (wordSimUtils.areWordsSimilar(textBox.getText(), instance.getFullName()) || references.stream()
                    .anyMatch(ref -> wordSimUtils.areWordsSimilar(ref.toLowerCase(Locale.ENGLISH), instance.getFullName().toLowerCase(Locale.ENGLISH)))) {
                var similarity = wordSimUtils.getSimilarity(textBox.getText().toLowerCase(Locale.ENGLISH), instance.getFullName().toLowerCase(Locale.ENGLISH));
                if (similarity > textBoxSimilarityThreshold && similarity > max) {
                    max = similarity;
                    mostSimilarModelInstance = instance;
                }
            }
        }
        if (max > Double.MIN_VALUE) {
            return Optional.of(new Pair<>(max, mostSimilarModelInstance));
        }
        return Optional.empty();
    }

    /**
     * {@return a map of references contained by the specified box} If a reference contains uppercase letters, its references take precedence over entirely
     * lowercase references.
     *
     * @param box the box
     */
    private Map<TextBox, Set<String>> getReferencesPerTextBox(Box box) {
        var map = new LinkedHashMap<TextBox, Set<String>>();
        var texts = box.getTexts();
        for (TextBox textBox : texts) {
            map.put(textBox, getReferences(textBox));
        }

        var atleastOneUpperCaseCharacterInTBox = map.entrySet()
                .stream()
                .filter(e -> e.getValue().stream().anyMatch(s -> !s.equals(s.toLowerCase(Locale.ENGLISH))))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        if (!atleastOneUpperCaseCharacterInTBox.isEmpty())
            return atleastOneUpperCaseCharacterInTBox;

        return map;
    }

    /**
     * Determines a set of possible references for a textBox. Tries to filter out technical terms using {@link DbPediaHelper}.
     *
     * @param textBox the textBox
     * @return a set of possible names
     */
    private static @NotNull Set<String> getReferences(@NotNull TextBox textBox) {
        var names = new LinkedHashSet<String>();

        var text = textBox.getText();
        if (!FILTER.test(text))
            return names;

        var splitAndDecameled = processText(text).stream().filter(FILTER).toList();

        var noBlank = splitAndDecameled.stream().map(s -> s.replaceAll("\\s+", "")).filter(FILTER).toList();
        names.addAll(splitAndDecameled);
        names.addAll(noBlank);

        var atleastOneUpperCaseChar = names.stream().filter(s -> !s.equals(s.toLowerCase(Locale.ENGLISH))).collect(Collectors.toSet());

        if (!atleastOneUpperCaseChar.isEmpty())
            return atleastOneUpperCaseChar;

        return names;
    }

    private static final Predicate<String> FILTER = s -> !DbPediaHelper.isWordMarkupLanguage(s) && !DbPediaHelper.isWordProgrammingLanguage(s) && !DbPediaHelper
            .isWordSoftware(s);

    /**
     * {@return a set of alternative texts extracted from the input text}. The text is processed with {@link #splitBracketsAndEnumerations(String)} and
     * {@link #getDeCameledText(String)}.
     *
     * @param text the text
     */
    private static @NotNull Set<String> processText(@NotNull String text) {
        var words = new LinkedHashSet<String>();
        var split = splitBracketsAndEnumerations(text);
        var deCameledSplit = split.stream().map(DiagramModelReferenceInformant::getDeCameledText).toList();
        words.addAll(split);
        words.addAll(deCameledSplit);
        words.remove("");
        return words;
    }

    /**
     * Splits the string around brackets and commas. The results are trimmed. <span style=" white-space: nowrap;">Example: "Lorem (ipsum), Dolor, sit (Amet)" ->
     * {"Lorem","ipsum","Dolor","sit","Amet"}</span>
     *
     * @param text the text
     * @return a non-empty list of splits
     */
    private static @NotNull List<String> splitBracketsAndEnumerations(@NotNull String text) {
        return Arrays.stream(text.split("[,()]")).map(String::trim).toList();
    }

    /**
     * Decamels the word and returns it as words joined by space. <span style=" white-space: nowrap;">Example: "CamelCaseExample" -> "Camel Case Example",
     * "example" -> "example", etc.</span>
     *
     * @param word the word that should be decameled
     * @return the decameled word
     */
    private static @NotNull String getDeCameledText(@NotNull String word) {
        return String.join(" ", word.split("(?<!([A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])")).replaceAll("\\s+", " ");
    }
}
