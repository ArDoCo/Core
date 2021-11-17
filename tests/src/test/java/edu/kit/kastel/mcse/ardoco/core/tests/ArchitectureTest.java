package edu.kit.kastel.mcse.ardoco.core.tests;

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
            .resideInAnyPackage("..pipeline..", "..tests..");

    @ArchTest
    public static final ArchRule instancesOnlyAfterRecommendationGenerator = classes().that()
            .haveSimpleName("IModelInstance")
            .should()
            .onlyHaveDependentClassesThat()
            .resideInAnyPackage("..model..", "..connectiongenerator..", "..inconsistency..", "..pipeline..", "..util..", "..datastructures..");

    @ArchTest
    public static final ArchRule layerRule = layeredArchitecture()
            // Layer definition
            .layer("TextExtractor")
            .definedBy("..textextractor..", "..tests..")
            .layer("ModelExtractor")
            .definedBy("..model..", "..tests..")
            .layer("RecommendationGenerator")
            .definedBy("..recommendationgenerator..", "..tests..")
            .layer("ConnectionGenerator")
            .definedBy("..connectiongenerator..", "..tests..")
            .layer("InconsistencyDetection")
            .definedBy("..inconsistency..", "..tests..")
            .layer("Pipeline")
            .definedBy("..pipeline..", "..tests..")
            // rule definition
            .whereLayer("InconsistencyDetection")
            .mayOnlyBeAccessedByLayers("Pipeline")
            .whereLayer("ConnectionGenerator")
            .mayOnlyBeAccessedByLayers("InconsistencyDetection", "Pipeline")
            .whereLayer("RecommendationGenerator")
            .mayOnlyBeAccessedByLayers("ConnectionGenerator", "InconsistencyDetection", "Pipeline")
            .whereLayer("TextExtractor")
            .mayOnlyBeAccessedByLayers("RecommendationGenerator", "ConnectionGenerator", "InconsistencyDetection", "Pipeline")
            .whereLayer("ModelExtractor")
            .mayOnlyBeAccessedByLayers("RecommendationGenerator", "ConnectionGenerator", "InconsistencyDetection", "Pipeline");
}
