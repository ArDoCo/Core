/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.api.agent.IAgent;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IWord;

public interface IInstanceRelation {
    IInstanceRelation createCopy();

    boolean addLink(IWord relator, List<IWord> from, List<IWord> to, IAgent<?> claimant);

    boolean matches(IRecommendedInstance fromInstance, IRecommendedInstance toInstance);

    boolean isIn(IWord relator, List<IWord> from, List<IWord> to);

    double getProbability();

    int getSize();

    void setProbability(IAgent<?> claimant, double newProbability);

    IRecommendedInstance getFromInstance();

    IRecommendedInstance getToInstance();
}
