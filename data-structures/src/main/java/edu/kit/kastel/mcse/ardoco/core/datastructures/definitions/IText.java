package edu.kit.kastel.mcse.ardoco.core.datastructures.definitions;

import java.util.List;

public interface IText {

    IWord getStartNode();

    default int getLength() {
        return getWords().size();
    }

    List<IWord> getWords();
}
