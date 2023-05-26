/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.collections.api.factory.Maps;

import com.fasterxml.jackson.annotation.JsonProperty;

class CodeItemRepository {
    private static CodeItemRepository instance;

    @JsonProperty
    private final Map<String, CodeItem> repository = Maps.mutable.empty();

    private CodeItemRepository() {
        instance = this;
    }

    static synchronized CodeItemRepository getInstance() {
        if (instance == null) {
            instance = new CodeItemRepository();
        }

        return instance;
    }

    public Map<String, CodeItem> getRepository() {
        return repository;
    }

    void addCodeItem(CodeItem codeItem) {
        repository.put(codeItem.getId(), codeItem);
    }

    boolean containsCodeItem(String id) {
        return repository.containsKey(id);
    }

    CodeItem getCodeItem(String id) {
        return repository.get(id);
    }

    List<CodeItem> getCodeItemsFromIds(List<String> codeItemIds) {
        return codeItemIds.stream().map(this::getCodeItem).filter(Objects::nonNull).toList();
    }
}
