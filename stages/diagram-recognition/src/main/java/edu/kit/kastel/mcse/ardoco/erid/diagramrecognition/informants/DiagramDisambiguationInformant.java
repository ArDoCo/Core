package edu.kit.kastel.mcse.ardoco.erid.diagramrecognition.informants;

import edu.kit.kastel.mcse.ardoco.core.api.Disambiguation;
import edu.kit.kastel.mcse.ardoco.core.common.util.AbbreviationDisambiguationHelper;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.collections.impl.factory.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiagramDisambiguationInformant extends Informant {
  private final static Logger logger =
          LoggerFactory.getLogger(DiagramDisambiguationInformant.class);

  public DiagramDisambiguationInformant(DataRepository dataRepository) {
    super(DiagramDisambiguationInformant.class.getSimpleName(), dataRepository);
  }

  @Override
  public void process() {
    var diagramRecognitionState = DataRepositoryHelper.getDiagramRecognitionState(dataRepository);
    var boxes = Lists.mutable.fromStream(
            diagramRecognitionState.getDiagrams().stream().flatMap(d -> d.getBoxes().stream()));
    for (var box : boxes) {
      var texts = box.getTexts();
      for (var textBox : texts) {
        var text = textBox.getText();
        var abbreviations = AbbreviationDisambiguationHelper.getPossibleAbbreviations(text);
        var meaningsMap = abbreviations.stream().collect(Collectors.toMap(a -> a,
                AbbreviationDisambiguationHelper.getInstance()::disambiguate));
        for (Map.Entry<String, Set<String>> e : meaningsMap.entrySet()) {
          diagramRecognitionState.addDisambiguation(new Disambiguation(e.getKey(),
                  e.getValue().toArray(new String[0])));
        }
      }
    }
  }
}
