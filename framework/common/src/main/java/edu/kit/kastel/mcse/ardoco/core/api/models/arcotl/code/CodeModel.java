/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @JsonIgnore
    private boolean initialized;

    private CodeModel() {
        // Jackson
        this.initialized = false;
    }

    /**
     * Creates a new code model that is a CMTL instance. The model has the specified code items as content.
     *
     * @param content the content of the code model
     */
    public CodeModel(CodeItemRepository codeItemRepository, Set<? extends CodeItem> content) {
        this.initialized = true;
        this.codeItemRepository = codeItemRepository;
        this.content = new ArrayList<>();
        for (var codeItem : content) {
            this.content.add(codeItem.getId());
        }
    }

    @JsonGetter("content")
    protected List<String> getContentIds() {
        if (!initialized)
            initialize();
        return content;
    }

    @Override
    public List<? extends CodeItem> getContent() {
        if (!initialized)
            initialize();
        return codeItemRepository.getCodeItemsFromIds(content);
    }

    @Override
    public List<? extends CodeCompilationUnit> getEndpoints() {
        if (!initialized)
            initialize();
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
        if (!initialized)
            initialize();
        Set<CodePackage> codePackages = new HashSet<>();
        getContent().forEach(c -> codePackages.addAll(c.getAllPackages()));
        return codePackages;
    }

    private void initialize() {
        this.codeItemRepository.init();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof CodeModel codeModel))
            return false;
        if (!super.equals(o))
            return false;

        if (!Objects.equals(codeItemRepository, codeModel.codeItemRepository))
            return false;
        return Objects.equals(content, codeModel.content);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (codeItemRepository != null ? codeItemRepository.hashCode() : 0);
        result = 31 * result + (content != null ? content.hashCode() : 0);
        return result;
    }
}
