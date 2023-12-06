/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.SortedMap;

import org.eclipse.collections.api.factory.SortedMaps;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CodeItemRepository implements Serializable {

    @JsonProperty
    private final SortedMap<String, CodeItem> repository = SortedMaps.mutable.empty();

    public SortedMap<String, CodeItem> getRepository() {
        return repository;
    }

    void addCodeItem(CodeItem codeItem) {
        repository.put(codeItem.getId(), codeItem);
    }

    boolean containsCodeItem(String id) {
        return repository.containsKey(id);
    }

    CodeItem getCodeItem(String id) {
        if (id == null)
            return null;
        return repository.get(id);
    }

    public List<CodeItem> getCodeItemsFromIds(List<String> codeItemIds) {
        return codeItemIds.stream().map(this::getCodeItem).filter(Objects::nonNull).toList();
    }

    public void init() {
        this.repository.values().forEach(it -> it.registerCurrentCodeItemRepository(this));
    }
}
