package edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.agents;

import edu.kit.kastel.mcse.ardoco.core.datastructures.InstanceRelation;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.Configuration;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.DependencyAgent;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.*;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.GenericRecommendationConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.collections.api.list.ImmutableList;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Adds
 * @see InstanceRelation
 * instances to {@link InstanceRelationAgent#recommendationState}
 */
public class InstanceRelationAgent extends DependencyAgent {

    private static final Logger logger = LogManager.getLogger("InstanceRelationAgent");

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
     */
    public InstanceRelationAgent(IText text, ITextState textState, IModelState modelState, IRecommendationState recommendationState) {
        super(GenericRecommendationConfig.class, text, textState, modelState, recommendationState);
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
        return new InstanceRelationAgent(text, textState, modelState, recommendationState);
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
                        IWord from = word.getPosition() < secondWord.getPosition() ? word : secondWord;
                        IWord to = word.getPosition() < secondWord.getPosition() ? secondWord : word;
                        for (IRecommendedInstance secondInstance : recommendationState.getRecommendedInstances()) {

                            if (this.getInstanceWords(secondInstance).contains(secondWord) && !instance.equals(secondInstance) && !word.equals(secondWord)) {
                                boolean newInstance = true;
                                for (IInstanceRelation relation : this.recommendationState.getInstanceRelations()) {
                                    if (relation.isIn(verbOn, Collections.singletonList(from), Collections.singletonList(to))) {
                                        /*
                                        Break loop if relation already exists
                                         */
                                        newInstance = false;
                                        break;
                                    } else if (relation.matches(instance, secondInstance)) {
                                        /*
                                        Add to existing if instances match, but specific relation does not exist there yet
                                         */
                                        newInstance = false;
                                        logger.debug("Add to existing InstanceRelation from {} over {} to {}", word.getText(), verbOn.getText(), secondWord.getText());
                                        relation.addLink(verbOn, Collections.singletonList(from), Collections.singletonList(to));
                                        break;
                                    }
                                }
                                if (newInstance) {
                                    /*
                                    Add new relation if not found previously
                                     */
                                    logger.debug("Add new InstanceRelation from {} over {} to {}", word.getText(), verbOn.getText(), secondWord.getText());
                                    this.recommendationState.addInstanceRelation(instance,
                                            secondInstance,
                                            verbOn,
                                            Collections.singletonList(from),
                                            Collections.singletonList(to));
                                }
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
            words.addAll(mapping.getWords().castToList());
        }
        return words;
    }

    private List<IWord> getVerbsOn(IWord word) {
        // TODO isVerb access via class compatible with their jv version?
        return this.getNounDepOn(word).stream().filter(IWord::isVerb).collect(Collectors.toList());
    }

    private List<IWord> getNounDepOn(IWord word) {
        List<IWord> dependencies = new ArrayList<>();
        dependencies.addAll(word.getWordsThatAreDependentOnThis(DependencyTag.OBJ).castToList());
        dependencies.addAll(word.getWordsThatAreDependentOnThis(DependencyTag.NSUBJ).castToList());
        return dependencies;
    }

    private List<IWord> getVerbsOf(IWord word) {
        // TODO isVerb access via class compatible with their jv version?
        return this.getNounDepOf(word).stream().filter(IWord::isVerb).collect(Collectors.toList());
    }

    private List<IWord> getNounDepOf(IWord word) {
        List<IWord> dependencies = new ArrayList<>();
        dependencies.addAll(word.getWordsThatAreDependencyOfThis(DependencyTag.OBJ).castToList());
        dependencies.addAll(word.getWordsThatAreDependencyOfThis(DependencyTag.NSUBJ).castToList());
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
