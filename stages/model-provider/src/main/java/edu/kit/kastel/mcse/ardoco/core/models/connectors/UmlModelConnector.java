/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.models.connectors;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.mcse.ardoco.core.api.models.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelConnector;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelInstance;
import edu.kit.kastel.mcse.ardoco.core.models.ModelInstanceImpl;
import edu.kit.kastel.mcse.ardoco.core.models.connectors.generators.architecture.uml.parser.UmlModel;
import edu.kit.kastel.mcse.ardoco.core.models.connectors.generators.architecture.uml.parser.UmlModelRoot;

public class UmlModelConnector implements ModelConnector {
    private final UmlModelRoot model;

    public UmlModelConnector(File file) throws IOException {
        this(new FileInputStream(file));
    }

    public UmlModelConnector(InputStream is) {
        UmlModel umlModel = new UmlModel(is);
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
