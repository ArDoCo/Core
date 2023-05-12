/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("InterfaceUnit")
public class InterfaceUnit extends Datatype {

    @JsonProperty
    private List<String> content;

    private InterfaceUnit() {
        // Jackson
    }

    public InterfaceUnit(String name, Set<? extends CodeItem> content) {
        super(name);
        this.content = new ArrayList<>();
        for (var codeItem : content) {
            this.content.add(codeItem.getId());
        }
    }

    @JsonGetter("content")
    protected List<String> getContentIds() {
        return content;
    }

    @Override
    public List<CodeItem> getContent() {
        return CodeItemRepository.getInstance().getCodeItemsFromIds(content);
    }

    @Override
    public List<Datatype> getAllDataTypes() {
        List<Datatype> result = new ArrayList<>();
        result.add(this);
        getContent().forEach(c -> result.addAll(c.getAllDataTypes()));
        return result;
    }
}
