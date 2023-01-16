/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.api.data.textextraction;

public interface NounMappingChangeListener {
    void onDelete(NounMapping deletedNounMapping, NounMapping replacement);
}
