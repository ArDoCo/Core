/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.models.code;

import java.util.HashSet;
import java.util.Set;

import edu.kit.kastel.mcse.ardoco.core.api.models.Model;

/**
 * A code model that is a CMTL instance.
 */
public class CodeModel extends Model {

    private Set<? extends CodeItem> content;

    /**
     * Creates a new code model that is a CMTL instance. The model has the specified code items as content.
     *
     * @param content the content of the code model
     */
    public CodeModel(Set<? extends CodeItem> content) {
        this.content = content;
    }

    @Override
    public Set<? extends CodeItem> getContent() {
        return content;
    }

    @Override
    public Set<? extends CodeCompilationUnit> getEndpoints() {
        Set<CodeCompilationUnit> compilationUnits = new HashSet<>();
        content.forEach(c -> compilationUnits.addAll(c.getAllCompilationUnits()));
        return compilationUnits;
    }

    /**
     * Returns all code packages directly or indirectly owned by this code model.
     *
     * @return all code packages of this code model
     */
    public Set<? extends CodePackage> getAllPackages() {
        Set<CodePackage> codePackages = new HashSet<>();
        content.forEach(c -> codePackages.addAll(c.getAllPackages()));
        return codePackages;
    }
}
