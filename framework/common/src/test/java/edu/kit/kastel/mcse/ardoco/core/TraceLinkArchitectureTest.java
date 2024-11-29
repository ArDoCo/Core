package edu.kit.kastel.mcse.ardoco.core;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import edu.kit.kastel.mcse.ardoco.core.api.tracelink.TraceLink;

@AnalyzeClasses(packages = "edu.kit.kastel.mcse.ardoco")
class TraceLinkArchitectureTest {
    private static final DescribedPredicate<JavaClass> isSubclassOfTraceLink = new DescribedPredicate<>("subclass of " + TraceLink.class) {
        @Override
        public boolean test(JavaClass clazz) {
            return clazz.isAssignableTo(TraceLink.class);
        }
    };

    @ArchTest
    static final ArchRule onlyTraceLinkAsReturnType = methods().that()
            .haveRawReturnType(isSubclassOfTraceLink)
            .and()
            .areNotPrivate()
            .should()
            .haveRawReturnType(TraceLink.class)
            .because("the specific subclasses of TraceLink shall not be used as return type in non-private methods");
}
