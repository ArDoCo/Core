/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.models.informants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.*;

import org.eclipse.collections.api.factory.Lists;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelStates;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.models.ModelExtractionStateImpl;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;

public class LegacyCodeModelInformant extends Informant {
    public LegacyCodeModelInformant(DataRepository dataRepository) {
        super(LegacyCodeModelInformant.class.getSimpleName(), dataRepository);
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
        // empty
    }

    @Override
    public void run() {
        var models = DataRepositoryHelper.getModelStatesData(getDataRepository());
        var codeModel = findCodeModel(models);
        if (codeModel == null)
            return;
        List<CodeItem> codeItems = new ArrayList<>();
        fillPackages(codeModel.getAllPackages(), codeItems);
        fillCompilationUnits(codeModel.getEndpoints(), codeItems);
        models.addModelExtractionState(codeModel.getId(), new ModelExtractionStateImpl(codeModel.getId(), Metamodel.CODE, Lists.immutable.withAll(codeItems)));
    }

    private void fillPackages(Collection<? extends CodePackage> packages, List<CodeItem> codeItems) {
        for (var modelElement : packages) {
            codeItems.add(new CodePackage(new CodeItemRepository(), modelElement.getName()));
        }
    }

    private void fillCompilationUnits(Collection<? extends CodeCompilationUnit> units, List<CodeItem> codeItems) {
        for (var unit : units) {
            var content = unit.getContent().stream().filter(it -> unit.getName().contains(it.getName())).findFirst().orElse(null);
            codeItems.add(content);
        }

    }

//    private String findType(CodeCompilationUnit unit) { todo: remove
//        // Assumption mostly one class per unit
//        var content = unit.getContent().stream().filter(it -> unit.getName().contains(it.getName())).findFirst().orElse(null);
//        if (content instanceof ClassUnit) {
//            return "Class";
//        }
//        if (content instanceof InterfaceUnit) {
//            return "Interface";
//        }
//        if (unit.getPath().endsWith("package-info.java")) {
//            return "PackageInfo";
//        }
//        if (unit.getPath().endsWith(".java")) {
//            // Default to Class
//            return "Class";
//        }
//        if (unit.getLanguage() == ProgrammingLanguage.SHELL) {
//            return "ShellScript";
//        }
//        throw new IllegalStateException("Unknown type of CodeCompilationUnit");
//    }

    private CodeModel findCodeModel(ModelStates models) {
        for (var modelId : models.modelIds()) {
            var model = models.getModel(modelId);
            if (model instanceof CodeModel codeModel)
                return codeModel;
        }
        return null;
    }
}
