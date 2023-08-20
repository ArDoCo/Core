/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.common.tuple;

import java.io.Serializable;

public record Pair<T, U>(T first, U second) implements Serializable {
}
