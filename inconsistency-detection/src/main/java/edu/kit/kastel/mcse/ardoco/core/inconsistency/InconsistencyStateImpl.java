/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.inconsistency;

import java.util.List;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.mcse.ardoco.core.api.data.AbstractState;
import edu.kit.kastel.mcse.ardoco.core.api.data.inconsistency.Inconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.data.inconsistency.InconsistencyState;
import edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator.RecommendedInstance;

public class InconsistencyStateImpl extends AbstractState implements InconsistencyState {

    private transient MutableList<RecommendedInstance> recommendedInstances;
    private transient MutableList<Inconsistency> inconsistencies;

    public InconsistencyStateImpl() {
        inconsistencies = Lists.mutable.empty();
        recommendedInstances = Lists.mutable.empty();
    }

    /**
     * Add an Inconsistency to this state. Does not add duplicate inconsistencies.
     *
     * @param inconsistency the inconsistency to add
     * @return true if added successfully
     */
    @Override
    public boolean addInconsistency(Inconsistency inconsistency) {
        if (!inconsistencies.contains(inconsistency)) {
            return inconsistencies.add(inconsistency);
        }
        return false;
    }

    @Override
    public boolean removeInconsistency(Inconsistency inconsistency) {
        return inconsistencies.remove(inconsistency);
    }

    /**
     * Returns a list of inconsistencies held by this state
     *
     * @return list of inconsistencies
     */
    @Override
    public ImmutableList<Inconsistency> getInconsistencies() {
        return inconsistencies.toImmutable();
    }

    @Override
    public boolean addRecommendedInstance(RecommendedInstance recommendedInstance) {
        return this.recommendedInstances.add(recommendedInstance);
    }

    @Override
    public boolean removeRecommendedInstance(RecommendedInstance recommendedInstance) {
        return this.recommendedInstances.remove(recommendedInstance);
    }

    /**
     * @return the recommendedInstances
     */
    @Override
    public MutableList<RecommendedInstance> getRecommendedInstances() {
        return recommendedInstances;
    }

    /**
     * @param recommendedInstances the recommendedInstances to set
     */
    @Override
    public void setRecommendedInstances(List<RecommendedInstance> recommendedInstances) {
        this.recommendedInstances = Lists.mutable.empty();
        this.recommendedInstances.addAll(recommendedInstances);
    }

}
