/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.informalin.framework.models.uml.UMLModel;
import edu.kit.kastel.informalin.framework.models.uml.UMLModelRoot;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelConnector;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelInstance;

public class UMLModelConnector implements ModelConnector {
    private final UMLModelRoot model;

    public UMLModelConnector(File file) throws IOException {
        this(new FileInputStream(file));
    }

    public UMLModelConnector(InputStream is) {
        UMLModel umlModel = new UMLModel(is);
        this.model = Objects.requireNonNull(umlModel.getModel());
    }

    @Override
    public String getModelId() {
        return model.getId();
    }

    @Override
    public Metamodel getMetamodel() {
        return Metamodel.ARCHITECTURE;
    }

    @Override
    public ImmutableList<ModelInstance> getInstances() {
        return Lists.immutable.withAll(model.getComponents()).collect(c -> new ModelInstanceImpl(c.getName(), "Component", c.getId()));
    }
}
