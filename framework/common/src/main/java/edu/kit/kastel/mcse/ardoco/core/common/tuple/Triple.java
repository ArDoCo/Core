/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.common.tuple;

import java.io.Serializable;

public record Triple<T, U, V>(T first, U second, V third) implements Serializable {
}
