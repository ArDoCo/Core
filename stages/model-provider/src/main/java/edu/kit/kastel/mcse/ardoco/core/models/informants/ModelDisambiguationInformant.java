package edu.kit.kastel.mcse.ardoco.core.models.informants;

import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;

public class ModelDisambiguationInformant extends Informant {
  public ModelDisambiguationInformant(DataRepository dataRepository) {
    super(ModelDisambiguationInformant.class.getSimpleName(), dataRepository);
  }

  @Override
  public void run() {

  }
}
