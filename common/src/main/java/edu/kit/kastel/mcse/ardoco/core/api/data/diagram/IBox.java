/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.api.data.diagram;

import java.awt.*;
import java.util.List;
import java.util.Map;

public interface IBox {
    List<String> getWordsThatBelongToThisBox();

    Map<Color, List<String>> getWordsThatBelongToThisBoxGroupedByColor();

    Color getDominatingColor();
}
