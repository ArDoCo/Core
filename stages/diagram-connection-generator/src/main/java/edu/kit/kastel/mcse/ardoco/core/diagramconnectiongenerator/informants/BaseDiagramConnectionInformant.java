package edu.kit.kastel.mcse.ardoco.core.diagramconnectiongenerator.informants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.common.util.SimilarityUtils;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.models.ModelInstanceImpl;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;

public class BaseDiagramConnectionInformant extends Informant {
    private final Pattern abbreviationsPattern = Pattern.compile("\\b(?:[A-Z][a-z]*){2,}");

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
        for (var model : modelStates.extractionModelIds()) {
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
        for (Diagram diagram : diagramState.getDiagrams()) {
            for (Pair<DiagramElement, ModelInstance> pair : diagramToModelInstances(diagram)) {
                var mostLikelyRi = SimilarityUtils.getMostRecommendedInstancesToInstanceByReferences(pair.second(), recommendedInstances);
                for (var recommendedInstance : mostLikelyRi) {
                    diagramConnectionState.addToDiagramLinks(recommendedInstance, pair.first(), this, 1);
                }
            }
        }
    }

    private void createLinksForEqualOrSimilarRecommendedInstances(DiagramRecognitionState diagramState, RecommendationState recommendationState,
            DiagramConnectionState diagramConnectionState) {
        for (Diagram diagram : diagramState.getDiagrams()) {
            for (var recommendedInstance : recommendationState.getRecommendedInstances()) {
                var sameInstances = diagramToModelInstances(diagram).stream()
                        .filter(pair -> SimilarityUtils.isRecommendedInstanceSimilarToModelInstance(recommendedInstance, pair.second()));
                sameInstances.forEach(pair -> diagramConnectionState.addToDiagramLinks(recommendedInstance, pair.first(), this, 1));
            }
        }
    }

    private void createLinksBasedOnDiagramElements(@NotNull DiagramRecognitionState diagramState, @NotNull RecommendationState recommendationState,
            @NotNull DiagramConnectionState diagramConnectionState) {
        for (Diagram diagram : diagramState.getDiagrams()) {
            for (var box : diagram.getBoxes()) {
                for (var tBox : box.getTexts()) {
                    for (var recommendedInstance : recommendationState.getRecommendedInstances()) {
                        if (isShorteningOf(recommendedInstance.getName(), tBox.getText())) {
                            diagramConnectionState.addToDiagramLinks(recommendedInstance, box, this, 1);
                        }
                    }
                }
            }
        }
    }

    private List<Pair<DiagramElement, ModelInstance>> diagramToModelInstances(Diagram diagram) {
        //Create model instances so we can reuse a lot of code
        var instances = new ArrayList<Pair<DiagramElement, ModelInstance>>();

        for (Box box : diagram.getBoxes()) {
            var names = possibleNames(box);
            names.forEach(name -> instances.add(new Pair<>(box, new ModelInstanceImpl(name, "", Integer.toString(name.hashCode())))));
        }

        return instances;
    }

    private ImmutableSet<String> possibleNames(@NotNull Box box) {
        var names = Sets.mutable.<String>empty();

        names.addAll(box.getTexts().stream().flatMap(t -> processText(t.getText()).stream()).toList());
        names.addAll(names.stream().flatMap(t -> possibleAbbreviations(t).stream()).toList());
        var noBlank = names.stream().map(s -> s.replaceAll("\\s+", "")).toList();
        names.addAll(noBlank);

        return Sets.immutable.ofAll(names);
    }

    private ImmutableSet<String> processText(@NotNull String text) {
        //Split up "Sth (Sthelse)"
        var split = Arrays.stream(text.split(",|\\(|\\)")).map(String::trim).collect(Collectors.toList());
        //Split up "camelCase", "CamelCase", "CamelABBREVIATIONCase" etc
        var decameled = split.stream().flatMap(s -> Arrays.stream(s.split("(?<!([A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])"))).reduce((l, r) -> l + " " + r);
        decameled.ifPresent(split::add);
        split.remove("");
        return Sets.immutable.ofAll(split);
    }

    private ImmutableSet<String> possibleAbbreviations(String text) {
        var matcher = abbreviationsPattern.matcher(text);
        return Sets.immutable.fromStream(matcher.results().map(MatchResult::group));
    }

    private boolean containsAllInOrder(@NotNull String s, @NotNull String query) {
        var previous = -1;
        for (char c : query.toCharArray()) {
            var current = s.indexOf(String.valueOf(c));
            if (current <= previous)
                return false;
            previous = current;
        }
        return true;
    }

    private boolean isShorteningOf(@NotNull String text, @NotNull String shortening) {
        if (!couldBeShortening(shortening))
            return false;

        var lc = text.toLowerCase();
        var shortLc = shortening.toLowerCase();

        //Check if the entire shortening is contained within the single word
        if (!lc.contains(" "))
            return lc.startsWith(shortLc.substring(0, 1)) && containsAllInOrder(lc, shortLc);

        var reg = "";
        for (var c : shortLc.toCharArray()) {
            reg += c + "|";
        }

        var onlyShorteningLettersAndBlank = "\\[^(" + reg + "\\s)\\]";
        var split = lc.split("\\s+");
        var reducedText = Arrays.stream(split).filter(s -> s.startsWith(onlyShorteningLettersAndBlank)).reduce("", (l, r) -> l + r);

        //The text contains words that are irrelevant to the supposed shortening
        if (reducedText.length() != split.length)
            return false;

        return containsAllInOrder(reducedText, shortLc);
    }

    private boolean couldBeShortening(@NotNull String text) {
        if (text.isEmpty())
            return false;
        var upperCaseCharacters = 0;
        for (char c : text.toCharArray()) {
            if (Character.isUpperCase(c))
                upperCaseCharacters++;
        }
        return upperCaseCharacters >= 0.5 * text.length();
    }
}
