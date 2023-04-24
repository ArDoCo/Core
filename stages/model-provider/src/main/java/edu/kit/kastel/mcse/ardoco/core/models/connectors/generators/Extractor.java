package edu.kit.kastel.mcse.ardoco.core.models.connectors.generators;

import edu.kit.kastel.mcse.ardoco.core.api.models.Model;

public abstract class Extractor {

    public abstract Model extractModel(String path);
}
