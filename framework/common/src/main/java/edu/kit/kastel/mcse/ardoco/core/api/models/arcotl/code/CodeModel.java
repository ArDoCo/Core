/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.Model;

/**
 * A code model that is a CMTL instance.
 */
public class CodeModel extends Model {

    @JsonProperty
    private CodeItemRepository codeItemRepository;

    @JsonProperty
    private List<String> content;

    private CodeModel() {
        // Jackson
    }

    /**
     * Creates a new code model that is a CMTL instance. The model has the specified code items as content.
     *
     * @param content the content of the code model
     */
    public CodeModel(Set<? extends CodeItem> content) {
        this.content = new ArrayList<>();
        for (var codeItem : content) {
            this.content.add(codeItem.getId());
        }
        codeItemRepository = CodeItemRepository.getInstance();
    }

    @JsonGetter("content")
    protected List<String> getContentIds() {
        return content;
    }

    @Override
    public List<? extends CodeItem> getContent() {
        return codeItemRepository.getCodeItemsFromIds(content);
    }

    @Override
    public List<? extends CodeCompilationUnit> getEndpoints() {
        List<CodeCompilationUnit> compilationUnits = new ArrayList<>();
        getContent().forEach(c -> compilationUnits.addAll(c.getAllCompilationUnits()));
        return compilationUnits;
    }

    /**
     * Returns all code packages directly or indirectly owned by this code model.
     *
     * @return all code packages of this code model
     */
    public Set<? extends CodePackage> getAllPackages() {
        Set<CodePackage> codePackages = new HashSet<>();
        getContent().forEach(c -> codePackages.addAll(c.getAllPackages()));
        return codePackages;
    }
}
