/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.agents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.api.agent.RecommendationAgent;
import edu.kit.kastel.mcse.ardoco.core.api.agent.RecommendationAgentData;
import edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator.IInstanceRelation;
import edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator.IRecommendationState;
import edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator.IRecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.DependencyTag;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IWord;
import edu.kit.kastel.mcse.ardoco.core.api.data.textextraction.INounMapping;
import edu.kit.kastel.mcse.ardoco.core.common.util.WordHelper;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.InstanceRelation;

/**
 * Adds
 *
 * @see InstanceRelation instances to {@link InstanceRelationAgent#recommendationState}
 */
public class InstanceRelationAgent extends RecommendationAgent {

    /**
     * Default constructor
     */
    public InstanceRelationAgent() {
    }

    /**
     * Agent execution function
     */
    @Override
    public void execute(RecommendationAgentData data) {
        for (var model : data.getModelIds()) {
            getRelations(data.getRecommendationState(data.getModelState(model).getMetamodel()));
        }
    }

    /**
     * Search for relations between RecommendedInstances
     *
     * @param recommendationState
     * @return list of recommended relations
     */
    private void getRelations(IRecommendationState recommendationState) {
        for (IRecommendedInstance instance : recommendationState.getRecommendedInstances()) {
            processRecommendedInstance(recommendationState, instance);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Found {} InstanceRelations", recommendationState.getInstanceRelations().size());
        }
    }

    private void processRecommendedInstance(IRecommendationState recommendationState, IRecommendedInstance instance) {
        for (IWord word : getInstanceWords(instance)) {
            processVersOn(recommendationState, instance, word);
        }
    }

    private void processVersOn(IRecommendationState recommendationState, IRecommendedInstance instance, IWord word) {
        for (IWord verbOn : getVerbsOn(word)) {
            for (IWord secondWord : getNounDepOf(verbOn)) {
                if (word.getSentenceNo() != verbOn.getSentenceNo() || secondWord.getSentenceNo() != verbOn.getSentenceNo()) {
                    // If word, verbOn and secondWord are not in the same sentence, ignore
                    continue;
                }
                IWord from = word.getPosition() < secondWord.getPosition() ? word : secondWord;
                IWord to = word.getPosition() < secondWord.getPosition() ? secondWord : word;
                processWithSecondInstance(recommendationState, instance, word, verbOn, secondWord, from, to);
            }
        }
    }

    private void processWithSecondInstance(IRecommendationState recommendationState, IRecommendedInstance instance, IWord word, IWord verbOn, IWord secondWord,
            IWord from, IWord to) {
        for (IRecommendedInstance secondInstance : recommendationState.getRecommendedInstances()) {

            if (getInstanceWords(secondInstance).contains(secondWord) && !instance.equals(secondInstance) && !word.equals(secondWord)) {
                boolean newInstance = checkForNewInstance(recommendationState, instance, word, verbOn, secondWord, from, to, secondInstance);
                if (newInstance) {
                    /*
                     * Add new relation if not found previously
                     */
                    logger.debug("Add new InstanceRelation from {} over {} to {}", word.getText(), verbOn.getText(), secondWord.getText());
                    recommendationState.addInstanceRelation(instance, secondInstance, verbOn, Collections.singletonList(from), Collections.singletonList(to));
                }
            }
        }
    }

    private boolean checkForNewInstance(IRecommendationState recommendationState, IRecommendedInstance instance, IWord word, IWord verbOn, IWord secondWord,
            IWord from, IWord to, IRecommendedInstance secondInstance) {
        boolean newInstance = true;
        for (IInstanceRelation relation : recommendationState.getInstanceRelations()) {
            if (relation.isIn(verbOn, Collections.singletonList(from), Collections.singletonList(to))) {
                /*
                 * Break loop if relation already exists
                 */
                newInstance = false;
            } else if (relation.matches(instance, secondInstance)) {
                /*
                 * Add to existing if instances match, but specific relation does not exist there yet
                 */
                newInstance = false;
                logger.debug("Add to existing InstanceRelation from {} over {} to {}", word.getText(), verbOn.getText(), secondWord.getText());
                relation.addLink(verbOn, Collections.singletonList(from), Collections.singletonList(to));
            }
            if (!newInstance) {
                break;
            }
        }
        return newInstance;
    }

    private static List<IWord> getInstanceWords(IRecommendedInstance instance) {
        List<IWord> words = new ArrayList<>();
        for (INounMapping mapping : instance.getNameMappings()) {
            words.addAll(mapping.getWords().castToList());
        }
        return words;
    }

    private static List<IWord> getVerbsOn(IWord word) {
        return getNounDepOn(word).stream().filter(WordHelper::isVerb).toList();
    }

    private static List<IWord> getNounDepOn(IWord word) {
        List<IWord> dependencies = new ArrayList<>();
        dependencies.addAll(word.getWordsThatAreDependentOnThis(DependencyTag.OBJ).castToList());
        dependencies.addAll(word.getWordsThatAreDependentOnThis(DependencyTag.NSUBJ).castToList());
        return dependencies;
    }

    private static List<IWord> getNounDepOf(IWord word) {
        List<IWord> dependencies = new ArrayList<>();
        dependencies.addAll(word.getWordsThatAreDependencyOfThis(DependencyTag.OBJ).castToList());
        dependencies.addAll(word.getWordsThatAreDependencyOfThis(DependencyTag.NSUBJ).castToList());
        return dependencies;
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
    }
}
