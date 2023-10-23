package edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.data.diagram;

/**
 * A line that is part of a {@link Diagram}.
 *
 * @param source
 *         The start of the line.
 * @param target
 *         The end of the line.
 */
public record Line(Box source, Box target) {

}
