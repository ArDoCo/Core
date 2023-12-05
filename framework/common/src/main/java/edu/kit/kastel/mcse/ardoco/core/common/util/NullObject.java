/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.common.util;

import java.io.Serializable;

public class NullObject<T extends Serializable> implements Serializable {
    public final T value;

    public NullObject(T value) {
        this.value = value;
    }
}
