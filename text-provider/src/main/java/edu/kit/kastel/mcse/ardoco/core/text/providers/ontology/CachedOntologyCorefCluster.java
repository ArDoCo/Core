/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.text.providers.ontology;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.collections.api.list.ImmutableList;

import edu.kit.kastel.mcse.ardoco.core.api.data.text.ICorefCluster;
import edu.kit.kastel.mcse.ardoco.core.api.data.text.IWord;

/**
 * @author Jan Keim
 */
public final class CachedOntologyCorefCluster implements ICorefCluster {
    private static final Map<ICorefCluster, CachedOntologyCorefCluster> cache = new HashMap<>();

    private final ICorefCluster ontologyCorefCluster;

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
    public synchronized int id() {
        if (id <= -1) {
            id = ontologyCorefCluster.id();
        }
        return id;
    }

    @Override
    public synchronized String representativeMention() {
        if (representativeMention == null) {
            representativeMention = ontologyCorefCluster.representativeMention();
        }
        return representativeMention;
    }

    @Override
    public synchronized ImmutableList<ImmutableList<IWord>> mentions() {
        if (mentions == null) {
            mentions = ontologyCorefCluster.mentions().collect(list -> list.collect(CachedOntologyWord::get));
        }
        return mentions;
    }

}
