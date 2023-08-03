/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.pipeline.agent;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractPipelineStep;

public abstract class Informant extends AbstractPipelineStep implements Claimant {
    protected Informant(String id, DataRepository dataRepository) {
        super(id, dataRepository);
    }

    protected Informant() {
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(Map<String, String> additionalConfiguration) {
        //FIXME Informants conceptually are at the very bottom of the hierarchy, currently 30+ classes override this method doing nothing because of that.
        //FIXME Since providing a base implementation in this class does not prevent subclasses from overriding the method in fringe cases, this would reduce unnecessary code duplication, without removing functionality
    }

    @Serial
    private void writeObject(ObjectOutputStream objectOutputStream) throws IOException {
        objectOutputStream.defaultWriteObject();
        objectOutputStream.writeObject(getId());
        objectOutputStream.writeObject(getDataRepository());
    }

    @Serial
    private void readObject(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
        objectInputStream.defaultReadObject();
        this.id = (String) objectInputStream.readObject();
        this.dataRepository = (DataRepository) objectInputStream.readObject();
    }
}
