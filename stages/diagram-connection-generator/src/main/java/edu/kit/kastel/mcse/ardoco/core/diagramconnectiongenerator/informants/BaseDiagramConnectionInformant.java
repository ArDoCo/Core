package edu.kit.kastel.mcse.ardoco.core.diagramconnectiongenerator.informants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.set.ImmutableSet;
import org.jetbrains.annotations.NotNull;

import edu.kit.kastel.mcse.ardoco.core.api.diagramconnectiongenerator.DiagramConnectionState;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconnectiongenerator.DiagramConnectionStates;
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Box;
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Diagram;
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramElement;
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramRecognitionState;
import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelInstance;
import edu.kit.kastel.mcse.ardoco.core.api.recommendationgenerator.RecommendationState;
import edu.kit.kastel.mcse.ardoco.core.common.tuple.Pair;
import edu.kit.kastel.mcse.ardoco.core.common.util.AbbreviationDisambiguationHelper;
import edu.kit.kastel.mcse.ardoco.core.common.util.DBPediaHelper;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.common.util.SimilarityUtils;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.WordSimUtils;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.models.ModelInstanceImpl;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;

public class BaseDiagramConnectionInformant extends Informant {
    /**
     * Matches abbreviations with up to 1 lowercase letter between uppercase letters. Accounts for camelCase by lookahead, e.g. UserDBAdapter is matched as "DB"
     * rather than "DBA". Matches abbreviations at any point in the word, including at the start and end.
     */
    private final Pattern abbreviationsPattern = Pattern.compile("(?:([A-Z]+[a-z]?)+[A-Z])(?=([A-Z][a-z])|\\b)");

    public BaseDiagramConnectionInformant(DataRepository dataRepository) {
        super(BaseDiagramConnectionInformant.class.getSimpleName(), dataRepository);
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
        //Empty
    }

    @Override
    public void run() {
        var dataRepository = getDataRepository();
        var diagramState = dataRepository.getData(DiagramRecognitionState.ID, DiagramRecognitionState.class).get();
        var modelStates = DataRepositoryHelper.getModelStatesData(dataRepository);
        var recommendationStates = DataRepositoryHelper.getRecommendationStates(dataRepository);
        var diagramConnectionStates = dataRepository.getData(DiagramConnectionStates.ID, DiagramConnectionStates.class).get();
        var modelIds = modelStates.extractionModelIds();
        for (var model : modelIds) {
            var modelState = modelStates.getModelExtractionState(model);
            Metamodel mm = modelState.getMetamodel();
            var recommendationState = recommendationStates.getRecommendationState(mm);
            var diagramConnectionState = diagramConnectionStates.getDiagramConnectionState(mm);

            findTextOfDiagramInstancesInSupposedMappings(diagramState, recommendationState, diagramConnectionState);
            createLinksForEqualOrSimilarRecommendedInstances(diagramState, recommendationState, diagramConnectionState);
            createLinksBasedOnDiagramElements(diagramState, recommendationState, diagramConnectionState);
        }
    }

    private void findTextOfDiagramInstancesInSupposedMappings(DiagramRecognitionState diagramState, RecommendationState recommendationState,
            DiagramConnectionState diagramConnectionState) {
        var recommendedInstances = recommendationState.getRecommendedInstances();
        var diagrams = diagramState.getDiagrams();
        for (Diagram diagram : diagrams) {
            var diagramModelInstances = diagramToModelInstances(diagram);
            for (Pair<DiagramElement, ModelInstance> pair : diagramModelInstances) {
                var mostLikelyRi = SimilarityUtils.getMostRecommendedInstancesToInstanceByReferences(pair.second(), recommendedInstances);
                for (var recommendedInstance : mostLikelyRi) {
                    diagramConnectionState.addToDiagramLinks(recommendedInstance, pair.first(), this,
                            WordSimUtils.getSimilarity(recommendedInstance.getName(), pair.second().getName()));
                }
            }
        }
    }

    private void createLinksForEqualOrSimilarRecommendedInstances(DiagramRecognitionState diagramState, RecommendationState recommendationState,
            DiagramConnectionState diagramConnectionState) {
        var diagrams = diagramState.getDiagrams();
        for (Diagram diagram : diagrams) {
            var diagramModelInstances = diagramToModelInstances(diagram);
            var ris = recommendationState.getRecommendedInstances();
            for (var recommendedInstance : ris) {
                var sameInstances = diagramModelInstances.stream()
                        .filter(pair -> SimilarityUtils.isRecommendedInstanceSimilarToModelInstance(recommendedInstance, pair.second()));
                sameInstances.forEach(pair -> diagramConnectionState.addToDiagramLinks(recommendedInstance, pair.first(), this,
                        WordSimUtils.getSimilarity(recommendedInstance.getName(), pair.second().getName())));
            }
        }
    }

    private void createLinksBasedOnDiagramElements(@NotNull DiagramRecognitionState diagramState, @NotNull RecommendationState recommendationState,
            @NotNull DiagramConnectionState diagramConnectionState) {
        var diagrams = diagramState.getDiagrams();
        for (Diagram diagram : diagrams) {
            var boxes = diagram.getBoxes();
            for (var box : boxes) {
                var texts = box.getTexts();
                for (var tBox : texts) {
                    var ris = recommendationState.getRecommendedInstances();
                    for (var recommendedInstance : ris) {
                        if (isInitialismOf(recommendedInstance.getName(), tBox.getText())) {
                            diagramConnectionState.addToDiagramLinks(recommendedInstance, box, this,
                                    WordSimUtils.getSimilarity(recommendedInstance.getName(), tBox.getText()));
                        }
                    }
                }
            }
        }
    }

    private List<Pair<DiagramElement, ModelInstance>> diagramToModelInstances(Diagram diagram) {
        //Create model instances so we can reuse a lot of code
        var instances = new ArrayList<Pair<DiagramElement, ModelInstance>>();

        var boxes = diagram.getBoxes();
        for (Box box : boxes) {
            var names = possibleNames(box);
            names.forEach(name -> instances.add(new Pair<>(box, new ModelInstanceImpl(name, "", Integer.toString(name.hashCode())))));
        }

        return instances;
    }

    private ImmutableSet<String> possibleNames(@NotNull Box box) {
        var names = Sets.mutable.<String>empty();

        var texts = box.getTexts();
        for (var textBox : texts) {
            var text = textBox.getText();
            var splitAndDecameled = processText(text).toList()
                    .stream()
                    .filter(s -> !DBPediaHelper.isWordMarkupLanguage(s))
                    .filter(s -> !DBPediaHelper.isWordProgrammingLanguage(s))
                    .filter(s -> !DBPediaHelper.isWordSoftware(s))
                    .toList();
            var abbreviations = possibleAbbreviations(text).stream().map(AbbreviationDisambiguationHelper.getInstance()::get).toList();
            var noBlank = splitAndDecameled.stream().map(s -> s.replaceAll("\\s+", "")).toList();
            names.addAll(splitAndDecameled);
            names.addAll(noBlank);
        }

        return Sets.immutable.ofAll(names);
    }

    private ImmutableSet<String> processText(@NotNull String text) {
        var words = Sets.mutable.<String>empty();
        //Split up "Sth (Sthelse)"
        var split = Arrays.stream(text.split(",|\\(|\\)")).map(String::trim).toList();
        //Reduce back to single string, remove duplicate whitespaces
        var deCameledSplit = split.stream().map(this::deCamel).toList();
        words.addAll(split);
        words.addAll(deCameledSplit);
        words.remove("");
        return Sets.immutable.ofAll(words);
    }

    private String deCamel(String input) {
        //Split up "camelCase", "CamelCase", "CamelABBREVIATIONCase" etc
        return String.join(" ", input.split("(?<!([A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])")).replaceAll("\\s+", " ");
    }

    private ImmutableSet<String> possibleAbbreviations(String text) {
        var matcher = abbreviationsPattern.matcher(text);
        return Sets.immutable.fromStream(matcher.results().map(MatchResult::group));
    }

    private boolean containsAllInOrder(@NotNull String s, @NotNull String query) {
        var previous = -1;
        var cArray = query.toCharArray();
        for (char c : cArray) {
            var current = s.indexOf(String.valueOf(c));
            if (current <= previous)
                return false;
            previous = current;
        }
        return true;
    }

    private boolean isInitialismOf(@NotNull String text, @NotNull String Initialism) {
        if (!couldBeInitialism(Initialism))
            return false;

        var lc = text.toLowerCase();
        var initialLc = Initialism.toLowerCase();

        //Check if the entire Initialism is contained within the single word
        if (!lc.contains(" "))
            return lc.startsWith(initialLc.substring(0, 1)) && containsAllInOrder(lc, initialLc);

        var reg = "";
        var initialLcArray = initialLc.toCharArray();
        for (var c : initialLcArray) {
            reg += c + "|";
        }

        var onlyInitialismLettersAndBlank = "\\[^(" + reg + "\\s)\\]";
        var split = lc.split("\\s+");
        var reducedText = Arrays.stream(split).filter(s -> s.startsWith(onlyInitialismLettersAndBlank)).reduce("", (l, r) -> l + r);

        //The text contains words that are irrelevant to the supposed Initialism
        if (reducedText.length() != split.length)
            return false;

        return containsAllInOrder(reducedText, initialLc);
    }

    private boolean couldBeInitialism(@NotNull String text) {
        if (text.isEmpty())
            return false;
        var upperCaseCharacters = 0;
        var cArray = text.toCharArray();
        for (char c : cArray) {
            if (Character.isUpperCase(c))
                upperCaseCharacters++;
        }
        return upperCaseCharacters >= 0.5 * text.length();
    }
}
