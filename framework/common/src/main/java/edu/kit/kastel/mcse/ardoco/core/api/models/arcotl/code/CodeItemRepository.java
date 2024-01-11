/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CodeItemRepository implements Serializable {

    @JsonProperty
    private SortedMap<String, CodeItem> repository = new TreeMap<>();
    @JsonIgnore
    private boolean initialized = false;

    public SortedMap<String, CodeItem> getRepository() {
        return new TreeMap<>(repository);
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

    public synchronized void init() {
        if (initialized)
            return;
        this.repository.values().forEach(it -> it.registerCurrentCodeItemRepository(this));
        initialized = true;
    }
}
