/**
 *
 */
package edu.kit.kastel.mcse.ardoco.core.datastructures.definitions;

import org.eclipse.collections.api.list.ImmutableList;

/**
 * This defines the interface of a Coreference Cluster (CorefCluster). A CorefCluster is a cluster that collects all
 * mentions of an entity and has a representative mention.
 *
 * @author Jan Keim
 *
 */
public interface ICorefCluster {

    int getId();

    String getRepresentativeMention();

    ImmutableList<ImmutableList<IWord>> getMentions();
}
