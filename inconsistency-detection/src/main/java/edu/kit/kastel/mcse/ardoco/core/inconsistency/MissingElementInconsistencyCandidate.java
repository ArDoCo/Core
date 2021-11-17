package edu.kit.kastel.mcse.ardoco.core.inconsistency;

import java.util.Objects;

import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.set.MutableSet;

import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.IRecommendedInstance;

public class MissingElementInconsistencyCandidate {

    private IRecommendedInstance recommendedInstance;
    private MutableSet<MissingElementSupport> supports = Sets.mutable.empty();

    public MissingElementInconsistencyCandidate(IRecommendedInstance recommendedInstance) {
        super();
        this.recommendedInstance = recommendedInstance;
    }

    public MissingElementInconsistencyCandidate(IRecommendedInstance recommendedInstance, MissingElementSupport support) {
        super();
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
    public IRecommendedInstance getRecommendedInstance() {
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
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        MissingElementInconsistencyCandidate other = (MissingElementInconsistencyCandidate) obj;
        return Objects.equals(recommendedInstance, other.recommendedInstance);
    }

}
