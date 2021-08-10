package edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.agents_extractors;

import edu.kit.kastel.mcse.ardoco.core.datastructures.InstanceRelation;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.Configuration;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.DependencyType;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.Loader;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.DependencyAgent;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.*;
import edu.kit.kastel.mcse.ardoco.core.datastructures.extractors.DependencyExtractor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Adds
 * @see InstanceRelation
 * instances to {@link InstanceRelationAgent#recommendationState}
 */
public class InstanceRelationAgent extends DependencyAgent {

    private static final Logger logger = LogManager.getLogger("InstanceRelationAgent");
    private List<DependencyExtractor> extractors = new ArrayList<>();

    /**
     * Default constructor
     */
    public InstanceRelationAgent() {
        super(GenericRecommendationConfig.class);
    }

    /**
     * Constructor
     *
     * @param text annotated text
     * @param textState state of text
     * @param modelState model state
     * @param recommendationState state of recommendations
     * @param config configuration
     */
    public InstanceRelationAgent(IText text, ITextState textState, IModelState modelState, IRecommendationState recommendationState,
                                 GenericRecommendationConfig config) {
        super(DependencyType.TEXT_MODEL_RECOMMENDATION, GenericRecommendationConfig.class, text, textState, modelState, recommendationState);
        initializeExtractors(config.dependencyExtractors, config);
    }

    /**
     * Initialize extractors, currently not used
     *
     * @param extractorList list of extractors
     * @param config configuration
     */
    private void initializeExtractors(List<String> extractorList, GenericRecommendationConfig config) {
        Map<String, DependencyExtractor> loadedExtractors = Loader.loadLoadable(DependencyExtractor.class);

        for (String dependencyExtractor : extractorList) {
            if (!loadedExtractors.containsKey(dependencyExtractor)) {
                throw new IllegalArgumentException("DependencyExtractor " + dependencyExtractor + " not found");
            }
            extractors.add(loadedExtractors.get(dependencyExtractor).create(textState, modelState, recommendationState, config));
        }
    }

    /**
     * Create a new InstanceRelationAgent from passed parameters
     *
     * @param text annotated text
     * @param textState state of text
     * @param modelState model state
     * @param recommendationState state of recommendations
     * @param config configuration
     * @return new InstanceRelationAgent instance
     */
    @Override
    public InstanceRelationAgent create(IText text, ITextState textState, IModelState modelState, IRecommendationState recommendationState, Configuration config) {
        return new InstanceRelationAgent(text, textState, modelState, recommendationState, (GenericRecommendationConfig) config);
    }

    /**
     * Agent execution function
     */
    @Override
    public void exec() {
        this.getRelations();
    }

    /**
     * Search for relations between RecommendedInstances
     *
     * @return list of recommended relations
     */
    private void getRelations() {
        for (IRecommendedInstance instance : recommendationState.getRecommendedInstances()) {
            for (IWord word : this.getInstanceWords(instance)) {
                List<IWord> verbsOn = this.getVerbsOn(word);
                for (IWord verbOn : verbsOn) {
                    for (IWord secondWord : this.getNounDepOf(verbOn)) {
                        if (word.getSentenceNo() != verbOn.getSentenceNo() || secondWord.getSentenceNo() != verbOn.getSentenceNo()) {
                            // If word, verbOn and secondWord are not in the same sentence, ignore
                            continue;
                        }
                        for (IRecommendedInstance secondInstance : recommendationState.getRecommendedInstances()) {
                            if (!secondInstance.equals(instance) &&
                                this.getInstanceWords(secondInstance).contains(secondWord) &&
                                !word.equals(secondWord) &&
                                this.recommendationState.getInstanceRelations().stream().noneMatch(
                                    i ->
                                    i.isIn(verbOn, Collections.singletonList(word), Collections.singletonList(secondWord)) ||
                                    i.isIn(verbOn, Collections.singletonList(secondWord), Collections.singletonList(word)))
                            ) {
                                /*
                                Add new relation only if
                                1. Instance and secondInstance are not equal
                                2. SecondInstance contains secondWord
                                3. Word and secondWord are not equal
                                4. The relation is not already contained in relations
                                 */
                                logger.debug("Add InstanceRelation from {} over {} to {}", word.getLemma(), verbOn.getLemma(), secondWord.getLemma());
                                this.recommendationState.addInstanceRelation(verbOn.getLemma(),
                                        1,
                                        Collections.singletonList(instance),
                                        Collections.singletonList(secondInstance),
                                        verbOn,
                                        Collections.singletonList(word),
                                        Collections.singletonList(secondWord));
                            }
                        }
                    }
                }
            }
        }

        logger.info("Found {} InstanceRelations", this.recommendationState.getInstanceRelations().size());
    }

    private List<IWord> getInstanceWords(IRecommendedInstance instance) {
        List<IWord> words = new ArrayList<>();
        for (INounMapping mapping : instance.getNameMappings()) {
            words.addAll(mapping.getWords());
        }
        return words;
    }

    private List<IWord> getVerbsOn(IWord word) {
        // TODO isVerb access via class compatible with their jv version?
        return this.getNounDepOn(word).stream().filter(IWord::isVerb).collect(Collectors.toList());
    }

    private List<IWord> getNounDepOn(IWord word) {
        List<IWord> dependencies = word.getWordsThatAreDependentOnThis(DependencyTag.OBJ);
        dependencies.addAll(word.getWordsThatAreDependentOnThis(DependencyTag.NSUBJ));
        return dependencies;
    }

    private List<IWord> getVerbsOf(IWord word) {
        // TODO isVerb access via class compatible with their jv version?
        return this.getNounDepOf(word).stream().filter(IWord::isVerb).collect(Collectors.toList());
    }

    private List<IWord> getNounDepOf(IWord word) {
        List<IWord> dependencies = word.getWordsThatAreDependencyOfThis(DependencyTag.OBJ);
        dependencies.addAll(word.getWordsThatAreDependencyOfThis(DependencyTag.NSUBJ));
        return dependencies;
    }

    private String listToString(List<IWord> text) {
        StringBuilder str = new StringBuilder();
        for (IWord word : text) {
            str.append(word.getText()).append(" ");
        }
        return str.toString();
    }
}
