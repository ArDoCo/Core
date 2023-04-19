/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.execution;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.kit.kastel.mcse.ardoco.core.api.data.model.ArchitectureModelType;
import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;

/**
 * Record that sets up ArDoCo and can run it. This serves as a simple intermediate object to easy constructing, running, and testing ArDoCo. The {@link Builder}
 * within this record helps with construction.
 *
 * @param inputText                  the input text
 * @param inputModelArchitecture     the input model architecture
 * @param inputArchitectureModelType the architecture type (e.g., PCM, UML)
 * @param inputModelCode             the input model code (can be null)
 * @param additionalConfigs          the additional configs
 * @param outputDir                  the output directory
 * @param name                       the name of the project to run ArDoCo on
 */
public record ArDoCoRunner(File inputText, File inputModelArchitecture, ArchitectureModelType inputArchitectureModelType, File inputModelCode,
                           File additionalConfigs, File outputDir, String name) {

    private static final Logger logger = LoggerFactory.getLogger(ArDoCoRunner.class);

    /**
     * Run ArDoCo
     *
     * @return the result after running ArDoCo
     */
    public ArDoCoResult runArDoCo() {
        ArDoCo arDoCo = ArDoCo.getInstance(name);
        var additionalConfigsMap = ConfigurationHelper.loadAdditionalConfigs(additionalConfigs);
        try {
            arDoCo.definePipeline(inputText, inputModelArchitecture, inputArchitectureModelType, inputModelCode, additionalConfigsMap);
        } catch (IOException e) {
            logger.error("Problem in initialising pipeline when loading data (IOException)", e.getCause());
            return null;
        }
        return arDoCo.runAndSave(null);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof ArDoCoRunner other))
            return false;
        return Objects.equals(this.inputText, other.inputText) && Objects.equals(this.inputModelArchitecture, other.inputModelArchitecture) && Objects.equals(
                this.inputArchitectureModelType, other.inputArchitectureModelType) && Objects.equals(this.inputModelCode,
                other.inputModelCode) && Objects.equals(this.additionalConfigs, other.additionalConfigs) && Objects.equals(this.outputDir,
                other.outputDir) && Objects.equals(this.name, other.name);
    }

    @Override
    public String toString() {
        return "ArDoCoRunArguments[" + "inputText=" + inputText + ", " + "inputModelArchitecture=" + inputModelArchitecture + ", " + "inputModelCode=" + inputModelCode + ", " + "additionalConfigs=" + additionalConfigs + ", " + "outputDir=" + outputDir + ", " + "name=" + name + ']';
    }

    /**
     * Builder to set up ArDoCo in an easy way.
     */
    public static class Builder {
        private final String name;
        private File inputText = null;
        private File inputModelArchitecture = null;
        private ArchitectureModelType inputArchitectureModelType = null;
        private File inputModelCode = null;
        private File additionalConfigs = null;
        private File outputDir = null;

        private List<String> optionalFields = List.of("inputModelCode", "additionalConfigs");

        /**
         * Create the builder
         *
         * @param name the name of the project
         */
        public Builder(String name) {
            this.name = name;
        }

        /**
         * Build ArDoCo with all the previously set configuration options/inputs. Checks if all necessary options are set.
         *
         * @return An {@link ArDoCoRunner} with the set options/inputs
         * @throws IllegalStateException If necessary options are not set
         */
        public ArDoCoRunner build() throws IllegalStateException {
            for (Field field : this.getClass().getDeclaredFields()) {
                if (optionalFields.contains(field.getName())) {
                    continue;
                }
                try {
                    if (Objects.isNull(field.get(this))) {
                        String fieldName = field.getName();
                        throw new IllegalStateException("Cannot invoke build() because " + fieldName + " is null.");
                    }
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException("Cannot access field.");
                }
            }
            return new ArDoCoRunner(inputText, inputModelArchitecture, inputArchitectureModelType, inputModelCode, additionalConfigs, outputDir, name);
        }

        /**
         * Add input text file
         *
         * @param inputText the input text file
         * @return the builder
         */
        public Builder withInputText(File inputText) {
            this.inputText = inputText;
            return this;
        }

        /**
         * Add the string that represents a path to the input text file
         *
         * @param inputTextPath the file path as string
         * @return the builder
         */
        public Builder withInputText(String inputTextPath) {
            this.inputText = new File(inputTextPath);
            return this;
        }

        /**
         * Add the input architecture model file
         *
         * @param inputModelArchitecture the input architecture model file
         * @return the builder
         */
        public Builder withInputModelArchitecture(File inputModelArchitecture) {
            this.inputModelArchitecture = inputModelArchitecture;
            return this;
        }

        /**
         * Add the string that represents a path to the input architecture model file
         *
         * @param inputModelArchitecturePath the path string to the input architecture model file
         * @return the builder
         */
        public Builder withInputModelArchitecture(String inputModelArchitecturePath) {
            this.inputModelArchitecture = new File(inputModelArchitecturePath);
            return this;
        }

        /**
         * Add the architecture model type
         *
         * @param inputArchitectureModelType the architecture model type
         * @return the builder
         */
        public Builder withInputArchitectureModelType(ArchitectureModelType inputArchitectureModelType) {
            this.inputArchitectureModelType = inputArchitectureModelType;
            return this;
        }

        /**
         * Sets PCM as architecture model type
         *
         * @return the builder
         */
        public Builder withPcmModelType() {
            this.inputArchitectureModelType = ArchitectureModelType.PCM;
            return this;
        }

        /**
         * Set UML as architecture model type
         *
         * @return the builder
         */
        public Builder withUmlModelType() {
            this.inputArchitectureModelType = ArchitectureModelType.UML;
            return this;
        }

        /**
         * Set the output directory to the provided file
         *
         * @param outputDir the file that represents the output directory
         * @return the builder
         */
        public Builder withOutputDir(File outputDir) {
            this.outputDir = outputDir;
            return this;
        }

        /**
         * Set the output directory to the string that represents the desired path
         *
         * @param outputDir the string that represents the path of the desired output directory
         * @return the builder
         */
        public Builder withOutputDir(String outputDir) {
            this.outputDir = new File(outputDir);
            return this;
        }

        /**
         * Sets the input model code
         *
         * @param inputModelCode the input model code file
         * @return the builder
         */
        public Builder withInputModelCode(File inputModelCode) {
            this.inputModelCode = inputModelCode;
            return this;
        }

        /**
         * Sets the input model code to the provided path (as String)
         *
         * @param inputModelCodePath the input model code file as string-based path
         * @return the builder
         */
        public Builder withInputModelCode(String inputModelCodePath) {
            this.inputModelCode = new File(inputModelCodePath);
            return this;
        }

        /**
         * Sets the additional configs to the provided config file
         *
         * @param additionalConfigs the file with additional configs
         * @return the builder
         */
        public Builder withAdditionalConfigs(File additionalConfigs) {
            this.additionalConfigs = additionalConfigs;
            return this;
        }

        /**
         * Sets the additional configs to the config file that is provided as string path.
         *
         * @param additionalConfigsPath the path as string to the file with additional configs
         * @return the builder
         */
        public Builder withAdditionalConfigs(String additionalConfigsPath) {
            this.additionalConfigs = new File(additionalConfigsPath);
            return this;
        }

        /**
         * Sets the additional configs to the configs provided with the Key-Value map.
         *
         * @param additionalConfigs the additional config Key-value map
         * @return the builder
         * @throws IOException If the file system cannot be used for temporary file saving
         */
        public Builder withAdditionalConfigs(Map<String, String> additionalConfigs) throws IOException {
            File temporaryAdditionalConfigsFile = getTemporaryAdditionalConfigsFile();
            temporaryAdditionalConfigsFile.deleteOnExit();

            for (var entry : additionalConfigs.entrySet()) {
                String configOption = entry.getKey() + "=" + entry.getValue() + "\n";
                Files.writeString(temporaryAdditionalConfigsFile.toPath(), configOption, StandardOpenOption.APPEND);
            }

            this.additionalConfigs = temporaryAdditionalConfigsFile;
            return this;
        }

        private static File getTemporaryAdditionalConfigsFile() throws IOException {
            String prefix = ".additionalConfigs";
            String suffix = "tmp";

            File temporaryAdditionalConfigsFile;
            if (SystemUtils.IS_OS_UNIX) {
                FileAttribute<Set<PosixFilePermission>> attr = PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwx------"));
                temporaryAdditionalConfigsFile = Files.createTempFile(prefix, suffix, attr).toFile();
            } else {
                temporaryAdditionalConfigsFile = Files.createTempFile(prefix, suffix).toFile();
                boolean readableOk = temporaryAdditionalConfigsFile.setReadable(true, true);
                boolean writeableOk = temporaryAdditionalConfigsFile.setWritable(true, true);
                boolean executableOk = temporaryAdditionalConfigsFile.setExecutable(true, true);
                if (!readableOk || !writeableOk || !executableOk) {
                    logger.warn("Problems occurred when creating temporary file permissions.");
                }
            }
            return temporaryAdditionalConfigsFile;
        }
    }

}
