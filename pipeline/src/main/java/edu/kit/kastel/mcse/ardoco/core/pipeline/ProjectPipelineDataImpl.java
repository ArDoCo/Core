/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.pipeline;

import edu.kit.kastel.mcse.ardoco.core.api.data.ProjectPipelineData;

/**
 * Implementation of {@link ProjectPipelineData} that simply takes the project's name in the constructor to store it.
 */
public class ProjectPipelineDataImpl implements ProjectPipelineData {

    private final String projectName;

    /**
     * Construct this class using the project's name
     * 
     * @param projectName the project's name
     */
    ProjectPipelineDataImpl(String projectName) {
        super();
        this.projectName = projectName;
    }

    @Override
    public String getProjectName() {
        return projectName;
    }
}
