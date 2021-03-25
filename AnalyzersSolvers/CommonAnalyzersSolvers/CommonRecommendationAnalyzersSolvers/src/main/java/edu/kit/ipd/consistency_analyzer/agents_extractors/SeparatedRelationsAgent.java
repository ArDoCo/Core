package edu.kit.ipd.consistency_analyzer.agents_extractors;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.kohsuke.MetaInfServices;

import edu.kit.ipd.consistency_analyzer.agents_extractors.agents.Agent;
import edu.kit.ipd.consistency_analyzer.agents_extractors.agents.AgentDatastructure;
import edu.kit.ipd.consistency_analyzer.agents_extractors.agents.Configuration;
import edu.kit.ipd.consistency_analyzer.agents_extractors.agents.DependencyType;
import edu.kit.ipd.consistency_analyzer.agents_extractors.agents.RecommendationAgent;
import edu.kit.ipd.consistency_analyzer.common.SimilarityUtils;
import edu.kit.ipd.consistency_analyzer.common.Utilis;
import edu.kit.ipd.consistency_analyzer.datastructures.IModelState;
import edu.kit.ipd.consistency_analyzer.datastructures.INounMapping;
import edu.kit.ipd.consistency_analyzer.datastructures.IRecommendationState;
import edu.kit.ipd.consistency_analyzer.datastructures.IRecommendedInstance;
import edu.kit.ipd.consistency_analyzer.datastructures.IText;
import edu.kit.ipd.consistency_analyzer.datastructures.ITextState;

/**
 * The separated relation solver is a solver for the creation of recommended relations. Whenever a recommended instance
 * owes an occurrence with a separator a separator relation is recommended.
 *
 * @author Sophie
 *
 */
@MetaInfServices(RecommendationAgent.class)
public class SeparatedRelationsAgent extends RecommendationAgent {

    private double probability;
    private String relName = "separated";

    /**
     * Creates a new SeparatedRelationsSolver
     *
     * @param graph               the PARSE graph
     * @param textState           the text extraction state
     * @param modelState          the model extraction state
     * @param recommendationState the recommendation state
     */
    public SeparatedRelationsAgent(IText text, ITextState textState, IModelState modelState, IRecommendationState recommendationState) {
        this(text, textState, modelState, recommendationState, GenericRecommendationConfig.DEFAULT_CONFIG);
    }

    public SeparatedRelationsAgent(IText text, ITextState textState, IModelState modelState, IRecommendationState recommendationState,
            GenericRecommendationConfig config) {
        super(DependencyType.RECOMMENDATION, text, textState, modelState, recommendationState);
        probability = config.separatedRelationSolverProbility;
    }

    public SeparatedRelationsAgent() {
        super(DependencyType.TEXT_MODEL_RECOMMENDATION);
    }

    @Override
    public RecommendationAgent create(IText text, ITextState textState, IModelState modelExtractionState, IRecommendationState recommendationState,
            Configuration config) {
        return new SeparatedRelationsAgent(text, textState, modelExtractionState, recommendationState, (GenericRecommendationConfig) config);
    }

    /**
     * Executes the solver
     */
    @Override
    public void exec() {

        addRelationsForAllRecommendedInstances();
    }

    /**
     * Searches for recommended instances with a separator in one of their occurrences. For each part of the splitted
     * occurrences different matchings for the end points of the relation are possible. Therefore, the carthesian
     * product is created. For each combination a possible recommendation is created.
     */
    private void addRelationsForAllRecommendedInstances() {
        for (IRecommendedInstance recommendedInstance : recommendationState.getRecommendedInstances()) {

            List<String> occurrencesWithSeparator = collectOccurrencesWithSeparators(recommendedInstance);

            for (String occurrence : occurrencesWithSeparator) {
                buildRelation(getAllCorrespondingRecommendationsForParticipants(recommendedInstance, occurrence));
            }

        }
    }

    private List<String> collectOccurrencesWithSeparators(IRecommendedInstance recommendedInstance) {
        List<String> occs = collectOccurrencesAsStrings(recommendedInstance);
        return occs.stream().filter(SimilarityUtils::containsSeparator).collect(Collectors.toList());

    }

    private List<String> collectOccurrencesAsStrings(IRecommendedInstance recInstance) {
        List<String> occurrences = new ArrayList<>();
        for (INounMapping nnm : recInstance.getNameMappings()) { occurrences.addAll(nnm.getOccurrences()); }
        return occurrences;
    }

    private List<List<IRecommendedInstance>> getAllCorrespondingRecommendationsForParticipants(IRecommendedInstance recInstance, String occurrence) {
        String recInstanceName = recInstance.getName();

        occurrence = SimilarityUtils.splitAtSeparators(occurrence);
        List<String> relationParticipants = new ArrayList<>(List.of(occurrence.split(" ")));

        List<List<IRecommendedInstance>> participatingRecInstances = new ArrayList<>();

        for (int i = 0; i < relationParticipants.size(); i++) {
            String participant = relationParticipants.get(i);
            participatingRecInstances.add(new ArrayList<>());

            if (SimilarityUtils.areWordsSimilar(recInstanceName, participant)) {
                participatingRecInstances.get(i).add(recInstance);
                continue;
            }

            participatingRecInstances.get(i).addAll(recommendationState.getRecommendedInstancesBySimilarName(participant));
        }
        return participatingRecInstances;
    }

    private void buildRelation(List<List<IRecommendedInstance>> recommendedParticipants) {
        if (recommendedParticipants.size() < 2) {
            return;
        }

        List<List<IRecommendedInstance>> allRelationProbabilities = Utilis.cartesianProduct(new ArrayList<>(), recommendedParticipants);

        for (List<IRecommendedInstance> possibility : allRelationProbabilities) {

            IRecommendedInstance r1 = possibility.get(0);
            IRecommendedInstance r2 = possibility.get(1);
            possibility.remove(r1);
            possibility.remove(r2);

            recommendationState.addRecommendedRelation(relName, r1, r2, possibility, probability, new ArrayList<>());

        }

    }

    @Override
    public RecommendationAgent create(IText text, ITextState textState, IModelState modelState, IRecommendationState recommendationState) {
        return create(text, textState, modelState, recommendationState, GenericRecommendationConfig.DEFAULT_CONFIG);
    }

    @Override
    public Agent create(AgentDatastructure data) {
        return create(data, GenericRecommendationConfig.DEFAULT_CONFIG);
    }

}
