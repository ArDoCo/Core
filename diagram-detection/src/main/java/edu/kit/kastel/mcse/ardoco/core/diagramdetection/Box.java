/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.diagramdetection;

import edu.kit.kastel.mcse.ardoco.core.api.data.diagram.IBox;

import java.util.ArrayList;
import java.util.List;

class Box implements IBox {
    private final List<String> words;

    public Box(List<String> words) {
        this.words = new ArrayList<>(words);
    }

    @Override
    public List<String> getWordsThatBelongToThisBox() {
        return new ArrayList<>(words);
    }
}
