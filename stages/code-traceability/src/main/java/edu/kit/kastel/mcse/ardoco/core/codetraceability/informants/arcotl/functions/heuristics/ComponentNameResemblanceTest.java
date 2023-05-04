/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.codetraceability.informants.arcotl.functions.heuristics;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.kit.kastel.mcse.ardoco.core.api.models.Entity;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureComponent;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureInterface;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeCompilationUnit;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.EndpointTuple;
import edu.kit.kastel.mcse.ardoco.core.codetraceability.informants.arcotl.NameComparisonUtils;
import edu.kit.kastel.mcse.ardoco.core.codetraceability.informants.arcotl.computation.Confidence;

public class ComponentNameResemblanceTest extends DependentHeuristic {

    private static final Set<String> commonWords = Set.of("Test", "Action", "Impl", "Factory", "Exception");

    @Override
    protected Confidence calculateConfidence(ArchitectureComponent archComponent, CodeCompilationUnit compUnit) {
        return calculateNameResemblance(archComponent, compUnit);
    }

    @Override
    protected Confidence calculateConfidence(ArchitectureInterface archInterface, CodeCompilationUnit compUnit) {
        if (!archInterface.getSignatures().isEmpty()) {
            return new Confidence();
        }
        return calculateNameResemblance(archInterface, compUnit);
    }

    private Confidence calculateNameResemblance(ArchitectureItem archEndpoint, CodeCompilationUnit compUnit) {
        if (getNodeResult().getConfidence(new EndpointTuple(archEndpoint, compUnit)).hasValue()) {
            return new Confidence();
        }
        Set<String> filteredCommonWords = new HashSet<>(commonWords);
        for (Entity ae : getArchModel().getEndpoints()) {
            filteredCommonWords = NameComparisonUtils.removeWords(filteredCommonWords, ae);
        }
        Set<CodeItem> items = compUnit.getAllDatatypesAndSelf();
        if (areSimilar(items, Set.of(archEndpoint), filteredCommonWords)) {
            return new Confidence(1.0);
        }
        Confidence maxConfidence = new Confidence();
        Set<Entity> linkedEndpoints = getNodeResult().getLinkedEndpoints(archEndpoint);
        for (Entity linkedEndpoint : linkedEndpoints) {
            CodeCompilationUnit linkedCompUnit = (CodeCompilationUnit) linkedEndpoint;
            if (!InheritLinks.areInSamePackage(compUnit, linkedCompUnit)) {
                continue;
            }
            if (!areSimilar(items, linkedCompUnit.getAllDatatypesAndSelf(), filteredCommonWords)) {
                continue;
            }
            Confidence extendedConfidence = getNodeResult().getConfidence(new EndpointTuple(archEndpoint, linkedCompUnit));
            if (extendedConfidence.compareTo(maxConfidence) > 0) {
                maxConfidence = extendedConfidence;
            }
        }
        return maxConfidence;
    }

    private static boolean areSimilar(Set<? extends Entity> entities1, Set<? extends Entity> entities2, Set<String> filteredCommonWords) {
        for (var entity1 : entities1) {
            for (var entity2 : entities2) {
                List<String> words1 = NameComparisonUtils.removeWords(entity1, filteredCommonWords);
                List<String> words2 = NameComparisonUtils.removeWords(entity2, filteredCommonWords);
                if (words1.equals(words2)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "ComponentNameResemblanceTest";
    }
}
