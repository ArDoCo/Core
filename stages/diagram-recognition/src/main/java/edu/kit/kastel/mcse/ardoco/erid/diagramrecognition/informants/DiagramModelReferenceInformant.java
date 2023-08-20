package edu.kit.kastel.mcse.ardoco.erid.diagramrecognition.informants;

import edu.kit.kastel.mcse.ardoco.core.api.Disambiguation;
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Box;
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.DiagramUtil;
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.TextBox;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelInstance;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelStates;
import edu.kit.kastel.mcse.ardoco.core.common.tuple.Pair;
import edu.kit.kastel.mcse.ardoco.core.common.tuple.Triple;
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper;
import edu.kit.kastel.mcse.ardoco.core.common.util.wordsim.WordSimUtils;
import edu.kit.kastel.mcse.ardoco.core.configuration.Configurable;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;
import java.util.*;
import org.eclipse.collections.api.list.ImmutableList;
import org.eclipse.collections.api.list.MutableList;
import org.eclipse.collections.impl.factory.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiagramModelReferenceInformant extends Informant {
  private final static Logger logger =
          LoggerFactory.getLogger(DiagramModelReferenceInformant.class);

  @Configurable
  private double textBoxSimilarityThreshold = 0.5;

  public DiagramModelReferenceInformant(DataRepository dataRepository) {
    super(DiagramModelReferenceInformant.class.getSimpleName(), dataRepository);
  }

  @Override
  public void process() {
    var optModelStates = dataRepository.getData(ModelStates.ID, ModelStates.class);
    if (optModelStates.isEmpty()) {
      logger.warn(String.format("%s couldn't be found, skipping informant",
              ModelStates.class.getSimpleName()));
      return;
    }

    var boxes = Lists.mutable.fromStream(
            DataRepositoryHelper.getDiagramRecognitionState(dataRepository).getDiagrams().stream().flatMap(d -> d.getBoxes().stream()));
    setReferences(boxes, optModelStates.orElseThrow());
  }

  private void setReferences(MutableList<Box> boxes, ModelStates modelStates) {
    var modelIds = modelStates.extractionModelIds();


    var diagramDisambiguations =
            DataRepositoryHelper.getDiagramRecognitionState(dataRepository).getDisambiguations();

    for (var model : modelIds) {
      var modelState = modelStates.getModelExtractionState(model);
      var instances = modelState.getInstances();
      for (var box : boxes) {
        box.setReferences(Set.of());
        var references = getReferencesPerTextBox(box, diagramDisambiguations);
        var similar = similarModelInstance(instances, box, references);
        similar.forEach(s -> logger.info(box + " similar to " + s));
        var isEmpty = similar.isEmpty();
        for (var ref : references.entrySet()) {
          if (isEmpty || similar.stream().anyMatch(t -> t.first().equals(ref.getKey()))) {
            ref.getValue().forEach(box::addReference);
          }
        }
      }
    }
  }

  private MutableList<Triple<TextBox, Double, ModelInstance>> similarModelInstance(ImmutableList<ModelInstance> modelInstances, Box box,
                                                                                   Map<TextBox,
                                                                                           Set<String>> references) {
    var list = Lists.mutable.<Triple<TextBox, Double, ModelInstance>>of();
    for (var entry : references.entrySet()) {
      var textBox = entry.getKey();
      var textBoxRefs = entry.getValue();
      var optPair = getMostSimilarModelInstance(modelInstances, textBox, textBoxRefs);
      if (optPair.isPresent()) {
        var pair = optPair.orElseThrow();
        list.add(new Triple<>(textBox, pair.first(), pair.second()));
      }
    }
    return list;
  }

  private Optional<Pair<Double, ModelInstance>> getMostSimilarModelInstance(ImmutableList<ModelInstance> modelInstances, TextBox textBox,
                                                                            Set<String> references) {
    var max = Double.MIN_VALUE;
    ModelInstance mostSimilarModelInstance = null;
    for (var instance : modelInstances) {
      if (WordSimUtils.areWordsSimilar(textBox.getText(), instance.getFullName()) || references.stream()
              .anyMatch(ref -> WordSimUtils.areWordsSimilar(ref.toLowerCase(Locale.US),
                      instance.getFullName().toLowerCase(Locale.US)))) {
        var similarity = WordSimUtils.getSimilarity(textBox.getText().toLowerCase(Locale.US),
                instance.getFullName().toLowerCase(Locale.US));
        if (similarity > textBoxSimilarityThreshold && similarity > max) {
          max = similarity;
          mostSimilarModelInstance = instance;
        }
      }
    }
    if (max > Double.MIN_VALUE) {
      return Optional.of(new Pair<>(max, mostSimilarModelInstance));
    }
    return Optional.empty();
  }

  private Map<TextBox, Set<String>> getReferencesPerTextBox(Box box,
                                                            List<Disambiguation> disambiguations) {
    var map = new LinkedHashMap<TextBox, Set<String>>();
    var texts = box.getTexts();
    for (TextBox textBox : texts) {
      map.put(textBox, DiagramUtil.getReferences(textBox, Disambiguation.toMap(disambiguations)));
    }
    return map;
  }
}
