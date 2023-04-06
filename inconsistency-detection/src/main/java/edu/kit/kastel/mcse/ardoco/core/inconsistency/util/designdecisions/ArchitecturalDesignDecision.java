/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.inconsistency.util.designdecisions;

import java.util.List;

import org.eclipse.collections.api.factory.Lists;

/**
 * This enum represents the taxonomy of Architectural Design Decisions. It contains certain methods to get ancestors and
 * the level within the taxonomy. Also contains a function to check if an ADD is a subcategory/subclass of another ADD.
 * This is helfpul if you want to look only at certain categories and need to merge the subclasses to its
 * parents/ancestors.
 *
 */
public enum ArchitecturalDesignDecision {
    NO_DESIGN_DECISION(null), //
    DESIGN_DECISION(null), //
    //
    EXISTENCE_DECISION(DESIGN_DECISION), //
    ///
    STRUCTURAL_DECISION(EXISTENCE_DECISION), //
    EXTRA_SYSTEMIC(STRUCTURAL_DECISION), DATA_FILE(EXTRA_SYSTEMIC), INTEGRATION(EXTRA_SYSTEMIC), //
    INTRA_SYSTEMIC(STRUCTURAL_DECISION), INTERFACE(INTRA_SYSTEMIC), COMPONENT(INTRA_SYSTEMIC), //
    CLASS_RELATED(INTRA_SYSTEMIC), ASSOCIATION(CLASS_RELATED), CLASS(CLASS_RELATED), INHERITANCE(CLASS_RELATED), //
    ///
    ARRANGEMENT_DECISION(EXISTENCE_DECISION), //
    ARCHITECTURAL_STYLE(ARRANGEMENT_DECISION), //
    ARCHITECTURAL_PATTERN(ARRANGEMENT_DECISION), //
    REFERENCE_ARCHITECTURE(ARRANGEMENT_DECISION), //
    ///
    BEHAVIORAL_DECISION(EXISTENCE_DECISION), //
    RELATION(BEHAVIORAL_DECISION), //
    FUNCTION(BEHAVIORAL_DECISION), //
    ALGORITHM(BEHAVIORAL_DECISION), //
    MESSAGING(BEHAVIORAL_DECISION), //
    //
    PROPERTY_DECISION(DESIGN_DECISION), //
    GUIDELINE(PROPERTY_DECISION), //
    DESIGN_RULE(PROPERTY_DECISION), //
    //
    EXECUTIVE_DECISION(DESIGN_DECISION), //
    ORGANIZATIONAL_PROCESS_RELATED(EXECUTIVE_DECISION), //
    TECHNOLOGICAL(EXECUTIVE_DECISION), //
    TOOL(TECHNOLOGICAL), //
    DATA_BASE(TECHNOLOGICAL), //
    PLATFORM(TECHNOLOGICAL), //
    BOUNDARY_INTERFACE(TECHNOLOGICAL), USER_INTERFACE(BOUNDARY_INTERFACE), API(BOUNDARY_INTERFACE), //
    PROGRAMMING_LANGUAGE(TECHNOLOGICAL), //
    FRAMEWORK(TECHNOLOGICAL), //

    ;

    private ArchitecturalDesignDecision parent;

    ArchitecturalDesignDecision(ArchitecturalDesignDecision parent) {
        this.parent = parent;
    }

    /**
     * Checks if the calling class is contained in another {@link ArchitecturalDesignDecision} based on the hierarchy.
     * Here, we assume that a class also contains itself. This means, for example, that every class except
     * NO_DESIGN_DECISION is contained in DESIGN_DECISION and nothing is contained in
     * NO_DESIGN_DECISION (except itself). Basically, this method checks if the shortest path from the caller
     * to DESIGN_DECISION contains the given class (= is an ancestor).
     *
     * @param other the other {@link ArchitecturalDesignDecision} that may contain this
     * @return if the other {@link ArchitecturalDesignDecision} is an ancestor.
     */
    public boolean isContainedIn(ArchitecturalDesignDecision other) {
        if (other == this) {
            return true;
        }
        var ancestor = parent;
        while (ancestor != null) {
            if (ancestor == other) {
                return true;
            }
            ancestor = ancestor.parent;
        }
        return false;
    }

    /**
     * Returns a list of ancestors. The ancestors are sorted based on their level (ascending). Does not contain the
     * calling {@link ArchitecturalDesignDecision} itself.
     *
     * @return list of ancestors
     */
    public List<ArchitecturalDesignDecision> getAncestors() {
        List<ArchitecturalDesignDecision> ancestors = Lists.mutable.empty();
        var ancestor = parent;
        while (ancestor != null) {
            ancestors.add(ancestor);
            ancestor = ancestor.parent;
        }
        ancestors.sort((a, b) -> Integer.compare(a.getLevel(), b.getLevel()));
        return ancestors;
    }

    /**
     * Returns the level of the {@link ArchitecturalDesignDecision}. At the top of the taxonomy, the level is 0 and the
     * level is increasing with the path length to the leaves.
     *
     * @return the level
     */
    public int getLevel() {
        return getAncestors().size();
    }

}
