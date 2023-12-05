/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.models.generators.pcm;

import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.ArchitectureModel;
import edu.kit.kastel.mcse.ardoco.core.models.connectors.generators.architecture.pcm.PcmExtractor;
import edu.kit.kastel.mcse.ardoco.core.models.generators.ArchitectureExtractorTest;

class PcmExtractorTest extends ArchitectureExtractorTest {

    @Test
    void extractorTest() {
        var pcmExtractor = new PcmExtractor("src/test/resources/mediastore/architecture/pcm/ms.repository");
        ArchitectureModel model = pcmExtractor.extractModel();
        checkModel(model);
    }

}
