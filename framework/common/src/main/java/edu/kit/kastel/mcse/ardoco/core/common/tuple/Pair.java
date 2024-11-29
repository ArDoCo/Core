/* Licensed under MIT 2022-2024. */
package edu.kit.kastel.mcse.ardoco.core.common.tuple;

import java.io.Serializable;

import com.github.jsonldjava.shaded.com.google.common.base.Objects;

public record Pair<T extends Serializable, U extends Serializable>(T first, U second) implements Serializable {

    public boolean hasElement(Serializable element) {
        return Objects.equal(this.first, element) || Objects.equal(this.second, element);
    }

    public Serializable getOtherElement(Serializable element) {
        if (Objects.equal(this.first, element)) {
            return this.second;
        }
        if (Objects.equal(this.second, element)) {
            return this.first;
        }
        throw new IllegalArgumentException("Unknown element: " + element);
    }

}
