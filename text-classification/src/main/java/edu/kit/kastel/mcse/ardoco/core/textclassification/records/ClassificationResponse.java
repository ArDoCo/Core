package edu.kit.kastel.mcse.ardoco.core.textclassification.records;

import java.util.Map;

public record ClassificationResponse(
        Map<Integer, String> classifications
) {}
