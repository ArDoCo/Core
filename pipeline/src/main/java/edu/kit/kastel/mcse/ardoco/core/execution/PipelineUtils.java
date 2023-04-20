/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.execution;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import edu.kit.kastel.mcse.ardoco.core.api.data.model.ArchitectureModelType;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelConnector;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.NlpInformant;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.ConnectionGenerator;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.InconsistencyChecker;
import edu.kit.kastel.mcse.ardoco.core.model.ModelProviderAgent;
import edu.kit.kastel.mcse.ardoco.core.model.connectors.JavaJsonModelConnector;
import edu.kit.kastel.mcse.ardoco.core.model.connectors.PcmXMLModelConnector;
import edu.kit.kastel.mcse.ardoco.core.model.connectors.UMLModelConnector;
import edu.kit.kastel.mcse.ardoco.core.model.informants.ModelProviderInformant;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.RecommendationGenerator;
import edu.kit.kastel.mcse.ardoco.core.text.providers.TextPreprocessingAgent;
import edu.kit.kastel.mcse.ardoco.core.text.providers.informants.corenlp.CoreNLPProvider;
import edu.kit.kastel.mcse.ardoco.core.textextraction.TextExtraction;

public class PipelineUtils {
    /**
     * Creates an {@link InconsistencyChecker} and applies the additional configuration to it.
     *
     * @param additionalConfigs the additional configuration
     * @param dataRepository    the data repository
     * @return an instance of InconsistencyChecker
     */
    public static InconsistencyChecker getInconsistencyChecker(Map<String, String> additionalConfigs, DataRepository dataRepository) {
        var inconsistencyChecker = new InconsistencyChecker(dataRepository);
        inconsistencyChecker.applyConfiguration(additionalConfigs);
        return inconsistencyChecker;
    }

    /**
     * Creates a {@link ConnectionGenerator} and applies the additional configuration to it.
     *
     * @param additionalConfigs the additional configuration
     * @param dataRepository    the data repository
     * @return an instance of connectionGenerator
     */
    public static ConnectionGenerator getConnectionGenerator(Map<String, String> additionalConfigs, DataRepository dataRepository) {
        var connectionGenerator = new ConnectionGenerator(dataRepository);
        connectionGenerator.applyConfiguration(additionalConfigs);
        return connectionGenerator;
    }

    /**
     * Creates a {@link RecommendationGenerator} and applies the additional configuration to it.
     *
     * @param additionalConfigs the additional configuration
     * @param dataRepository    the data repository
     * @return an instance of {@link RecommendationGenerator}
     */
    public static RecommendationGenerator getRecommendationGenerator(Map<String, String> additionalConfigs, DataRepository dataRepository) {
        var recommendationGenerator = new RecommendationGenerator(dataRepository);
        recommendationGenerator.applyConfiguration(additionalConfigs);
        return recommendationGenerator;
    }

    /**
     * Creates a {@link TextExtraction} and applies the additional configuration to it.
     *
     * @param additionalConfigs the additional configuration
     * @param dataRepository    the data repository
     * @return an instance of InconsistencyChecker
     */
    public static TextExtraction getTextExtraction(Map<String, String> additionalConfigs, DataRepository dataRepository) {
        var textExtractor = new TextExtraction(dataRepository);
        textExtractor.applyConfiguration(additionalConfigs);
        return textExtractor;
    }

    /**
     * Creates a {@link ModelProviderInformant} for Java.
     *
     * @param inputCodeModel the path to the input Java Code Model
     * @param dataRepository the data repository
     * @return A ModelProvider for the Java Code Model
     * @throws IOException if the Code Model cannot be accessed
     */
    public static ModelProviderInformant getJavaModelProvider(File inputCodeModel, DataRepository dataRepository) throws IOException {
        ModelConnector javaModel = new JavaJsonModelConnector(inputCodeModel);
        return new ModelProviderInformant(dataRepository, javaModel);
    }

    /**
     * Creates a {@link ModelProviderInformant} for PCM.
     *
     * @param inputArchitectureModel the path to the input PCM
     * @param architectureModelType  the architecture model to use
     * @param dataRepository         the data repository
     * @return A ModelProvider for the PCM
     * @throws IOException if the Code Model cannot be accessed
     */
    public static ModelProviderAgent getArchitectureModelProvider(File inputArchitectureModel, ArchitectureModelType architectureModelType,
            DataRepository dataRepository) throws IOException {
        ModelConnector connector = switch (architectureModelType) {
        case PCM -> new PcmXMLModelConnector(inputArchitectureModel);
        case UML -> new UMLModelConnector(inputArchitectureModel);
        };
        return new ModelProviderAgent(dataRepository, List.of(connector));
    }

    /**
     * Creates a {@link CoreNLPProvider} as {@link NlpInformant} and reads the provided text.
     *
     * @param additionalConfigs the additional configuration that should be applied
     * @param dataRepository    the data repository
     * @return a CoreNLPProvider with the provided text read in
     */
    public static TextPreprocessingAgent getTextPreprocessing(Map<String, String> additionalConfigs, DataRepository dataRepository) {
        var textProvider = new TextPreprocessingAgent(dataRepository);
        textProvider.applyConfiguration(additionalConfigs);
        return textProvider;
    }
}
