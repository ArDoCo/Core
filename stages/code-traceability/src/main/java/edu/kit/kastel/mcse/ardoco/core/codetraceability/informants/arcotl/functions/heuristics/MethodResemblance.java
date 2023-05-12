/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.codetraceability.informants.arcotl.functions.heuristics;

import java.util.HashSet;
import java.util.Set;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureInterface;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureMethod;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.CodeCompilationUnit;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.ControlElement;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.Datatype;
import edu.kit.kastel.mcse.ardoco.core.codetraceability.informants.arcotl.NameComparisonUtils;
import edu.kit.kastel.mcse.ardoco.core.codetraceability.informants.arcotl.computation.Confidence;

public class MethodResemblance extends StandaloneHeuristic {

    @Override
    protected Confidence calculateConfidence(ArchitectureInterface archInterface, CodeCompilationUnit compUnit) {
        int numArchMethods = archInterface.getSignatures().size();
        if (0 == numArchMethods) {
            return new Confidence();
        }

        Set<ControlElement> firstMethods = compUnit.getDeclaredMethods();
        for (Datatype datatype : compUnit.getAllDatatypes()) {
            for (ControlElement codeMethod : datatype.getDeclaredMethods()) {
                if (!isImplementedMethod(codeMethod, datatype) && !isExtendedMethod(codeMethod, datatype)) {
                    firstMethods.add(codeMethod);
                }
            }
        }

        //

        double sumConfidenceValue = 0.0;
        for (ArchitectureMethod archMethod : archInterface.getSignatures()) {
            double maxConfidenceValue = 0.0;
            for (ControlElement codeMethod : firstMethods) {
                double similarity = NameComparisonUtils.areEqual(archMethod, codeMethod) ? 1.0 : 0.0;
                maxConfidenceValue = Math.max(maxConfidenceValue, similarity);
            }
            sumConfidenceValue += maxConfidenceValue;
        }
        if (sumConfidenceValue <= 1) {
            return new Confidence();
        }
        double avgConfidenceValue = (2 * sumConfidenceValue) / (numArchMethods + firstMethods.size());

        // could be necessary if one code-method-name is equal to multiple arch-method-names and there are more arch-methods than code-methods
        avgConfidenceValue = Math.min(avgConfidenceValue, 1.0);

        return new Confidence(avgConfidenceValue);
    }

    private boolean isImplementedMethod(ControlElement codeMethod, Datatype codeType) {
        Set<ControlElement> implMethods = new HashSet<>();
        getAllImplementedInterfaces(codeType).forEach(i -> getAllExtendedTypes(i).forEach(j -> implMethods.addAll(j.getDeclaredMethods())));
        if (implMethods.stream().anyMatch(implMethod -> implMethod.getName().equalsIgnoreCase(codeMethod.getName()))) {
            return true;
        }
        return false;
    }

    private boolean isExtendedMethod(ControlElement codeMethod, Datatype codeType) {
        Set<ControlElement> extendedMethods = new HashSet<>();
        Set<Datatype> extendedTypes = getAllExtendedTypes(codeType);
        extendedTypes.remove(codeType);
        extendedTypes.forEach(i -> extendedMethods.addAll(i.getDeclaredMethods()));
        if (extendedMethods.stream().anyMatch(i -> i.getName().equalsIgnoreCase(codeMethod.getName()))) {
            return true;
        }
        return false;
    }

    // returns extended types + type itself
    public static Set<Datatype> getAllExtendedTypes(Datatype codeType) {
        Set<Datatype> allExtendedTypes = new HashSet<>();
        allExtendedTypes.add(codeType);
        for (Datatype extendedType : codeType.getExtendedTypes()) {
            allExtendedTypes.add(extendedType);
            allExtendedTypes.addAll(getAllExtendedTypes(extendedType));
        }
        return allExtendedTypes;
    }

    public static Set<Datatype> getAllImplementedInterfaces(Datatype codeType) {
        Set<Datatype> allImplementedInterfaces = new HashSet<>();
        for (Datatype extendedType : getAllExtendedTypes(codeType)) {
            allImplementedInterfaces.addAll(extendedType.getImplementedTypes());
        }
        return allImplementedInterfaces;
    }

    @Override
    public String toString() {
        return "MethodResemblance";
    }
}