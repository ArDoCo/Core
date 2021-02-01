package edu.kit.ipd.consistency_analyzer.analyzers_solvers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.kohsuke.MetaInfServices;

import edu.kit.ipd.consistency_analyzer.agents.AgentDatastructure;
import edu.kit.ipd.consistency_analyzer.agents.DependencyType;
import edu.kit.ipd.consistency_analyzer.agents.Loader;
import edu.kit.ipd.consistency_analyzer.agents.RecommendationAgent;
import edu.kit.ipd.consistency_analyzer.common.Tuple;
import edu.kit.ipd.consistency_analyzer.datastructures.IModelState;
import edu.kit.ipd.consistency_analyzer.datastructures.IRecommendationState;
import edu.kit.ipd.consistency_analyzer.datastructures.IText;
import edu.kit.ipd.consistency_analyzer.datastructures.ITextState;
import edu.kit.ipd.consistency_analyzer.datastructures.IWord;
import edu.kit.ipd.consistency_analyzer.extractors.IExtractor;
import edu.kit.ipd.consistency_analyzer.extractors.RecommendationExtractor;

@MetaInfServices(RecommendationAgent.class)
public class InitialRecommendationAgent extends RecommendationAgent {

    private Logger logger = Logger.getLogger(getName());
    private List<IExtractor> extractors = new ArrayList<>();

    public InitialRecommendationAgent(IText text, ITextState textState, IModelState modelState, IRecommendationState recommendationState) {
        super(DependencyType.TEXT_MODEL_RECOMMENDATION, text, textState, modelState, recommendationState);
        initializeAgents();
    }

    public InitialRecommendationAgent(AgentDatastructure data) {
        this(data.getText(), data.getTextState(), data.getModelState(), data.getRecommendationState());
    }

    public InitialRecommendationAgent() {
        super(DependencyType.TEXT_MODEL_RECOMMENDATION);
    }

    public InitialRecommendationAgent(AgentDatastructure data, Tuple<List<IExtractor>, Map<IExtractor, List<Double>>> extractorsTuple) {

        this(data);

        extractors = new ArrayList<>(extractorsTuple.getFirst());
        Map<IExtractor, List<Double>> map = extractorsTuple.getSecond();
        for (IExtractor extractor : extractors) {
            if (map.containsKey(extractor)) {
                extractor.setProbability(map.get(extractor));
            } else {
                logger.warning("The extractor " + extractor.getName() + " has no specified probability!");
            }
        }
    }

    private void initializeAgents() {
        Map<String, RecommendationExtractor> loadedExtractors = Loader.loadLoadable(RecommendationExtractor.class);

        for (String recommendationExtractor : GenericRecommendationConfig.RECOMMENDATION_EXTRACTORS) {
            if (!loadedExtractors.containsKey(recommendationExtractor)) {
                throw new IllegalArgumentException("RecommendationExtractor " + recommendationExtractor + " not found");
            }
            extractors.add(loadedExtractors.get(recommendationExtractor).create(textState, modelState, recommendationState));
        }

    }

    @Override
    public RecommendationAgent create(IText text, ITextState textState, IModelState modelState, IRecommendationState recommendationState) {
        return new InitialRecommendationAgent(text, textState, modelState, recommendationState);
    }

    @Override
    public void exec() {

        for (IWord word : text.getWords()) { for (IExtractor extractor : extractors) { extractor.exec(word); } }
    }
}
