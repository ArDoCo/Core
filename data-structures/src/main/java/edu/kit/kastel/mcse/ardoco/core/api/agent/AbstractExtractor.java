/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.api.agent;

import edu.kit.kastel.mcse.ardoco.core.api.data.IData;
import edu.kit.kastel.mcse.ardoco.core.common.AbstractConfigurable;

public abstract class AbstractExtractor<D extends IData> extends AbstractConfigurable implements IExtractor<D> {
}
