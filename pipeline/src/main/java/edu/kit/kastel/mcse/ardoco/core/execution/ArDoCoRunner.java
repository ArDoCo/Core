/* Licensed under MIT 2022. */
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

import edu.kit.kastel.mcse.ardoco.core.api.output.ArDoCoResult;

public record ArDoCoRunner(File inputText, File inputModelArchitecture, ArchitectureModelType inputArchitectureModelType, File inputModelCode,
                           File additionalConfigs, File outputDir, String name) {

    private static final Logger logger = LoggerFactory.getLogger(ArDoCoRunner.class);

    public ArDoCoResult runArDoCo() {
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

    public static class Builder {
        private final String name;
        private File inputText = null;
        private File inputModelArchitecture = null;
        private ArchitectureModelType inputArchitectureModelType = null;
        private File inputModelCode = null;
        private File additionalConfigs = null;
        private File outputDir = null;

        private List<String> optionalFields = List.of("inputModelCode", "additionalConfigs");

        public Builder(String name) {
            this.name = name;
        }

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

        public Builder withInputText(File inputText) {
            this.inputText = inputText;
            return this;
        }

        public Builder withInputText(String inputTextPath) {
            this.inputText = new File(inputTextPath);
            return this;
        }

        public Builder withInputModelArchitecture(File inputModelArchitecture) {
            this.inputModelArchitecture = inputModelArchitecture;
            return this;
        }

        public Builder withInputModelArchitecture(String inputModelArchitecturePath) {
            this.inputModelArchitecture = new File(inputModelArchitecturePath);
            return this;
        }

        public Builder withInputArchitectureModelType(ArchitectureModelType inputArchitectureModelType) {
            this.inputArchitectureModelType = inputArchitectureModelType;
            return this;
        }

        public Builder withPcmModelType() {
            this.inputArchitectureModelType = ArchitectureModelType.PCM;
            return this;
        }

        public Builder withUmlModelType() {
            this.inputArchitectureModelType = ArchitectureModelType.UML;
            return this;
        }

        public Builder withOutputDir(File outputDir) {
            this.outputDir = outputDir;
            return this;
        }

        public Builder withOutputDir(String outputDir) {
            this.outputDir = new File(outputDir);
            return this;
        }

        public Builder withInputModelCode(File inputModelCode) {
            this.inputModelCode = inputModelCode;
            return this;
        }

        public Builder withInputModelCode(String inputModelCodePath) {
            this.inputModelCode = new File(inputModelCodePath);
            return this;
        }

        public Builder withAdditionalConfigs(File additionalConfigs) {
            this.additionalConfigs = additionalConfigs;
            return this;
        }

        public Builder withAdditionalConfigs(String additionalConfigsPath) {
            this.additionalConfigs = new File(additionalConfigsPath);
            return this;
        }

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
