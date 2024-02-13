package edu.kit.kastel.mcse.ardoco.erid.diagramrecognition.informants

import edu.kit.kastel.mcse.ardoco.core.api.Disambiguation
import edu.kit.kastel.mcse.ardoco.core.common.util.AbbreviationDisambiguationHelper
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant

/**
 * Responsible for disambiguating abbreviations that are contained in
 * [DiagramElements][edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramElement] and their
 * [TextBoxes][edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.TextBox].
 *
 * @see AbbreviationDisambiguationHelper
 */
class DiagramDisambiguationInformant(dataRepository: DataRepository?) : Informant(DiagramDisambiguationInformant::class.java.getSimpleName(), dataRepository) {
    /**
     * Iterates over all diagram elements and their text boxes. Creates disambiguations for each contained abbreviation.
     *
     * @see AbbreviationDisambiguationHelper.getAbbreviationCandidates
     * @see AbbreviationDisambiguationHelper.disambiguate
     */
    public override fun process() {
        val diagramRecognitionState = DataRepositoryHelper.getDiagramRecognitionState(dataRepository)
        val boxes = diagramRecognitionState.getDiagrams().flatMap { d -> d.getBoxes() }
        for (box in boxes) {
            for (textBox in box.texts) {
                val text = textBox.text
                val abbreviations = AbbreviationDisambiguationHelper.getAbbreviationCandidates(text)
                for (abbreviation in abbreviations) {
                    val disambiguation = AbbreviationDisambiguationHelper.disambiguate(abbreviation)
                    // TODO Add to transient abbreviation cache? Currently not sure about performance impact
                    diagramRecognitionState.addDisambiguation(Disambiguation(abbreviation, disambiguation.toTypedArray<String>()))
                }
            }
        }
    }
}
