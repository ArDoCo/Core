/**
 *
 */
package edu.kit.kastel.mcse.ardoco.core.text.providers.ontology;

import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntProperty;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;

import edu.kit.kastel.informalin.ontology.OntologyConnector;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.ICorefCluster;
import edu.kit.kastel.mcse.ardoco.core.datastructures.definitions.IWord;

/**
 * @author Jan Keim
 *
 */
public final class OntologyCorefCluster implements ICorefCluster {

    private Individual corefIndividual;
    private OntologyConnector ontologyConnector;

    private OntProperty mentionProperty;
    private OntProperty representativeMentionProperty;
    private OntProperty wordsProperty;
    private OntProperty uuidProperty;

    private OntologyCorefCluster(OntologyConnector ontologyConnector, Individual corefIndividual) {
        this.corefIndividual = corefIndividual;
        this.ontologyConnector = ontologyConnector;
    }

    static OntologyCorefCluster get(OntologyConnector ontologyConnector, Individual corefIndividual) {
        if (ontologyConnector == null || corefIndividual == null) {
            return null;
        }

        var occ = new OntologyCorefCluster(ontologyConnector, corefIndividual);
        occ.init();
        return occ;
    }

    private void init() {
        mentionProperty = ontologyConnector.getPropertyByIri(CommonOntologyUris.HAS_MENTION_PROPERTY.getUri()).orElseThrow();
        representativeMentionProperty = ontologyConnector.getPropertyByIri(CommonOntologyUris.REPRESENTATIVE_MENTION_PROPERTY.getUri()).orElseThrow();
        wordsProperty = ontologyConnector.getPropertyByIri(CommonOntologyUris.HAS_WORDS_PROPERTY.getUri()).orElseThrow();
        uuidProperty = ontologyConnector.getPropertyByIri(CommonOntologyUris.UUID_PROPERTY.getUri()).orElseThrow();
    }

    @Override
    public int getId() {
        var optId = ontologyConnector.getPropertyIntValue(corefIndividual, uuidProperty);
        if (optId.isPresent()) {
            return optId.get();
        }
        return -1;
    }

    @Override
    public String getRepresentativeMention() {
        var representativeMention = ontologyConnector.getPropertyStringValue(corefIndividual, representativeMentionProperty);
        if (representativeMention.isPresent()) {
            return representativeMention.get();
        }
        return null;
    }

    @Override
    public ImmutableList<ImmutableList<IWord>> getMentions() {
        MutableList<ImmutableList<IWord>> mentionList = Lists.mutable.empty();

        ImmutableList<Individual> mentions = ontologyConnector.getObjectsOf(corefIndividual, mentionProperty).collect(n -> n.as(Individual.class));
        if (mentions == null || mentions.isEmpty()) {
            return mentionList.toImmutable();
        }

        for (var mention : mentions) {
            var wordListResource = ontologyConnector.getPropertyValue(mention, wordsProperty).asResource();
            var wordListOpt = ontologyConnector.getListByIri(wordListResource.getURI());
            if (wordListOpt.isEmpty()) {
                continue;
            }
            var wordList = wordListOpt.get();

            MutableList<IWord> words = Lists.mutable.empty();
            for (var wordIndividual : wordList) {
                var word = OntologyWord.get(ontologyConnector, wordIndividual);
                words.add(word);
            }
            mentionList.add(words.toImmutable());
        }

        return mentionList.toImmutable();
    }

}
