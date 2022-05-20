/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.text.providers.json;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Assertions;

import edu.kit.kastel.mcse.ardoco.core.text.providers.ITextConnector;
import edu.kit.kastel.mcse.ardoco.core.text.providers.base.ProviderTest;

class JsonTextProviderTest extends ProviderTest {
    protected static String path = "src/test/resources/teastore.json";

    private static JsonTextProvider textProvider;

    public static synchronized JsonTextProvider getTextProvider() {
        if (textProvider != null)
            return textProvider;
        try {
            textProvider = JsonTextProvider.loadFromFile(new File(path));
        } catch (IOException e) {
            Assertions.fail(e.getMessage());
        }
        return textProvider;
    }

    @Override
    protected ITextConnector getProvider() {
        return JsonTextProviderTest.getTextProvider();
    }
}
