/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.api.data;

import edu.kit.kastel.informalin.data.PipelineStepData;

/**
 * {@link ProjectPipelineData} represents data that we know overall about the project such as the name of the project.
 */
public interface ProjectPipelineData extends PipelineStepData {
    String ID = "ProjectPipelineData";

    /**
     * Return the project name
     * 
     * @return the project name
     */
    String getProjectName();

}
