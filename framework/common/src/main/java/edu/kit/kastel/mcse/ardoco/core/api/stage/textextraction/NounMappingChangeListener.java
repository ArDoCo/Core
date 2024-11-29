/* Licensed under MIT 2022-2024. */
package edu.kit.kastel.mcse.ardoco.core.api.stage.textextraction;

import java.io.Serializable;

public interface NounMappingChangeListener extends Serializable {
    void onDelete(NounMapping deletedNounMapping, NounMapping replacement);
}
