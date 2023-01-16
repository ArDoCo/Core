/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.diagramdetection;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import edu.kit.kastel.informalin.framework.common.JavaUtils;

class Box implements edu.kit.kastel.mcse.ardoco.core.api.data.diagram.Box {

    private final Color dominatingColor;
    private final Map<Color, List<String>> words;

    public Box(Color dominatingColor, Map<Color, List<String>> words) {
        this.dominatingColor = dominatingColor;
        this.words = JavaUtils.copyMap(words, ArrayList::new);
    }

    @Override
    public List<String> getWordsThatBelongToThisBox() {
        return words.values().stream().flatMap(Collection::stream).toList();
    }

    @Override
    public Color getDominatingColor() {
        return dominatingColor;
    }

    @Override
    public Map<Color, List<String>> getWordsThatBelongToThisBoxGroupedByColor() {
        return JavaUtils.copyMap(words, ArrayList::new);
    }
}
