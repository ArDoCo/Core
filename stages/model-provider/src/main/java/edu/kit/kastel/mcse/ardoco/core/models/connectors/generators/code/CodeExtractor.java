/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.models.connectors.generators.code;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import edu.kit.kastel.mcse.ardoco.core.api.models.CodeModelType;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelType;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.common.JsonUtils;
import edu.kit.kastel.mcse.ardoco.core.models.connectors.generators.Extractor;

public abstract class CodeExtractor extends Extractor {
    private static final Logger logger = LoggerFactory.getLogger(CodeExtractor.class);

    private static final String CODE_MODEL_FILE_NAME = "codeModel.acm";

    protected CodeExtractor(String path) {
        super(path);
    }

    @Override
    public abstract CodeModel extractModel();

    @Override
    public ModelType getModelType() {
        return CodeModelType.CODE_MODEL;
    }

    public void writeOutCodeModel(CodeModel codeModel) {
        ObjectMapper objectMapper = JsonUtils.createObjectMapper();
        objectMapper.registerModule(new Jdk8Module());
        try {
            File file = new File(getCodeModelFileString());
            objectMapper.writeValue(file, codeModel);
        } catch (IOException e) {
            logger.warn("An exception occurred when writing the code model.", e);
        }
    }

    public CodeModel readInCodeModel() {
        File codeModelFile = new File(this.getCodeModelFileString());
        return readInCodeModel(codeModelFile);
    }

    public static CodeModel readInCodeModel(File codeModelFile) {
        if (codeModelFile != null && codeModelFile.isFile()) {
            logger.info("Reading in existing code model.");
            ObjectMapper objectMapper = JsonUtils.createObjectMapper();
            objectMapper.registerModule(new Jdk8Module());
            try {
                return objectMapper.readValue(codeModelFile, CodeModel.class);
            } catch (IOException e) {
                logger.warn("An exception occurred when reading the code model.", e);
            }
        }
        return null;
    }

    private String getCodeModelFileString() {
        return path + File.separator + CODE_MODEL_FILE_NAME;
    }

}
