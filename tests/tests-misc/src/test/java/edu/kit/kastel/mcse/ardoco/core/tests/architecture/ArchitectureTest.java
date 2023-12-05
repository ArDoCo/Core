/* Licensed under MIT 2021-2023. */
package edu.kit.kastel.mcse.ardoco.core.tests.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.domain.JavaFieldAccess;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

@AnalyzeClasses(packages = "edu.kit.kastel.mcse.ardoco.core")
public class ArchitectureTest {
    @ArchTest
    public static final ArchRule noDependencyOnExecution = classes().that()
            .resideInAPackage("..execution..")
            .should()
            .onlyHaveDependentClassesThat()
            .resideInAnyPackage("..execution..", "..tests..");
    @ArchTest
    public static final ArchRule modelInstancesOnlyAfterModelExtraction = classes().that()
            .haveSimpleName("ModelInstance")
            .or()
            .haveSimpleName("Model")
            .should()
            .onlyHaveDependentClassesThat()
            .resideInAnyPackage("..models..", "..connectiongenerator..", "..inconsistency..", "..pipeline..", "..common..", "..output..", "..tests..");

    @ArchTest
    public static final ArchRule linksOnlyAfterConnectionGenerator = classes().that()
            .haveSimpleNameEndingWith("Link")
            .should()
            .onlyHaveDependentClassesThat()
            .resideInAnyPackage("..connectiongenerator..", "..codetraceability..", "..tracelinks..", "..inconsistency..", "..pipeline..", "..common..",
                    "..api..", "..tests..");

    @ArchTest
    public static final ArchRule usingLinkAsNamingOnlyInConnectionGenerator = classes().that()
            .haveSimpleNameEndingWith("Link")
            .should()
            .resideInAnyPackage("..models.tracelinks..", "..connectiongenerator..", "..output..", "..tests..");

    @ArchTest
    public static final ArchRule inconsistencyOnlyAfterInconsistencyDetection = classes().that()
            .haveSimpleNameContaining("Inconsistency")
            .should()
            .onlyHaveDependentClassesThat()
            .resideInAnyPackage("..inconsistency..", "..execution..", "..api..", "..common..", "..tests..");

    @ArchTest
    public static final ArchRule layerRule = layeredArchitecture().consideringAllDependencies()
            // Layer definition
            .layer("Common")
            .definedBy("..common..", "..api..", "..tests..")
            .layer("TextExtractor")
            .definedBy("..textextraction..")
            .layer("ModelExtractor")
            .definedBy("..core.models..")
            .layer("RecommendationGenerator")
            .definedBy("..recommendationgenerator..")
            .layer("ConnectionGenerator")
            .definedBy("..connectiongenerator..")
            .layer("InconsistencyDetection")
            .definedBy("..inconsistency..")
            .layer("CodeTraceability")
            .definedBy("..codetraceability..")
            .layer("Pipeline")
            .definedBy("..pipeline..")
            .layer("Execution")
            .definedBy("..execution..")
            // rule definition
            .whereLayer("Execution")
            .mayOnlyBeAccessedByLayers("Common") // Needed for tests
            .whereLayer("InconsistencyDetection")
            .mayOnlyBeAccessedByLayers("Pipeline", "Common", "Execution")
            .whereLayer("ConnectionGenerator")
            .mayOnlyBeAccessedByLayers("CodeTraceability", "InconsistencyDetection", "Pipeline", "Common", "Execution")
            .whereLayer("RecommendationGenerator")
            .mayOnlyBeAccessedByLayers("ConnectionGenerator", "InconsistencyDetection", "Pipeline", "Common", "Execution")
            .whereLayer("TextExtractor")
            .mayOnlyBeAccessedByLayers("RecommendationGenerator", "ConnectionGenerator", "InconsistencyDetection", "Pipeline", "Common", "Execution")
            .whereLayer("ModelExtractor")
            .mayOnlyBeAccessedByLayers("RecommendationGenerator", "ConnectionGenerator", "CodeTraceability", "InconsistencyDetection", "Pipeline", "Common",
                    "Execution");

    @ArchTest
    public static final ArchRule transientRule = fields().that().haveModifier(JavaModifier.TRANSIENT).should(new ArchCondition<>("beAccessedIndirectly") {
        @Override
        public void check(JavaField javaField, ConditionEvents conditionEvents) {
            javaField.getAccessesToSelf().forEach(fieldAccess -> {
                var origin = fieldAccess.getOrigin();
                if (fieldAccess.getAccessType().equals(JavaFieldAccess.AccessType.GET)) {
                    if (origin.isMethod()) {
                        if (origin.getName().equalsIgnoreCase("get" + fieldAccess.getName())) {
                            satisfied(conditionEvents, javaField, null);
                        } else {
                            violated(conditionEvents, javaField, "Method accesses " + origin.getFullName() + " accesses transient field " + javaField
                                    .getFullName() + ", but is not a getter or does not match the name pattern for getters 'get{$FieldName}'");
                        }
                    } else {
                        violated(conditionEvents, javaField, "Transient field " + javaField
                                .getFullName() + " has to be accessed by a getter with name 'get{$fieldName}'");
                    }
                } else {
                    if (origin.isConstructor()) {
                        violated(conditionEvents, javaField, "Transient field " + javaField.getFullName() + " has to be set outside of the constructor");
                    } else {
                        satisfied(conditionEvents, javaField, null);
                    }
                }
            });
        }
    }).orShould(new ArchCondition<>("belongToClassWithCustomSerialization") {
        @Override
        public void check(JavaField javaField, ConditionEvents conditionEvents) {
            if (javaField.getOwner().getMethods().stream().anyMatch(method -> method.getName().equalsIgnoreCase("writeObject"))) {
                satisfied(conditionEvents, javaField, null);
            } else {
                violated(conditionEvents, javaField, "Transient field " + javaField.getFullName() + " doesn't belong to a class with a custom serialization");
            }
        }
    });

    private static void satisfied(ConditionEvents events, Object location, String message) {
        var event = new SimpleConditionEvent(location, true, message);
        events.add(event);
    }

    private static void violated(ConditionEvents events, Object location, String message) {
        var event = new SimpleConditionEvent(location, false, message);
        events.add(event);
    }
}
