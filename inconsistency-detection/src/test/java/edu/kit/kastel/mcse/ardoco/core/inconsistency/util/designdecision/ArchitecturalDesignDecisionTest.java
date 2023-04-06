package edu.kit.kastel.mcse.ardoco.core.inconsistency.util.designdecision;

import static edu.kit.kastel.mcse.ardoco.core.inconsistency.util.designdecisions.ArchitecturalDesignDecision.*;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.inconsistency.util.designdecisions.ArchitecturalDesignDecision;

class ArchitecturalDesignDecisionTest {

    @Test
    @DisplayName("Test hierarchy for sub-classes of decision")
    void hierarchyDesignDecisionTest() {
        Assertions.assertAll(//
                () -> Assertions.assertFalse(NO_DESIGN_DECISION.isContainedIn(DESIGN_DECISION)), //
                () -> Assertions.assertTrue(DESIGN_DECISION.isContainedIn(DESIGN_DECISION)), //
                () -> Assertions.assertTrue(EXISTENCE_DECISION.isContainedIn(DESIGN_DECISION)), //
                () -> Assertions.assertTrue(STRUCTURAL_DECISION.isContainedIn(DESIGN_DECISION)), //
                () -> Assertions.assertTrue(INTRA_SYSTEMIC.isContainedIn(DESIGN_DECISION)), //
                () -> Assertions.assertTrue(COMPONENT.isContainedIn(DESIGN_DECISION)), //
                () -> Assertions.assertTrue(CLASS.isContainedIn(DESIGN_DECISION)), //
                () -> Assertions.assertTrue(ARRANGEMENT_DECISION.isContainedIn(DESIGN_DECISION)), //
                () -> Assertions.assertTrue(ARCHITECTURAL_STYLE.isContainedIn(DESIGN_DECISION)), //
                () -> Assertions.assertTrue(ARCHITECTURAL_PATTERN.isContainedIn(DESIGN_DECISION)), //
                () -> Assertions.assertTrue(REFERENCE_ARCHITECTURE.isContainedIn(DESIGN_DECISION)), //
                () -> Assertions.assertTrue(BEHAVIORAL_DECISION.isContainedIn(DESIGN_DECISION)), //
                () -> Assertions.assertTrue(FUNCTION.isContainedIn(DESIGN_DECISION)), //
                () -> Assertions.assertTrue(PROPERTY_DECISION.isContainedIn(DESIGN_DECISION)), //
                () -> Assertions.assertTrue(GUIDELINE.isContainedIn(DESIGN_DECISION)), //
                () -> Assertions.assertTrue(EXECUTIVE_DECISION.isContainedIn(DESIGN_DECISION)), //
                () -> Assertions.assertTrue(TECHNOLOGICAL.isContainedIn(DESIGN_DECISION)), //
                () -> Assertions.assertTrue(ORGANIZATIONAL_PROCESS_RELATED.isContainedIn(DESIGN_DECISION)), //
                () -> Assertions.assertTrue(BOUNDARY_INTERFACE.isContainedIn(DESIGN_DECISION)), //
                () -> Assertions.assertTrue(API.isContainedIn(DESIGN_DECISION)), //
                () -> Assertions.assertTrue(FRAMEWORK.isContainedIn(DESIGN_DECISION)) //
        );
    }

    @Test
    @DisplayName("Test no design decision hierarchy")
    void hierarchyNoDesignDecisionTest() {
        Assertions.assertAll(//
                () -> Assertions.assertTrue(NO_DESIGN_DECISION.isContainedIn(NO_DESIGN_DECISION)), //
                () -> Assertions.assertFalse(DESIGN_DECISION.isContainedIn(NO_DESIGN_DECISION)), //
                () -> Assertions.assertFalse(EXISTENCE_DECISION.isContainedIn(NO_DESIGN_DECISION)), //
                () -> Assertions.assertFalse(STRUCTURAL_DECISION.isContainedIn(NO_DESIGN_DECISION)), //
                () -> Assertions.assertFalse(INTRA_SYSTEMIC.isContainedIn(NO_DESIGN_DECISION)), //
                () -> Assertions.assertFalse(COMPONENT.isContainedIn(NO_DESIGN_DECISION)), //
                () -> Assertions.assertFalse(CLASS.isContainedIn(NO_DESIGN_DECISION)), //
                () -> Assertions.assertFalse(ARRANGEMENT_DECISION.isContainedIn(NO_DESIGN_DECISION)), //
                () -> Assertions.assertFalse(ARCHITECTURAL_STYLE.isContainedIn(NO_DESIGN_DECISION)), //
                () -> Assertions.assertFalse(ARCHITECTURAL_PATTERN.isContainedIn(NO_DESIGN_DECISION)), //
                () -> Assertions.assertFalse(REFERENCE_ARCHITECTURE.isContainedIn(NO_DESIGN_DECISION)), //
                () -> Assertions.assertFalse(BEHAVIORAL_DECISION.isContainedIn(NO_DESIGN_DECISION)), //
                () -> Assertions.assertFalse(FUNCTION.isContainedIn(NO_DESIGN_DECISION)), //
                () -> Assertions.assertFalse(PROPERTY_DECISION.isContainedIn(NO_DESIGN_DECISION)), //
                () -> Assertions.assertFalse(GUIDELINE.isContainedIn(NO_DESIGN_DECISION)), //
                () -> Assertions.assertFalse(EXECUTIVE_DECISION.isContainedIn(NO_DESIGN_DECISION)), //
                () -> Assertions.assertFalse(TECHNOLOGICAL.isContainedIn(NO_DESIGN_DECISION)), //
                () -> Assertions.assertFalse(ORGANIZATIONAL_PROCESS_RELATED.isContainedIn(NO_DESIGN_DECISION)), //
                () -> Assertions.assertFalse(BOUNDARY_INTERFACE.isContainedIn(NO_DESIGN_DECISION)), //
                () -> Assertions.assertFalse(API.isContainedIn(NO_DESIGN_DECISION)), //
                () -> Assertions.assertFalse(FRAMEWORK.isContainedIn(NO_DESIGN_DECISION)) //
        );
    }

    @Test
    @DisplayName("Test hierarchy for sub-classes of existence decision")
    void hierarchyExistenceDecisionTest() {
        Assertions.assertAll(//
                () -> Assertions.assertTrue(EXISTENCE_DECISION.isContainedIn(EXISTENCE_DECISION)), //
                () -> Assertions.assertTrue(STRUCTURAL_DECISION.isContainedIn(EXISTENCE_DECISION)), //
                () -> Assertions.assertTrue(EXTRA_SYSTEMIC.isContainedIn(EXISTENCE_DECISION)), //
                () -> Assertions.assertTrue(INTRA_SYSTEMIC.isContainedIn(EXISTENCE_DECISION)), //
                () -> Assertions.assertTrue(CLASS_RELATED.isContainedIn(EXISTENCE_DECISION)), //
                () -> Assertions.assertTrue(DATA_FILE.isContainedIn(EXISTENCE_DECISION)), //
                () -> Assertions.assertTrue(INTEGRATION.isContainedIn(EXISTENCE_DECISION)), //
                () -> Assertions.assertTrue(INTERFACE.isContainedIn(EXISTENCE_DECISION)), //
                () -> Assertions.assertTrue(COMPONENT.isContainedIn(EXISTENCE_DECISION)), //
                () -> Assertions.assertTrue(ASSOCIATION.isContainedIn(EXISTENCE_DECISION)), //
                () -> Assertions.assertTrue(CLASS.isContainedIn(EXISTENCE_DECISION)), //
                () -> Assertions.assertTrue(INHERITANCE.isContainedIn(EXISTENCE_DECISION)), //
                () -> Assertions.assertTrue(ARRANGEMENT_DECISION.isContainedIn(EXISTENCE_DECISION)), //
                () -> Assertions.assertTrue(ARCHITECTURAL_STYLE.isContainedIn(EXISTENCE_DECISION)), //
                () -> Assertions.assertTrue(ARCHITECTURAL_PATTERN.isContainedIn(EXISTENCE_DECISION)), //
                () -> Assertions.assertTrue(REFERENCE_ARCHITECTURE.isContainedIn(EXISTENCE_DECISION)), //
                () -> Assertions.assertTrue(BEHAVIORAL_DECISION.isContainedIn(EXISTENCE_DECISION)), //
                () -> Assertions.assertTrue(RELATION.isContainedIn(EXISTENCE_DECISION)), //
                () -> Assertions.assertTrue(FUNCTION.isContainedIn(EXISTENCE_DECISION)), //
                () -> Assertions.assertTrue(ALGORITHM.isContainedIn(EXISTENCE_DECISION)), //
                () -> Assertions.assertTrue(MESSAGING.isContainedIn(EXISTENCE_DECISION)) //
        );
    }

    @Test
    @DisplayName("Test hierarchy for sub-classes of structural decision")
    void hierarchyStructuralDecisionTest() {
        Assertions.assertAll(//
                () -> Assertions.assertTrue(STRUCTURAL_DECISION.isContainedIn(STRUCTURAL_DECISION)), //
                () -> Assertions.assertTrue(EXTRA_SYSTEMIC.isContainedIn(STRUCTURAL_DECISION)), //
                () -> Assertions.assertTrue(INTRA_SYSTEMIC.isContainedIn(STRUCTURAL_DECISION)), //
                () -> Assertions.assertTrue(CLASS_RELATED.isContainedIn(STRUCTURAL_DECISION)), //
                () -> Assertions.assertTrue(DATA_FILE.isContainedIn(STRUCTURAL_DECISION)), //
                () -> Assertions.assertTrue(INTEGRATION.isContainedIn(STRUCTURAL_DECISION)), //
                () -> Assertions.assertTrue(INTERFACE.isContainedIn(STRUCTURAL_DECISION)), //
                () -> Assertions.assertTrue(COMPONENT.isContainedIn(STRUCTURAL_DECISION)), //
                () -> Assertions.assertTrue(ASSOCIATION.isContainedIn(STRUCTURAL_DECISION)), //
                () -> Assertions.assertTrue(CLASS.isContainedIn(STRUCTURAL_DECISION)), //
                () -> Assertions.assertTrue(INHERITANCE.isContainedIn(STRUCTURAL_DECISION)) //
        );
    }

    @Test
    @DisplayName("Test hierarchy for sub-classes of arrangement decision")
    void hierarchyArrangementDecisionTest() {
        Assertions.assertAll(//
                () -> Assertions.assertTrue(ARRANGEMENT_DECISION.isContainedIn(ARRANGEMENT_DECISION)), //
                () -> Assertions.assertTrue(ARCHITECTURAL_STYLE.isContainedIn(ARRANGEMENT_DECISION)), //
                () -> Assertions.assertTrue(ARCHITECTURAL_PATTERN.isContainedIn(ARRANGEMENT_DECISION)), //
                () -> Assertions.assertTrue(REFERENCE_ARCHITECTURE.isContainedIn(ARRANGEMENT_DECISION)) //
        );
    }

    @Test
    @DisplayName("Test hierarchy for sub-classes of behavioral decision")
    void hierarchyBehavioralDecisionTest() {
        Assertions.assertAll(//
                () -> Assertions.assertTrue(BEHAVIORAL_DECISION.isContainedIn(BEHAVIORAL_DECISION)), //
                () -> Assertions.assertTrue(RELATION.isContainedIn(BEHAVIORAL_DECISION)), //
                () -> Assertions.assertTrue(FUNCTION.isContainedIn(BEHAVIORAL_DECISION)), //
                () -> Assertions.assertTrue(ALGORITHM.isContainedIn(BEHAVIORAL_DECISION)), //
                () -> Assertions.assertTrue(MESSAGING.isContainedIn(BEHAVIORAL_DECISION)) //
        );
    }

    @Test
    @DisplayName("Test hierarchy for sub-classes of property decision")
    void hierarchyPropertyDecisionTest() {
        Assertions.assertAll(//
                () -> Assertions.assertTrue(PROPERTY_DECISION.isContainedIn(PROPERTY_DECISION)), //
                () -> Assertions.assertTrue(GUIDELINE.isContainedIn(PROPERTY_DECISION)), //
                () -> Assertions.assertTrue(DESIGN_RULE.isContainedIn(PROPERTY_DECISION)) //
        );
    }

    @Test
    @DisplayName("Test hierarchy for sub-classes of arrangement decision")
    void hierarchyExecutiveDecisionTest() {
        Assertions.assertAll(//
                () -> Assertions.assertTrue(EXECUTIVE_DECISION.isContainedIn(EXECUTIVE_DECISION)), //
                () -> Assertions.assertTrue(TECHNOLOGICAL.isContainedIn(EXECUTIVE_DECISION)), //
                () -> Assertions.assertTrue(ORGANIZATIONAL_PROCESS_RELATED.isContainedIn(EXECUTIVE_DECISION)), //
                () -> Assertions.assertTrue(TOOL.isContainedIn(EXECUTIVE_DECISION)), //
                () -> Assertions.assertTrue(DATA_BASE.isContainedIn(EXECUTIVE_DECISION)), //
                () -> Assertions.assertTrue(PLATFORM.isContainedIn(EXECUTIVE_DECISION)), //
                () -> Assertions.assertTrue(BOUNDARY_INTERFACE.isContainedIn(EXECUTIVE_DECISION)), //
                () -> Assertions.assertTrue(USER_INTERFACE.isContainedIn(EXECUTIVE_DECISION)), //
                () -> Assertions.assertTrue(API.isContainedIn(EXECUTIVE_DECISION)), //
                () -> Assertions.assertTrue(PROGRAMMING_LANGUAGE.isContainedIn(EXECUTIVE_DECISION)), //
                () -> Assertions.assertTrue(FRAMEWORK.isContainedIn(EXECUTIVE_DECISION)) //
        );
    }

    @Test
    @DisplayName("Test ancestor list retrieval")
    void getAncestorsTest() {
        var expectedAncestorsForClass = new ArchitecturalDesignDecision[] { DESIGN_DECISION, EXISTENCE_DECISION, STRUCTURAL_DECISION, INTRA_SYSTEMIC,
                CLASS_RELATED };
        Assertions.assertAll(//
                () -> Assertions.assertEquals(0, DESIGN_DECISION.getAncestors().size()), //
                () -> Assertions.assertEquals(0, NO_DESIGN_DECISION.getAncestors().size()), //
                () -> Assertions.assertEquals(1, EXISTENCE_DECISION.getAncestors().size()), //
                () -> Assertions.assertEquals(2, STRUCTURAL_DECISION.getAncestors().size()), //
                () -> Assertions.assertEquals(4, COMPONENT.getAncestors().size()), //
                () -> Assertions.assertEquals(3, RELATION.getAncestors().size()), //
                () -> Assertions.assertEquals(5, CLASS.getAncestors().size()), //
                () -> Assertions.assertEquals(4, API.getAncestors().size()), //
                () -> Assertions.assertArrayEquals(expectedAncestorsForClass, CLASS.getAncestors().toArray()) //
        );
    }

    @Test
    @DisplayName("Test level calculation")
    void levelTest() {
        Assertions.assertAll(//
                () -> Assertions.assertEquals(0, DESIGN_DECISION.getLevel()), //
                () -> Assertions.assertEquals(0, NO_DESIGN_DECISION.getLevel()), //
                () -> Assertions.assertEquals(1, EXISTENCE_DECISION.getLevel()), //
                () -> Assertions.assertEquals(2, STRUCTURAL_DECISION.getLevel()), //
                () -> Assertions.assertEquals(4, COMPONENT.getLevel()), //
                () -> Assertions.assertEquals(3, RELATION.getLevel()), //
                () -> Assertions.assertEquals(5, CLASS.getLevel()), //
                () -> Assertions.assertEquals(4, API.getLevel()) //
        );
    }
}
