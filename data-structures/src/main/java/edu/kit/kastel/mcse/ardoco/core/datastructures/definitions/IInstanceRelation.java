package edu.kit.kastel.mcse.ardoco.core.datastructures.definitions;

import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IRecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IWord;

import java.util.List;

public interface IInstanceRelation {
    IInstanceRelation createCopy();

    boolean addLink(IWord relator, List<IWord> from, List<IWord> to);

    boolean matches(IRecommendedInstance fromInstance, IRecommendedInstance toInstance);

    boolean isIn(IWord relator, List<IWord> from, List<IWord> to);

    double getProbability();

    void setProbability(double newProbability);

    IRecommendedInstance getFromInstance();

    IRecommendedInstance getToInstance();
}
