/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum Classification {
    // All available ..
    // 'ClassNode', 'Qualifier', 'NAryAssociationDiamond', 'Package', 'Comment', 'Label', 'Aggregation', 'Composition', 'Extension', 'Dependency', 'Realization', 'CommentConnection', 'AssociationUnidirectional', 'AssociationBidirectional'
    LABEL("Label"), CLASS("ClassNode"),

    // Artificial Classes
    TEXT("TEXT"), RAWTEXT("RAWTEXT"), UNKNOWN(null);

    private static final Logger logger = LoggerFactory.getLogger(Classification.class);
    private final String classificationString;

    Classification(String classificationString) {
        this.classificationString = classificationString;
    }

    public String getClassificationString() {
        return classificationString;
    }

    public static Classification byString(String classificationString) {
        for (Classification cls : values()) {
            if (Objects.equals(cls.classificationString, classificationString))
                return cls;
        }
        logger.error("Found new classification, defaulting to UNKNOWN: {}", classificationString);
        return Classification.UNKNOWN;
    }
}
