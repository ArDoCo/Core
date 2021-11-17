/**
 *
 */
package edu.kit.kastel.mcse.ardoco.core.text.providers.ontology;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.mcse.ardoco.core.text.ICorefCluster;
import edu.kit.kastel.mcse.ardoco.core.text.IWord;

/**
 * @author Jan Keim
 *
 */
public final class CachedOntologyCorefCluster implements ICorefCluster {
    private static final Map<ICorefCluster, CachedOntologyCorefCluster> cache = new HashMap<>();

    private ICorefCluster ontologyCorefCluster;

    private int id = -1;
    private String representativeMention = null;
    private ImmutableList<ImmutableList<IWord>> mentions = null;

    private CachedOntologyCorefCluster(ICorefCluster corefCluster) {
        ontologyCorefCluster = corefCluster;
    }

    static CachedOntologyCorefCluster get(ICorefCluster corefCluster) {
        synchronized (cache) {
            return cache.computeIfAbsent(corefCluster, CachedOntologyCorefCluster::new);
        }
    }

    @Override
    public synchronized int getId() {
        if (id <= -1) {
            id = ontologyCorefCluster.getId();
        }
        return id;
    }

    @Override
    public synchronized String getRepresentativeMention() {
        if (representativeMention == null) {
            representativeMention = ontologyCorefCluster.getRepresentativeMention();
        }
        return representativeMention;
    }

    @Override
    public synchronized ImmutableList<ImmutableList<IWord>> getMentions() {
        if (mentions == null) {
            mentions = ontologyCorefCluster.getMentions().collect(list -> list.collect(CachedOntologyWord::get));
        }
        return mentions;
    }

}
