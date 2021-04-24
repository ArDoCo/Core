package edu.kit.kastel.mcse.ardoco.core.datastructures.definitions;

import java.util.List;

public interface IWord {

    int getSentenceNo();

    String getText();

    PosTag getPosTag();

    IWord getPreWord();

    IWord getNextWord();

    int getPosition();

    String getLemma();

    List<IWord> getWordsThatAreDependencyOfThis(DependencyTag dependencyTag);

    List<IWord> getWordsThatAreDependentOnThis(DependencyTag dependencyTag);

}
