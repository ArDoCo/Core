/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.models.agents;

import edu.kit.kastel.mcse.ardoco.core.api.models.ArchitectureModelType;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelConnector;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.models.connectors.PcmXmlModelConnector;
import edu.kit.kastel.mcse.ardoco.core.models.connectors.UmlModelConnector;
import edu.kit.kastel.mcse.ardoco.core.models.informants.LegacyCodeModelInformant;
import edu.kit.kastel.mcse.ardoco.core.models.informants.ModelDisambiguationInformant;
import edu.kit.kastel.mcse.ardoco.core.models.informants.ModelProviderInformant;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.PipelineAgent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Agent that provides information from models.
 */
public class ModelProviderAgent extends PipelineAgent {
  /**
   * Instantiates a new model provider agent. The constructor takes a list of ModelConnectors
   * that are executed and used to extract information from models.
   *
   * @param dataRepository  the DataRepository
   * @param modelConnectors the list of ModelConnectors that should be used
   */
  public ModelProviderAgent(DataRepository dataRepository, List<ModelConnector> modelConnectors) {
    super(ModelProviderAgent.class.getSimpleName(), dataRepository,
            Stream.concat(fromConnectors(dataRepository, modelConnectors).stream(),
                    Stream.of(new ModelDisambiguationInformant(dataRepository))).toList());
  }

  private static List<Informant> fromConnectors(DataRepository dataRepository,
                                                List<ModelConnector> modelConnectors) {
    return modelConnectors.stream().map(modelConnector -> (Informant) new ModelProviderInformant(dataRepository,
            modelConnector)).toList();
  }

  private ModelProviderAgent(DataRepository data, LegacyCodeModelInformant codeModelInformant) {
    super(ModelProviderAgent.class.getSimpleName(), data, List.of(codeModelInformant));
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
  public static ModelProviderAgent get(File inputArchitectureModel,
                                       ArchitectureModelType architectureModelType,
                                       DataRepository dataRepository)
          throws IOException {
    ModelConnector connector = switch (architectureModelType) {
      case PCM -> new PcmXmlModelConnector(inputArchitectureModel);
      case UML -> new UmlModelConnector(inputArchitectureModel);
    };
    return new ModelProviderAgent(dataRepository, List.of(connector));
  }

  public static ModelProviderAgent getCodeProvider(DataRepository dataRepository) {
    return new ModelProviderAgent(dataRepository, new LegacyCodeModelInformant(dataRepository));
  }

  @Override
  protected List<Informant> getEnabledPipelineSteps() {
    return new ArrayList<>(getInformants());
  }
}
