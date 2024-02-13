/* Licensed under MIT 2022-2024. */
package edu.kit.kastel.mcse.ardoco.core.common.tuple;

import java.io.Serializable;

public record Pair<T extends Serializable, U extends Serializable>(T first, U second) implements Serializable {
}
