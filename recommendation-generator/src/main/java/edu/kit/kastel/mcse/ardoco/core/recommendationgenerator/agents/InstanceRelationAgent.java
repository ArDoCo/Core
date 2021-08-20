package edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.agents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import edu.kit.kastel.mcse.ardoco.core.datastructures.InstanceRelation;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.Configuration;
import edu.kit.kastel.mcse.ardoco.core.datastructures.agents.DependencyAgent;
import edu.kit.kastel.mcse.ardoco.core.datastructures.common.WordHelper;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.DependencyTag;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IInstanceRelation;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IModelState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.INounMapping;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IRecommendationState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IRecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IText;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.ITextState;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IWord;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.GenericRecommendationConfig;

/**
 * Adds
 *
 * @see InstanceRelation instances to {@link InstanceRelationAgent#recommendationState}
 */
public class InstanceRelationAgent extends DependencyAgent {

    /**
     * Default constructor
     */
    public InstanceRelationAgent() {
        super(GenericRecommendationConfig.class);
    }

    /**
     * Constructor
     *
     * @param text                annotated text
     * @param textState           state of text
     * @param modelState          model state
     * @param recommendationState state of recommendations
     */
    public InstanceRelationAgent(IText text, ITextState textState, IModelState modelState, IRecommendationState recommendationState) {
        super(GenericRecommendationConfig.class, text, textState, modelState, recommendationState);
    }

    /**
     * Create a new InstanceRelationAgent from passed parameters
     *
     * @param text                annotated text
     * @param textState           state of text
     * @param modelState          model state
     * @param recommendationState state of recommendations
     * @param config              configuration
     * @return new InstanceRelationAgent instance
     */
    @Override
    public InstanceRelationAgent create(IText text, ITextState textState, IModelState modelState, IRecommendationState recommendationState,
            Configuration config) {
        return new InstanceRelationAgent(text, textState, modelState, recommendationState);
    }

    /**
     * Agent execution function
     */
    @Override
    public void exec() {
        getRelations();
    }

    /**
     * Search for relations between RecommendedInstances
     *
     * @return list of recommended relations
     */
    private void getRelations() {
        for (IRecommendedInstance instance : recommendationState.getRecommendedInstances()) {
            for (IWord word : getInstanceWords(instance)) {
                List<IWord> verbsOn = getVerbsOn(word);
                for (IWord verbOn : verbsOn) {
                    for (IWord secondWord : getNounDepOf(verbOn)) {
                        if (word.getSentenceNo() != verbOn.getSentenceNo() || secondWord.getSentenceNo() != verbOn.getSentenceNo()) {
                            // If word, verbOn and secondWord are not in the same sentence, ignore
                            continue;
                        }
                        IWord from = word.getPosition() < secondWord.getPosition() ? word : secondWord;
                        IWord to = word.getPosition() < secondWord.getPosition() ? secondWord : word;
                        for (IRecommendedInstance secondInstance : recommendationState.getRecommendedInstances()) {

                            if (getInstanceWords(secondInstance).contains(secondWord) && !instance.equals(secondInstance) && !word.equals(secondWord)) {
                                boolean newInstance = true;
                                for (IInstanceRelation relation : recommendationState.getInstanceRelations()) {
                                    if (relation.isIn(verbOn, Collections.singletonList(from), Collections.singletonList(to))) {
                                        /*
                                         * Break loop if relation already exists
                                         */
                                        newInstance = false;
                                        break;
                                    } else if (relation.matches(instance, secondInstance)) {
                                        /*
                                         * Add to existing if instances match, but specific relation does not exist
                                         * there yet
                                         */
                                        newInstance = false;
                                        logger.debug("Add to existing InstanceRelation from {} over {} to {}", word.getText(), verbOn.getText(),
                                                secondWord.getText());
                                        relation.addLink(verbOn, Collections.singletonList(from), Collections.singletonList(to));
                                        break;
                                    }
                                }
                                if (newInstance) {
                                    /*
                                     * Add new relation if not found previously
                                     */
                                    logger.debug("Add new InstanceRelation from {} over {} to {}", word.getText(), verbOn.getText(), secondWord.getText());
                                    recommendationState.addInstanceRelation(instance, secondInstance, verbOn, Collections.singletonList(from),
                                            Collections.singletonList(to));
                                }
                            }
                        }
                    }
                }
            }
        }

        logger.info("Found {} InstanceRelations", recommendationState.getInstanceRelations().size());
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
        return getNounDepOn(word).stream().filter(WordHelper::isVerb).collect(Collectors.toList());
    }

    private List<IWord> getNounDepOn(IWord word) {
        List<IWord> dependencies = new ArrayList<>();
        dependencies.addAll(word.getWordsThatAreDependentOnThis(DependencyTag.OBJ).castToList());
        dependencies.addAll(word.getWordsThatAreDependentOnThis(DependencyTag.NSUBJ).castToList());
        return dependencies;
    }

    private List<IWord> getVerbsOf(IWord word) {
        // TODO isVerb access via class compatible with their jv version?
        return getNounDepOf(word).stream().filter(WordHelper::isVerb).collect(Collectors.toList());
    }

    private List<IWord> getNounDepOf(IWord word) {
        List<IWord> dependencies = new ArrayList<>();
        dependencies.addAll(word.getWordsThatAreDependencyOfThis(DependencyTag.OBJ).castToList());
        dependencies.addAll(word.getWordsThatAreDependencyOfThis(DependencyTag.NSUBJ).castToList());
        return dependencies;
    }
}
