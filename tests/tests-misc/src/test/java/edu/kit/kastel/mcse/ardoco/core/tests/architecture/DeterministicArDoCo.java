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

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import edu.kit.kastel.mcse.ardoco.core.api.UserReviewedDeterministic;

@AnalyzeClasses(packages = "edu.kit.kastel.mcse.ardoco.core")
public class DeterministicArDoCo {
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
}
