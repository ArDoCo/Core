/* Licensed under MIT 2021-2022. */
package edu.kit.kastel.mcse.ardoco.core.tests.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "edu.kit.kastel.mcse.ardoco.core")
public class ArchitectureTest {

    @ArchTest
    public static final ArchRule noDependencyOnPipeline = classes().that()
            .resideInAPackage("..pipeline..")
            .should()
            .onlyHaveDependentClassesThat()
            .resideInAnyPackage("..pipeline..", "..tests..", "..tests_new..");

    @ArchTest
    public static final ArchRule modelInstancesOnlyAfterModelExtraction = classes().that()
            .haveSimpleName("ModelInstance")
            .should()
            .onlyHaveDependentClassesThat()
            .resideInAnyPackage("..model..", "..connectiongenerator..", "..inconsistency..", "..pipeline..", "..common..", "..output..", "..tests..", "..tests_new..");

    @ArchTest
    public static final ArchRule linksOnlyAfterConnectionGenerator = classes().that()
            .haveSimpleNameEndingWith("Link")
            .should()
            .onlyHaveDependentClassesThat()
            .resideInAnyPackage("..connectiongenerator..", "..inconsistency..", "..pipeline..", "..common..", "..api..", "..tests..", "..tests_new..");

    @ArchTest
    public static final ArchRule usingLinkAsNamingOnlyInConnectionGenerator = classes().that()
            .haveSimpleNameEndingWith("Link")
            .should()
            .resideInAnyPackage("..connectiongenerator..", "..output..", "..tests..", "..tests_new..");

    @ArchTest
    public static final ArchRule inconsistencyOnlyAfterInconsistencyDetection = classes().that()
            .haveSimpleNameContaining("Inconsistency")
            .should()
            .onlyHaveDependentClassesThat()
            .resideInAnyPackage("..inconsistency..", "..pipeline..", "..api..", "..common..", "..tests..", "..tests_new..");

    @ArchTest
    public static final ArchRule layerRule = layeredArchitecture().consideringAllDependencies()
            // Layer definition
            .layer("Common")
            .definedBy("..common..", "..api..", "..tests..", "..tests_new..")
            .layer("TextExtractor")
            .definedBy("..textextraction..")
            .layer("ModelExtractor")
            .definedBy("..model..")
            .layer("RecommendationGenerator")
            .definedBy("..recommendationgenerator..")
            .layer("ConnectionGenerator")
            .definedBy("..connectiongenerator..")
            .layer("InconsistencyDetection")
            .definedBy("..inconsistency..")
            .layer("Pipeline")
            .definedBy("..pipeline..")
            // rule definition
            .whereLayer("Pipeline")
            .mayOnlyBeAccessedByLayers("Common")
            .whereLayer("InconsistencyDetection")
            .mayOnlyBeAccessedByLayers("Pipeline", "Common")
            .whereLayer("ConnectionGenerator")
            .mayOnlyBeAccessedByLayers("InconsistencyDetection", "Pipeline", "Common")
            .whereLayer("RecommendationGenerator")
            .mayOnlyBeAccessedByLayers("ConnectionGenerator", "InconsistencyDetection", "Pipeline", "Common")
            .whereLayer("TextExtractor")
            .mayOnlyBeAccessedByLayers("RecommendationGenerator", "ConnectionGenerator", "InconsistencyDetection", "Pipeline", "Common")
            .whereLayer("ModelExtractor")
            .mayOnlyBeAccessedByLayers("RecommendationGenerator", "ConnectionGenerator", "InconsistencyDetection", "Pipeline", "Common");
}
