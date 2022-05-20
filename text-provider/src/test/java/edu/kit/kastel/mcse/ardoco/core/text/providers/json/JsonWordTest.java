/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.text.providers.json;

import edu.kit.kastel.mcse.ardoco.core.text.providers.ITextConnector;
import edu.kit.kastel.mcse.ardoco.core.text.providers.base.WordTest;

class JsonWordTest extends WordTest {
    @Override
    protected ITextConnector getProvider() {
        return JsonTextProviderTest.getTextProvider();
    }
}
