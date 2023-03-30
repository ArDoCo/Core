/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.informalin.framework.common;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class JsonUtils {
    private JsonUtils() {
        throw new IllegalAccessError();
    }

    public static ObjectMapper createObjectMapper() {
        ObjectMapper oom = new ObjectMapper();
        oom.setVisibility(oom.getSerializationConfig()
                .getDefaultVisibilityChecker() //
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)//
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)//
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)//
                .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE));
        return oom;
    }
}
