package edu.kit.kastel.mcse.ardoco.core.common;

import java.util.concurrent.atomic.AtomicInteger;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItem;

public class IdentifierProvider {
    private static final AtomicInteger COUNTER = new AtomicInteger(1337);

    private IdentifierProvider() {
        // empty
    }

    public static String createId() {
        int currentCounter = COUNTER.getAndIncrement();
        return "acm%06djsd".formatted(currentCounter);
    }

    public static String createId(CodeItem codeItem) {
        return "TODO"; // TODO
    }

    public static String createId(ArchitectureItem architectureItem) {
        String type = architectureItem.getClass().toString().replace("Architecture", "");
        String name = architectureItem.getName();
        return "TODO"; // TODO
    }
}
