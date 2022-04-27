/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.api.data.text;

import java.util.StringJoiner;

import org.eclipse.collections.api.list.ImmutableList;

/**
 * This defines the interface of a Coreference Cluster (CorefCluster). A CorefCluster is a cluster that collects all
 * mentions of an entity and has a representative mention.
 *
 * @author Jan Keim
 *
 */
public interface ICorefCluster {

    int id();

    String representativeMention();

    ImmutableList<ImmutableList<IWord>> mentions();

    static String getTextForMention(ImmutableList<IWord> mention) {
        var textJoiner = new StringJoiner(" ");
        for (var word : mention) {
            textJoiner.add(word.getText());
        }
        return textJoiner.toString();
    }
}
