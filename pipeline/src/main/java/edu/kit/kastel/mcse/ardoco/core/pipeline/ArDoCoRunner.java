/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.pipeline;

import java.io.File;
import java.util.Objects;

import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;

record ArDoCoRunner(File inputText, File inputModelArchitecture, ArchitectureModelType inputArchitectureModelType, File inputModelCode, File additionalConfigs,
                    File outputDir, String name) {

    ArDoCoResult runArDoCo() {
        ArDoCo arDoCo = ArDoCo.getInstance(name);
        return arDoCo.runAndSave(name, inputText, inputModelArchitecture, inputArchitectureModelType, inputModelCode, additionalConfigs, outputDir);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof ArDoCoRunner other))
            return false;
        return Objects.equals(this.inputText, other.inputText) && Objects.equals(this.inputModelArchitecture, other.inputModelArchitecture) && Objects.equals(
                this.inputArchitectureModelType, other.inputArchitectureModelType) && Objects.equals(this.inputModelCode, other.inputModelCode) && Objects
                        .equals(this.additionalConfigs, other.additionalConfigs) && Objects.equals(this.outputDir, other.outputDir) && Objects.equals(this.name,
                                other.name);
    }

    @Override
    public String toString() {
        return "ArDoCoRunArguments[" + "inputText=" + inputText + ", " + "inputModelArchitecture=" + inputModelArchitecture + ", " + "inputModelCode=" + inputModelCode + ", " + "additionalConfigs=" + additionalConfigs + ", " + "outputDir=" + outputDir + ", " + "name=" + name + ']';
    }

}
