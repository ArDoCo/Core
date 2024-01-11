/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.erid.diagramrecognition.informants;

import org.eclipse.collections.impl.factory.Lists;

import edu.kit.kastel.mcse.ardoco.core.api.Disambiguation;
import edu.kit.kastel.mcse.ardoco.core.common.util.AbbreviationDisambiguationHelper;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;

/**
 * Responsible for disambiguating abbreviations that are contained in
 * {@link edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramElement DiagramElements} and their
 * {@link edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.TextBox TextBoxes}.
 *
 * @see AbbreviationDisambiguationHelper
 */
public class DiagramDisambiguationInformant extends Informant {

    /**
     * Creates a new informant that acts on the specified data repository
     *
     * @param dataRepository the data repository
     */
    public DiagramDisambiguationInformant(DataRepository dataRepository) {
        super(DiagramDisambiguationInformant.class.getSimpleName(), dataRepository);
    }

    /**
     * Iterates over all diagram elements and their text boxes. Creates disambiguations for each contained abbreviation.
     *
     * @see AbbreviationDisambiguationHelper#getAbbreviationCandidates(String)
     * @see AbbreviationDisambiguationHelper#disambiguate(String)
     */
    @Override
    public void process() {
        var diagramRecognitionState = DataRepositoryHelper.getDiagramRecognitionState(dataRepository);
        var boxes = Lists.mutable.fromStream(diagramRecognitionState.getDiagrams().stream().flatMap(d -> d.getBoxes().stream()));
        for (var box : boxes) {
            var texts = box.getTexts();
            for (var textBox : texts) {
                var text = textBox.getText();
                var abbreviations = AbbreviationDisambiguationHelper.getAbbreviationCandidates(text);
                for (var abbreviation : abbreviations) {
                    var disambiguation = AbbreviationDisambiguationHelper.disambiguate(abbreviation);
                    //TODO Add to transient abbreviation cache? Currently not sure about performance impact
                    diagramRecognitionState.addDisambiguation(new Disambiguation(abbreviation, disambiguation.toArray(new String[0])));
                }
            }
        }
    }
}
