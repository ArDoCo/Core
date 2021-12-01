/* Licensed under MIT 2021. */
package edu.kit.kastel.mcse.ardoco.core.recommendationgenerator;

import java.util.List;

import edu.kit.kastel.mcse.ardoco.core.text.IWord;

public interface IInstanceRelation {
    IInstanceRelation createCopy();

    boolean addLink(IWord relator, List<IWord> from, List<IWord> to);

    boolean matches(IRecommendedInstance fromInstance, IRecommendedInstance toInstance);

    boolean isIn(IWord relator, List<IWord> from, List<IWord> to);

    double getProbability();

    int getSize();

    void setProbability(double newProbability);

    IRecommendedInstance getFromInstance();

    IRecommendedInstance getToInstance();
}
