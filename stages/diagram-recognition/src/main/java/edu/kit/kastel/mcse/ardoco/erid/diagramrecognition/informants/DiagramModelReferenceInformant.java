package edu.kit.kastel.mcse.ardoco.erid.diagramrecognition.informants;

import edu.kit.kastel.mcse.ardoco.core.common.util.DbPediaHelper;

import java.util.*;
import java.util.stream.Collectors;

import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.factory.Lists;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Box;
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramUtil;
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.TextBox;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelInstance;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelStates;
import edu.kit.kastel.mcse.ardoco.core.common.tuple.Pair;
import edu.kit.kastel.mcse.ardoco.core.common.tuple.Triple;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.WordSimUtils;
import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;

public class DiagramModelReferenceInformant extends Informant {
    private final static Logger logger = LoggerFactory.getLogger(DiagramModelReferenceInformant.class);

    @Configurable
    private double textBoxSimilarityThreshold = 0.5;

    public DiagramModelReferenceInformant(DataRepository dataRepository) {
        super(DiagramModelReferenceInformant.class.getSimpleName(), dataRepository);
    }

    @Override
    public void process() {
        var optModelStates = dataRepository.getData(ModelStates.ID, ModelStates.class);
        if (optModelStates.isEmpty()) {
            logger.warn(String.format("%s couldn't be found, skipping informant", ModelStates.class.getSimpleName()));
            return;
        }

        var boxes = Lists.mutable.fromStream(
                DataRepositoryHelper.getDiagramRecognitionState(dataRepository).getDiagrams().stream().flatMap(d -> d.getBoxes().stream()));
        setReferences(boxes, optModelStates.orElseThrow());
    }

    private void setReferences(MutableList<Box> boxes, ModelStates modelStates) {
        var modelIds = modelStates.extractionModelIds();

        for (var model : modelIds) {
            var modelState = modelStates.getModelExtractionState(model);
            var instances = modelState.getInstances();
            for (var box : boxes) {
                box.setReferences(Set.of());
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
    }

    private MutableList<Triple<TextBox, Double, ModelInstance>> similarModelInstance(ImmutableList<ModelInstance> modelInstances,
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

    private Optional<Pair<Double, ModelInstance>> getMostSimilarModelInstance(ImmutableList<ModelInstance> modelInstances, TextBox textBox,
            Set<String> references) {
        var max = Double.MIN_VALUE;
        ModelInstance mostSimilarModelInstance = null;
        for (var instance : modelInstances) {
            if (WordSimUtils.areWordsSimilar(textBox.getText(), instance.getFullName()) || references.stream()
                    .anyMatch(ref -> WordSimUtils.areWordsSimilar(ref.toLowerCase(Locale.ENGLISH), instance.getFullName().toLowerCase(Locale.ENGLISH)))) {
                var similarity = WordSimUtils.getSimilarity(textBox.getText().toLowerCase(Locale.ENGLISH), instance.getFullName().toLowerCase(Locale.ENGLISH));
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
        var splitAndDecameled = processText(text).stream()
                .filter(s -> !DbPediaHelper.isWordMarkupLanguage(s))
                .filter(s -> !DbPediaHelper.isWordProgrammingLanguage(s))
                .filter(s -> !DbPediaHelper.isWordSoftware(s))
                .toList();

        var noBlank = splitAndDecameled.stream().map(s -> s.replaceAll("\\s+", "")).toList();
        names.addAll(splitAndDecameled);
        names.addAll(noBlank);

        var atleastOneUpperCaseChar = names.stream().filter(s -> !s.equals(s.toLowerCase(Locale.ENGLISH))).collect(Collectors.toSet());

        if (!atleastOneUpperCaseChar.isEmpty())
            return atleastOneUpperCaseChar;

        return names;
    }

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
