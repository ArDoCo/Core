/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.model.pcm;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.fuchss.xmlobjectmapper.XML2Object;

import edu.kit.kastel.mcse.ardoco.core.api.data.model.IModelInstance;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.IModelRelation;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.model.IModelConnector;
import edu.kit.kastel.mcse.ardoco.core.model.Instance;
import edu.kit.kastel.mcse.ardoco.core.model.pcm.xml.PCMComponent;
import edu.kit.kastel.mcse.ardoco.core.model.pcm.xml.PCMRepository;

public class PcmXMLModelConnector implements IModelConnector {

    private final PCMRepository repository;

    public PcmXMLModelConnector(File file) throws IOException, ReflectiveOperationException {
        this(new FileInputStream(file));
    }

    public PcmXMLModelConnector(InputStream is) throws ReflectiveOperationException, IOException {
        var xom = new XML2Object();
        xom.registerClasses(PCMRepository.class, PCMComponent.class);
        this.repository = Objects.requireNonNull(xom.parseXML(is, PCMRepository.class));
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
    public ImmutableList<IModelInstance> getInstances() {
        return repository.getComponents().collect(e -> new Instance(e.getEntityName(), e.getType(), e.getId()));
    }

    @Override
    public ImmutableList<IModelRelation> getRelations() {
        // NOT YET IMPLEMENTED!
        return Lists.immutable.empty();
    }
}
