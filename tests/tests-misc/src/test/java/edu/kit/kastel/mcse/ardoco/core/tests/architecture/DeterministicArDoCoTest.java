/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.tests.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.map.ImmutableMap;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.api.set.MutableSet;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

import edu.kit.kastel.mcse.ardoco.core.architecture.NoHashCodeEquals;
import edu.kit.kastel.mcse.ardoco.core.architecture.UserReviewedDeterministic;

@AnalyzeClasses(packages = "edu.kit.kastel.mcse.ardoco.core")
public class DeterministicArDoCoTest {
    @ArchTest
    public static final ArchRule forbidUnorderedSetsAndMaps = noClasses().that()
            .resideOutsideOfPackages("..tests..")
            .and()
            .resideOutsideOfPackages("..codetraceability..", "..arcotl..") // TODO: FixMe: Ignore ArCoTL for now ..
            .and()
            .areNotAnnotatedWith(UserReviewedDeterministic.class)
            .should()
            .accessClassesThat()
            .haveNameMatching(forbidden());

    private static String forbidden() {
        Set<Class<?>> forbiddenClasses = Set.of(Set.class, HashSet.class, MutableSet.class, ImmutableSet.class, Sets.class, //
                Map.class, HashMap.class, MutableMap.class, ImmutableMap.class, Maps.class //
        );
        return forbiddenClasses.stream().map(Class::getName).reduce((a, b) -> a + "|" + b).orElseThrow();
    }

    @ArchTest
    public static final ArchRule forbidEqualsAndHashCodeInCertainClasses = noClasses().that()
            .areAnnotatedWith(NoHashCodeEquals.class)
            .should(implementEqualsOrHashCode());

    private static ArchCondition<? super JavaClass> implementEqualsOrHashCode() {
        return new ArchCondition<>("implement equals or hashCode") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents conditionEvents) {
                var methods = javaClass.getAllMethods();
                for (var method : methods) {
                    if (!method.getFullName().contains(javaClass.getFullName()))
                        continue;
                    if (method.getName().equals("hashCode") || method.getName().equals("equals")) {
                        conditionEvents.add(new SimpleConditionEvent(method, true, "Class " + javaClass.getName() + " implements " + method.getFullName()));
                    }
                }
            }
        };
    }
}
