/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.api.diagramconsistency.common;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import edu.kit.kastel.mcse.ardoco.core.api.models.Entity;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.ArchitectureModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.CodeModel;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.Model;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureComponent;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureInterface;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureItem;
import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.code.*;
import edu.kit.kastel.mcse.ardoco.core.architecture.Deterministic;

/**
 * Utility to extract elements from models.
 */
@Deterministic
public final class Extractions {
    private Extractions() {
    }

    /**
     * Extracts all (relevant) items from a given architecture model.
     *
     * @param model
     *              The architecture model.
     * @return A map, giving all items that fulfill a certain role.
     */
    public static Map<ElementRole, Set<ArchitectureItem>> extractItemsFromModel(ArchitectureModel model) {
        Set<ArchitectureComponent> components = new LinkedHashSet<>();
        Set<ArchitectureInterface> interfaces = new LinkedHashSet<>();

        for (ArchitectureItem item : model.getEndpoints()) {
            if (item instanceof ArchitectureComponent component) {
                components.add(component);

                interfaces.addAll(component.getProvidedInterfaces());
                interfaces.addAll(component.getRequiredInterfaces());
            }
        }

        return Map.of(ElementRole.ARCHITECTURE_COMPONENT, Set.copyOf(components), ElementRole.ARCHITECTURE_INTERFACE, Set.copyOf(interfaces));
    }

    /**
     * Extracts all (relevant) items from a given code model.
     *
     * @param model
     *              The code model.
     * @return A map, giving all items that fulfill a certain role.
     */
    public static Map<ElementRole, Set<CodeItem>> extractItemsFromModel(CodeModel model) {
        Set<CodePackage> packages = new LinkedHashSet<>();
        Set<ClassUnit> classes = new LinkedHashSet<>();
        Set<InterfaceUnit> interfaces = new LinkedHashSet<>();

        extractPackagesFromModel(model, packages);

        for (CodePackage codePackage : packages) {
            extractItemsFromPackage(codePackage, classes, interfaces);
        }

        return Map.of(ElementRole.CODE_PACKAGE, Set.copyOf(packages), ElementRole.CODE_CLASS, Set.copyOf(classes), ElementRole.CODE_INTERFACE, Set.copyOf(
                interfaces));
    }

    private static void extractPackagesFromModel(CodeModel model, Set<CodePackage> packages) {
        for (CodeItem item : model.getContent()) {
            if (item instanceof CodePackage codePackage) {
                packages.add(codePackage);
                packages.addAll(codePackage.getAllPackages());
            }
        }
    }

    private static void extractItemsFromPackage(CodePackage codePackage, Set<ClassUnit> classes, Set<InterfaceUnit> interfaces) {
        for (CodeCompilationUnit compilationUnit : codePackage.getCompilationUnits()) {
            for (Datatype datatype : compilationUnit.getAllDataTypes()) {
                if (datatype instanceof ClassUnit classUnit) {
                    classes.add(classUnit);
                }

                if (datatype instanceof InterfaceUnit interfaceUnit) {
                    interfaces.add(interfaceUnit);
                }
            }
        }
    }

    private static String getDatatypePath(Datatype datatype) {
        if (datatype.getParentDatatype() != null) {
            return getDatatypePath(datatype.getParentDatatype()) + "." + datatype.getName();
        } else {
            return datatype.getName();
        }
    }

    /**
     * Gets the path of the given code item, a unique identifier.
     *
     * @param item
     *             The code item, which is part of a code model.
     * @return The path of the code item.
     */
    public static String getPath(CodeItem item) {
        if (item instanceof CodeCompilationUnit compilationUnit) {
            return compilationUnit.getPath();
        }

        if (item instanceof CodeModule codeModule) {
            if (codeModule.hasParent()) {
                return getPath(codeModule.getParent()) + "." + codeModule.getName();
            } else {
                return codeModule.getName();
            }
        }

        if (item instanceof Datatype datatype) {
            String packagePath = "";
            if (datatype.getCompilationUnit().hasParent()) {
                packagePath = getPath(datatype.getCompilationUnit().getParent()) + ".";
            }

            return packagePath + getDatatypePath(datatype) + "[" + datatype.getCompilationUnit().getPath() + "]";
        }

        return null;
    }

    /**
     * Extracts all entities from a given model.
     *
     * @param model
     *              The model.
     * @return A map, giving the entity corresponding to a certain ID or path.
     */
    public static Map<String, Entity> extractEntitiesFromModel(Model model) {
        Map<String, Entity> entities = new LinkedHashMap<>();

        if (model instanceof ArchitectureModel architectureModel) {
            Transformations.transform(architectureModel, item -> {
                entities.put(item.getId(), item);
                return item;
            }, (from, to) -> {
            }, (child, parent) -> {
            });
        } else if (model instanceof CodeModel codeModel) {
            Transformations.transform(codeModel, item -> {
                entities.put(Extractions.getPath(item), item);
                return item;
            }, (from, to) -> {
            }, (child, parent) -> {
            });
        } else {
            throw new IllegalArgumentException("Unknown model type: " + model.getClass());
        }

        return entities;
    }
}
