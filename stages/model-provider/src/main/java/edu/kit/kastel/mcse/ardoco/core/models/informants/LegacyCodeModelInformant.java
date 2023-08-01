/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.models.informants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.collections.api.factory.Lists;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelInstance;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelStates;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.ClassUnit;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeCompilationUnit;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeModule;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodePackage;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.InterfaceUnit;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.models.ModelExtractionStateImpl;
import edu.kit.kastel.mcse.ardoco.core.models.ModelInstanceImpl;
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
        List<ModelInstance> instances = new ArrayList<>();
        fillRecursive(codeModel, instances);
        models.addModelExtractionState(codeModel.getId(), new ModelExtractionStateImpl(codeModel.getId(), Metamodel.CODE, Lists.immutable.withAll(instances)));
    }

    private void fillRecursive(CodeModel codeModel, List<ModelInstance> instances) {
        for (var modelElement : codeModel.getAllPackages()) {
            String path = modelElement.getName();
            CodeModule parent = modelElement.getParent();
            while (parent != null) {
                path = parent.getName() + "/" + path;
                parent = parent.getParent();
            }
            // Ensure that package is handled as directory
            path += "/";
            instances.add(new ModelInstanceImpl(modelElement.getName(), "Package", path));
            fillRecursive(modelElement, instances);
        }
    }

    private void fillRecursive(CodePackage packageElement, List<ModelInstance> instances) {
        for (var unit : packageElement.getAllCompilationUnits()) {
            String type = findType(unit);
            instances.add(new ModelInstanceImpl(unit.getName(), type, unit.getPath()));
        }

    }

    private String findType(CodeCompilationUnit unit) {
        // Assumption mostly one class per unit
        var content = unit.getContent().stream().filter(it -> unit.getName().contains(it.getName())).findFirst().orElse(null);
        if (content instanceof ClassUnit) {
            return "Class";
        }
        if (content instanceof InterfaceUnit) {
            return "Interface";
        }
        if (unit.getPath().endsWith("package-info.java")) {
            return "PackageInfo";
        }
        if (unit.getPath().endsWith(".java")) {
            // Default to Class
            return "Class";
        }
        throw new IllegalStateException("Unknown type of CodeCompilationUnit");
    }

    private CodeModel findCodeModel(ModelStates models) {
        for (var modelId : models.modelIds()) {
            var model = models.getModel(modelId);
            if (model instanceof CodeModel codeModel)
                return codeModel;
        }
        return null;
    }
}
