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
import edu.kit.kastel.mcse.ardoco.core.models.connectors.generators.architecture.pcm.parser.PcmModel;
import edu.kit.kastel.mcse.ardoco.core.models.connectors.generators.architecture.pcm.parser.PcmRepository;

public class PcmXmlModelConnector implements ModelConnector {

    private final PcmRepository repository;

    public PcmXmlModelConnector(File file) throws IOException {
        this(new FileInputStream(file));
    }

    public PcmXmlModelConnector(InputStream is) {
        PcmModel pcmModel = new PcmModel(is);
        this.repository = Objects.requireNonNull(pcmModel.getRepository());
    }

    @Override
    public String getModelId() {
        return repository.getId();
    }

    @Override
    public Metamodel getMetamodel() {
        return Metamodel.ARCHITECTURE;
    }

    @Override
    public ImmutableList<ModelInstance> getInstances() {
        return Lists.immutable.withAll(repository.getComponents()).collect(e -> new ModelInstanceImpl(e.getEntityName(), e.getType(), e.getId()));
    }
}
