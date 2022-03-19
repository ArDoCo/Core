/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.api.agent;

import edu.kit.kastel.mcse.ardoco.core.api.data.IData;

public interface IAgent<D extends IData> {
	void execute(D data);

	default String getId() {
		return this.getClass().getSimpleName();
	}
}
