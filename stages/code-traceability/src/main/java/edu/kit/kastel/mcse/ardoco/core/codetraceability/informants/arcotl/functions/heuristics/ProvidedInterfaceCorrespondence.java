/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.codetraceability.informants.arcotl.functions.heuristics;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.kit.kastel.mcse.ardoco.core.api.models.Entity;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureComponent;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureInterface;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeCompilationUnit;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeModule;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodePackage;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.Datatype;
import edu.kit.kastel.mcse.ardoco.core.api.models.tracelinks.EndpointTuple;
import edu.kit.kastel.mcse.ardoco.core.codetraceability.informants.arcotl.NameComparisonUtils;
import edu.kit.kastel.mcse.ardoco.core.codetraceability.informants.arcotl.computation.Confidence;

public class ProvidedInterfaceCorrespondence extends DependentHeuristic {

    @Override
    protected Confidence calculateConfidence(ArchitectureComponent archComponent, CodeCompilationUnit compUnit) {
        return calculateProvidedInterfaceCorrespondence(archComponent, compUnit);
    }

    private Confidence calculateProvidedInterfaceCorrespondence(ArchitectureComponent archComponent, CodeCompilationUnit compUnit) {
        if (!getNodeResult().getConfidence(new EndpointTuple(archComponent, compUnit)).hasValue()) {
            return new Confidence();
        }
        if (!compUnit.hasParent()) {
            return new Confidence();
        }

        Set<Entity> interfaceLinks = new HashSet<>();
        for (ArchitectureInterface providedInterface : archComponent.getProvidedInterfaces()) {
            interfaceLinks.addAll(getLinks(providedInterface));
        }

        Set<CodeModule> componentPackage = getPackage(archComponent, compUnit);
        if (containsAny(interfaceLinks, componentPackage)) {
            return new Confidence();
        }

        Set<CodeModule> allPackages = getPackages(archComponent, getLinks(archComponent));
        if (containsAny(interfaceLinks, allPackages)) {
            return new Confidence(1.0);
        }
        return new Confidence();
    }

    private boolean containsAny(Set<Entity> interfaces, Set<CodeModule> componentPackages) {
        if (componentPackages.isEmpty() || interfaces.isEmpty()) {
            return false;
        }
        return interfaces.stream().anyMatch(i -> componentPackages.stream().anyMatch(p -> overrides(p.getAllCompilationUnits(), i)));
    }

    private boolean overrides(Set<CodeCompilationUnit> ces, Entity i) {
        for (CodeCompilationUnit ce : ces) {
            if (getAllOverridenTypes(ce).contains(i)) {
                return true;
            }
        }
        return false;
    }

    private Set<CodeCompilationUnit> getAllOverridenTypes(CodeCompilationUnit ce) {
        Set<CodeCompilationUnit> overridenCompUnits = new HashSet<>();
        Set<Datatype> overridenTypes = new HashSet<>();
        for (Datatype codeType : ce.getAllDataTypes()) {
            overridenTypes.addAll(MethodResemblance.getAllExtendedTypes(codeType));
            MethodResemblance.getAllImplementedInterfaces(codeType).forEach(i -> overridenTypes.addAll(MethodResemblance.getAllExtendedTypes(i)));
        }
        overridenTypes.forEach(t -> overridenCompUnits.add(t.getCompilationUnit()));
        return overridenCompUnits;
    }

    private Set<CodeCompilationUnit> getLinks(Entity ae) {
        Set<CodeCompilationUnit> ces = new HashSet<>();
        Set<Entity> endpoints = getNodeResult().getLinkedEndpoints(ae);
        endpoints.forEach(endpoint -> ces.add((CodeCompilationUnit) endpoint));
        return ces;
    }

    private Set<CodeModule> getPackages(Entity ae, Set<CodeCompilationUnit> ces) {
        Set<CodeModule> packages = new HashSet<>();
        for (CodeCompilationUnit ce : ces) {
            packages.addAll(getPackage(ae, ce));
        }
        return packages;
    }

    private Set<CodeModule> getPackage(Entity ae, CodeCompilationUnit ce) {
        List<CodePackage> cePackages = NameComparisonUtils.getMatchedPackages(ae, ce);
        if (!cePackages.isEmpty()) {
            return Set.of(cePackages.get(cePackages.size() - 1));
        }
        if (ce.hasParent()) {
            return Set.of(ce.getParent());
        }
        return Set.of();
    }

    @Override
    public String toString() {
        return "ProvidedInterfaceCorrespondence";
    }
}
