/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.pipeline.agent;

import java.util.SortedMap;

import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.AbstractPipelineStep;

public abstract class Informant extends AbstractPipelineStep implements Claimant {
    protected Informant(String id, DataRepository dataRepository) {
        super(id, dataRepository);
    }

    @Override
    protected void before() {
        //Nothing by default
    }

    @Override
    protected void after() {
        //Nothing by default
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(SortedMap<String, String> additionalConfiguration) {
        //FIXME Informants conceptually are at the very bottom of the hierarchy, currently 30+
        // classes override this method doing nothing because of that.
        //FIXME Since providing a base implementation in this class does not prevent subclasses
        // from overriding the method in fringe cases, this would reduce unnecessary code
        // duplication, without removing functionality
        //FIXME probably subclasses should be provided for informants that have subprocesses
        // (none as of now) and those which dont
    }
}
