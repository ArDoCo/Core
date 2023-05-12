/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("ClassUnit")
public class ClassUnit extends Datatype {

    @JsonProperty
    private final List<String> content;

    private ClassUnit() {
        // Jackson
        content = new ArrayList<>();
    }

    public ClassUnit(String name, Set<? extends CodeItem> content) {
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
