package records;

import java.util.Map;

public record ClassificationResponse(
        Map<Integer, String> classifications
) {}
