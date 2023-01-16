/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.api.data.diagram;

import java.awt.*;
import java.util.List;
import java.util.Map;

public interface Box {
    List<String> getWordsThatBelongToThisBox();

    Map<Color, List<String>> getWordsThatBelongToThisBoxGroupedByColor();

    Color getDominatingColor();
}
