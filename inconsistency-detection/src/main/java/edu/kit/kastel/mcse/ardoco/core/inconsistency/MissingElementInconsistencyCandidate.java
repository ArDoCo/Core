/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.inconsistency;

import java.util.Objects;

import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.set.MutableSet;

import edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator.RecommendedInstance;

public class MissingElementInconsistencyCandidate {

    private final RecommendedInstance recommendedInstance;
    private final MutableSet<MissingElementSupport> supports = Sets.mutable.empty();

    public MissingElementInconsistencyCandidate(RecommendedInstance recommendedInstance, MissingElementSupport support) {
        this.recommendedInstance = recommendedInstance;
        supports.add(support);
    }

    /**
     * Adds support for this candidate. If the support was already added before, it will not add it again.
     *
     * @param support the support that should be added
     * @return true if this candidate did not already contain the specified support
     */
    public boolean addSupport(MissingElementSupport support) {
        return supports.add(support);
    }

    /**
     * Returns an integer to show how much support this candidate has
     *
     * @return the amount of support
     */
    public int getAmountOfSupport() {
        return supports.size();
    }

    /**
     * @return the recommendedInstance
     */
    public RecommendedInstance getRecommendedInstance() {
        return recommendedInstance;
    }

    @Override
    public int hashCode() {
        return Objects.hash(recommendedInstance);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MissingElementInconsistencyCandidate other)) {
            return false;
        }
        return Objects.equals(recommendedInstance, other.recommendedInstance);
    }

    @Override
    public String toString() {
        return "MissingElementInconsistencyCandidate [" + (recommendedInstance != null ? "recommendedInstance=" + recommendedInstance : "") + "]";
    }

}
