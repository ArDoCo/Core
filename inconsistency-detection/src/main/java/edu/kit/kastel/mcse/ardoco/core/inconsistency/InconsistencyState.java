/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.inconsistency;

import edu.kit.kastel.mcse.ardoco.core.api.data.AbstractState;
import edu.kit.kastel.mcse.ardoco.core.api.data.inconsistency.IInconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.data.inconsistency.IInconsistencyState;
import edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator.IRecommendedInstance;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import java.util.List;
import java.util.Map;

/**
 * @author Jan Keim
 */
public class InconsistencyState extends AbstractState implements IInconsistencyState {

	private MutableList<IRecommendedInstance> recommendedInstances;
	private MutableList<IInconsistency> inconsistencies;

	public InconsistencyState(Map<String, String> configs) {
		super(configs);
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
	public boolean addInconsistency(IInconsistency inconsistency) {
		if (!inconsistencies.contains(inconsistency)) {
			return inconsistencies.add(inconsistency);
		}
		return false;
	}

	/**
	 * Returns a list of inconsistencies held by this state
	 *
	 * @return list of inconsistencies
	 */
	@Override
	public ImmutableList<IInconsistency> getInconsistencies() {
		return inconsistencies.toImmutable();
	}

	@Override
	public IInconsistencyState createCopy() {
		var newInconsistencyState = new InconsistencyState(this.configs);
		newInconsistencyState.inconsistencies = inconsistencies.collect(IInconsistency::createCopy);
		return newInconsistencyState;
	}

	@Override
	public boolean addRecommendedInstance(IRecommendedInstance recommendedInstance) {
		return this.recommendedInstances.add(recommendedInstance);
	}

	@Override
	public boolean removeRecommendedInstance(IRecommendedInstance recommendedInstance) {
		return this.recommendedInstances.remove(recommendedInstance);
	}

	/**
	 * @return the recommendedInstances
	 */
	@Override
	public MutableList<IRecommendedInstance> getRecommendedInstances() {
		return recommendedInstances;
	}

	/**
	 * @param recommendedInstances the recommendedInstances to set
	 */
	@Override
	public void setRecommendedInstances(List<IRecommendedInstance> recommendedInstances) {
		this.recommendedInstances = Lists.mutable.empty();
		this.recommendedInstances.addAll(recommendedInstances);
	}

}
