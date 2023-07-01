package edu.kit.kastel.mcse.ardoco.core.diagramconnectiongenerator.informants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.set.ImmutableSet;

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
        }
    }

    private void findTextOfDiagramInstancesInSupposedMappings(DiagramRecognitionState diagramState, RecommendationState recommendationState,
            DiagramConnectionState diagramConnectionState) {
        var recommendedInstances = recommendationState.getRecommendedInstances();
        for (Diagram diagram : diagramState.getDiagrams()) {
            for (Pair<DiagramElement, ModelInstance> pair : diagramToModelInstances(diagram)) {
                //SimilarityUtils.isRecommendedInstanceSimilarToModelInstance(recommendedInstances.get(10), dataRepository.getData("ModelStatesData", ModelStates.class).get().getModelExtractionState("_tRJJ0KESEeu-mYqkDskRow").getInstances().get(4))
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
                //TODO SimilarityUtils.isRecommendedInstanceSimilarToModelInstance(recommendedInstance, dataRepository.getData("ModelStatesData", ModelStates.class).get().getModelExtractionState("_tRJJ0KESEeu-mYqkDskRow").getInstances().get(4))
                var sameInstances = diagramToModelInstances(diagram).stream()
                        .filter(pair -> SimilarityUtils.isRecommendedInstanceSimilarToModelInstance(recommendedInstance, pair.second()));
                sameInstances.forEach(pair -> diagramConnectionState.addToDiagramLinks(recommendedInstance, pair.first(), this, 1));
            }
        }
    }

    private List<Pair<DiagramElement, ModelInstance>> diagramToModelInstances(Diagram diagram) {
        //Create model instances so we can reuse a lot of code
        var instances = new ArrayList<Pair<DiagramElement, ModelInstance>>();

        for (Box box : diagram.getBoxes()) {
            var names = possibleNames(box);
            names.forEach(
                    name -> instances.add(new Pair<DiagramElement, ModelInstance>(box, new ModelInstanceImpl(name, "", Integer.toString(name.hashCode())))));
        }

        return instances;
    }

    private Set<String> possibleNames(Box box) {
        var names = Sets.mutable.<String>empty();

        names.addAll(box.getTexts().stream().flatMap(t -> processText(t.getText()).stream()).toList());
        names.addAll(names.stream().flatMap(t -> possibleAbbreviations(t).stream()).toList());

        return names;
    }

    private ImmutableSet<String> processText(String text) {
        //Split up "Sth (Sthelse)"
        var split = Arrays.stream(text.split(",|\\(|\\)")).map(s -> s.trim()).collect(Collectors.toList());
        //Split up "camelCase", "CamelCase", "CamelABBREVIATIONCase" etc
        var decameled = split.stream().flatMap(s -> Arrays.stream(s.split("(?<!([A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])"))).reduce((l, r) -> l + " " + r);
        if (decameled.isPresent())
            split.add(decameled.get());
        split.remove("");
        return Sets.immutable.ofAll(split);
    }

    private ImmutableSet<String> possibleAbbreviations(String text) {
        var matcher = abbreviationsPattern.matcher(text);
        return Sets.immutable.fromStream(matcher.results().map(MatchResult::group));
    }
}
