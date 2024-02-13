/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.core.diagramconsistency.informants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.stream.Collectors;

import org.eclipse.collections.api.bimap.MutableBiMap;
import org.eclipse.collections.impl.bimap.mutable.HashBiMap;

import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.DiagramMatchingModelSelectionState;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.DiagramModelInconsistencyState;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.DiagramModelLinkState;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.DiagramState;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.DiagramUtility;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.Extractions;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.inconsistencies.Inconsistency;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.inconsistencies.rules.AllBoxesMustBeLinked;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.inconsistencies.rules.AllModelEntitiesMustBeRepresented;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.inconsistencies.rules.BoxesMustBeInParent;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.inconsistencies.rules.EntitiesMustBeConnectedExactlyToDependencies;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.inconsistencies.rules.PackagesMustContainAllSubpackagesIfOneIsEmpty;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.inconsistencies.rules.Rule;
import edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common.inconsistencies.rules.SameNameForLinkedElements;
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Box;
import edu.kit.kastel.mcse.ardoco.core.api.models.Entity;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelStates;
import edu.kit.kastel.mcse.ardoco.core.api.models.ModelType;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.ArchitectureModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.Model;
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.DiagramMatchingModelSelectionStateImpl;
import edu.kit.kastel.mcse.ardoco.core.diagramconsistency.DiagramStateImpl;
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant;

/**
 * Searches for inconsistencies between the diagram and the given models.
 */
public class DiagramModelInconsistencyInformant extends Informant {
    private static final List<Rule> BASIC_CONSISTENCY_RULES = List.of(new SameNameForLinkedElements(), new AllBoxesMustBeLinked(),
            new EntitiesMustBeConnectedExactlyToDependencies(), new BoxesMustBeInParent());
    private static final List<Rule> ARCHITECTURE_CONSISTENCY_RULES = addBasicConsistencyRules(List.of(new AllModelEntitiesMustBeRepresented()));
    private static final List<Rule> CODE_CONSISTENCY_RULES = addBasicConsistencyRules(List.of(new PackagesMustContainAllSubpackagesIfOneIsEmpty()));

    /**
     * Creates a new DiagramModelInconsistencyInformant.
     *
     * @param data
     *             The DataRepository.
     */
    public DiagramModelInconsistencyInformant(DataRepository data) {
        super(DiagramModelInconsistencyInformant.class.getSimpleName(), data);
    }

    private static List<Rule> addBasicConsistencyRules(List<Rule> rules) {
        List<Rule> combined = new ArrayList<>();
        combined.addAll(rules);
        combined.addAll(BASIC_CONSISTENCY_RULES);
        return combined;
    }

    private static MutableBiMap<Box, Entity> getTranslatedLinks(MutableBiMap<String, String> links, Map<String, Box> boxes, Map<String, Entity> entities) {
        MutableBiMap<Box, Entity> translatedLinks = new HashBiMap<>();
        for (var link : links.entrySet()) {
            Box box = boxes.get(link.getKey());
            Entity entity = entities.get(link.getValue());
            translatedLinks.put(Objects.requireNonNull(box), Objects.requireNonNull(entity));
        }
        return translatedLinks;
    }

    private static void checkRulesForLinkedElements(MutableBiMap<String, String> links, Map<String, Box> boxes, Map<String, Entity> entities, Context context) {
        for (var link : links.entrySet()) {
            Box box = boxes.remove(link.getKey());
            Entity entity = entities.remove(link.getValue());

            for (Rule rule : context.rules()) {
                for (var inconsistency : rule.check(box, entity)) {
                    context.addInconsistency(inconsistency);
                }
            }
        }
    }

    private static void checkRulesForLooseBoxes(Map<String, Box> boxes, Context context) {
        for (var box : boxes.values()) {
            for (Rule rule : context.rules()) {
                for (var inconsistency : rule.check(box, null)) {
                    context.addInconsistency(inconsistency);
                }
            }
        }
    }

    private static void checkRulesForLooseEntities(Map<String, Entity> entities, Context context) {
        for (var entity : entities.values()) {
            for (Rule rule : context.rules()) {
                for (var inconsistency : rule.check(null, entity)) {
                    context.addInconsistency(inconsistency);
                }
            }
        }
    }

    @Override
    public void process() {
        DataRepository data = this.getDataRepository();

        ModelStates models = data.getData(ModelStates.ID, ModelStates.class).orElse(null);
        DiagramState diagram = data.getData(DiagramState.ID, DiagramStateImpl.class).orElse(null);
        DiagramMatchingModelSelectionState selection = data.getData(DiagramMatchingModelSelectionState.ID, DiagramMatchingModelSelectionStateImpl.class)
                .orElse(null);
        DiagramModelLinkState matching = data.getData(DiagramModelLinkState.ID, DiagramModelLinkState.class).orElse(null);
        DiagramModelInconsistencyState inconsistencies = data.getData(DiagramModelInconsistencyState.ID, DiagramModelInconsistencyState.class).orElse(null);

        if (models == null || diagram == null || selection == null || matching == null || inconsistencies == null) {
            this.logger.error("DiagramModelInconsistencyInformant: Could not find all required data.");
            return;
        }

        for (var selectedModelType : selection.getSelection()) {
            Model model = models.getModel(selectedModelType.getModelId());

            Map<String, Entity> entities;
            List<Rule> rules;

            if (model instanceof ArchitectureModel architectureModel) {
                entities = Extractions.extractEntitiesFromModel(architectureModel);
                rules = ARCHITECTURE_CONSISTENCY_RULES;
            } else if (model instanceof CodeModel codeModel) {
                entities = Extractions.extractEntitiesFromModel(codeModel);
                rules = CODE_CONSISTENCY_RULES;
            } else {
                this.logger.error("DiagramModelLinkInformant: Unknown model type: {}", model.getClass().getSimpleName());
                continue;
            }

            Map<Entity, String> entityToId = entities.entrySet().stream().collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

            Map<String, Box> boxes = DiagramUtility.getBoxes(diagram.getDiagram());

            MutableBiMap<String, String> links = matching.getLinks(selectedModelType);
            MutableBiMap<Box, Entity> translatedLinks = getTranslatedLinks(links, boxes, entities);

            rules.forEach(rule -> rule.setup(diagram.getDiagram(), model, translatedLinks));

            Context context = new Context(selectedModelType, entityToId, rules, inconsistencies);
            checkRulesForLinkedElements(links, boxes, entities, context);
            checkRulesForLooseBoxes(boxes, context);
            checkRulesForLooseEntities(entities, context);

            rules.forEach(Rule::tearDown);
        }
    }

    private record Context(ModelType modelType, Map<Entity, String> entityToId, List<Rule> rules, DiagramModelInconsistencyState inconsistencies) {
        public void addInconsistency(Inconsistency<Box, Entity> inconsistency) {
            this.inconsistencies.addInconsistency(this.modelType, inconsistency.map(Box::getUUID, this.entityToId::get));
        }
    }

    @Override
    protected void delegateApplyConfigurationToInternalObjects(SortedMap<String, String> additionalConfiguration) {
        // Intentionally left empty.
    }
}
