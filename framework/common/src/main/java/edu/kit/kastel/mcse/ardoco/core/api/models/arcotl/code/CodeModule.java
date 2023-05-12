/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({ //
        @JsonSubTypes.Type(value = CodeAssembly.class, name = "CodeAssembly"),//
        @JsonSubTypes.Type(value = CodeCompilationUnit.class, name = "CodeCompilationUnit"), //
        @JsonSubTypes.Type(value = CodePackage.class, name = "CodePackage"), //
})
@JsonTypeName("CodeModule")
public class CodeModule extends CodeItem {

    @JsonProperty
    private String parentId;
    @JsonProperty
    private List<String> content;

    CodeModule() {
        // Jackson
    }

    public CodeModule(String name, Set<? extends CodeItem> content) {
        super(name);
        this.content = new ArrayList<>();
        for (var codeItem : content) {
            this.content.add(codeItem.getId());
        }
        parentId = null;
    }

    @JsonGetter("content")
    protected List<String> getContentIds() {
        return content;
    }

    @Override
    public List<CodeItem> getContent() {
        return CodeItemRepository.getInstance().getCodeItemsFromIds(content);
    }

    public void setContent(List<? extends CodeItem> content) {
        this.content = new ArrayList<>();
        for (var codeItem : content) {
            this.content.add(codeItem.getId());
        }
    }

    public void addContent(CodeItem content) {
        this.content.add(content.getId());
    }

    public void addContent(List<? extends CodeItem> content) {
        this.content.addAll(content.stream().map(CodeItem::getId).toList());
    }

    public CodeModule getParent() {
        CodeItem codeItem = CodeItemRepository.getInstance().getCodeItem(parentId);
        if (codeItem instanceof CodeModule codeModule) {
            return codeModule;
        }
        return null;
    }

    public boolean hasParent() {
        return getParent() != null;
    }

    public void setParent(CodeModule parent) {
        this.parentId = parent.getId();
        if (!CodeItemRepository.getInstance().containsCodeItem(parentId)) {
            CodeItemRepository.getInstance().addCodeItem(parent);
        }
    }

    @Override
    public Set<CodeCompilationUnit> getAllCompilationUnits() {
        Set<CodeCompilationUnit> result = new HashSet<>();
        getContent().forEach(c -> result.addAll(c.getAllCompilationUnits()));
        return result;
    }

    @Override
    public Set<? extends CodePackage> getAllPackages() {
        Set<CodePackage> result = new HashSet<>();
        getContent().forEach(c -> result.addAll(c.getAllPackages()));
        return result;
    }
}
