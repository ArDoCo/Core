/* Licensed under MIT 2023. */
package edu.kit.kastel.mcse.ardoco.core.tests.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

import org.eclipse.collections.api.factory.Maps;
import org.eclipse.collections.api.factory.Sets;
import org.eclipse.collections.api.map.ImmutableMap;
import org.eclipse.collections.api.map.MutableMap;
import org.eclipse.collections.api.map.sorted.ImmutableSortedMap;
import org.eclipse.collections.api.map.sorted.MutableSortedMap;
import org.eclipse.collections.api.set.ImmutableSet;
import org.eclipse.collections.api.set.MutableSet;
import org.eclipse.collections.api.set.sorted.ImmutableSortedSet;
import org.eclipse.collections.api.set.sorted.MutableSortedSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaParameterizedType;
import com.tngtech.archunit.core.domain.JavaWildcardType;
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

    private static final Logger logger = LoggerFactory.getLogger(DeterministicArDoCoTest.class);

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

    @ArchTest
    public static final ArchRule ensureContractBetweenEqualsHashCodeAndCompareTo = classes().that()
            .implement(Comparable.class)
            .and()
            .areNotEnums()
            .and()
            .areNotAnonymousClasses() // e.g., type references for jackson
            .and()
            .resideOutsideOfPackages("..codetraceability..", "..arcotl..") // TODO: FixMe: Ignore ArCoTL for now ..
            .should(implementEqualsAndHashCode());

    private static ArchCondition<? super JavaClass> implementEqualsAndHashCode() {
        return new ArchCondition<>("implement equals or hashCode") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents conditionEvents) {
                var methods = javaClass.getAllMethods();
                boolean equals = false;
                boolean hashCode = false;
                for (var method : methods) {
                    if (!method.getFullName().contains(javaClass.getFullName()))
                        continue;

                    if (method.getName().equals("hashCode")) {
                        hashCode = true;
                    } else if (method.getName().equals("equals")) {
                        equals = true;
                    }
                }

                if (equals && hashCode) {
                    conditionEvents.add(new SimpleConditionEvent(javaClass, true, "Class " + javaClass.getName() + " implements equals and hashCode"));
                } else if (equals) {
                    conditionEvents.add(new SimpleConditionEvent(javaClass, false, "Class " + javaClass.getName() + " implements equals but not hashCode"));
                } else if (hashCode) {
                    conditionEvents.add(new SimpleConditionEvent(javaClass, false, "Class " + javaClass.getName() + " implements hashCode but not equals"));
                } else {
                    conditionEvents.add(new SimpleConditionEvent(javaClass, false, "Class " + javaClass.getName() + " implements neither equals nor hashCode"));
                }
            }
        };
    }

    @ArchTest
    public static final ArchRule ensureSortedCollectionsOnlyForComparableTypes = fields().that()
            .haveRawType(SortedSet.class)
            .or()
            .haveRawType(ImmutableSortedSet.class)
            .or()
            .haveRawType(MutableSortedSet.class)
            .should(haveComparableGenericType());

    @ArchTest
    public static final ArchRule ensureSortedMapOnlyForComparableTypes = fields().that()
            .haveRawType(SortedMap.class)
            .or()
            .haveRawType(ImmutableSortedMap.class)
            .or()
            .haveRawType(MutableSortedMap.class)
            .should(haveComparableGenericType());

    @ArchTest
    public static final ArchRule ensureSortedCollectionsOnlyForComparableTypesInReturn = methods().that()
            .haveRawReturnType(SortedSet.class)
            .or()
            .haveRawReturnType(ImmutableSortedSet.class)
            .or()
            .haveRawReturnType(MutableSortedSet.class)
            .should(haveComparableReturn());

    @ArchTest
    public static final ArchRule ensureSortedMapOnlyForComparableTypesInReturn = methods().that()
            .haveRawReturnType(SortedMap.class)
            .or()
            .haveRawReturnType(ImmutableSortedMap.class)
            .or()
            .haveRawReturnType(MutableSortedMap.class)
            .should(haveComparableReturn());

    private static ArchCondition<? super JavaField> haveComparableGenericType() {
        return new ArchCondition<>("have Comparable generic type") {
            @Override
            public void check(JavaField javaField, ConditionEvents conditionEvents) {
                var type = javaField.getType();
                if (type instanceof JavaParameterizedType parameterizedType) {
                    var typeParameter = parameterizedType.getActualTypeArguments().get(0);
                    if ((typeParameter instanceof JavaClass typeParameterClass) && typeParameterClass.getAllRawInterfaces()
                            .stream()
                            .anyMatch(i -> i.getFullName().equals(Comparable.class.getName()))) {
                        conditionEvents.add(new SimpleConditionEvent(javaField, true, "Field " + javaField.getFullName() + " has a Comparable generic type"));
                    } else {
                        conditionEvents.add(new SimpleConditionEvent(javaField, false, "Field " + javaField
                                .getFullName() + " has a non-Comparable generic type"));
                    }
                } else if (type instanceof JavaClass) {
                    // Classes generated from lambdas cannot be checked :(
                    logger.debug("Skipping field {}", javaField.getFullName());
                } else {
                    conditionEvents.add(new SimpleConditionEvent(javaField, false, "Field " + javaField.getFullName() + " is not a parameterized type"));
                }
            }
        };
    }

    private static ArchCondition<? super JavaMethod> haveComparableReturn() {
        return new ArchCondition<>("have Comparable generic type") {
            @Override
            public void check(JavaMethod javaMethod, ConditionEvents conditionEvents) {
                var type = javaMethod.getReturnType();
                if (type instanceof JavaParameterizedType parameterizedType) {
                    var typeParameter = parameterizedType.getActualTypeArguments().get(0);
                    if ((typeParameter instanceof JavaClass typeParameterClass) && typeParameterClass.getAllRawInterfaces()
                            .stream()
                            .anyMatch(i -> i.getFullName().equals(Comparable.class.getName()))) {
                        conditionEvents.add(new SimpleConditionEvent(javaMethod, true, "Method " + javaMethod
                                .getFullName() + " has a Comparable generic type"));
                    } else if ((typeParameter instanceof JavaWildcardType typeParameterWildCard)) {
                        var upperBound = typeParameterWildCard.getUpperBounds().get(0);

                        if (!(upperBound instanceof JavaClass upperBoundClass) || upperBoundClass.getAllRawInterfaces()
                                .stream()
                                .noneMatch(i -> i.getFullName().equals(Comparable.class.getName()))) {
                            conditionEvents.add(new SimpleConditionEvent(javaMethod, false, "Method " + javaMethod
                                    .getFullName() + " has a non-Comparable generic type"));
                            return;
                        }

                        conditionEvents.add(new SimpleConditionEvent(javaMethod, true, "Method " + javaMethod
                                .getFullName() + " has a Comparable generic type"));
                    } else {
                        conditionEvents.add(new SimpleConditionEvent(javaMethod, false, "Method " + javaMethod
                                .getFullName() + " has a non-Comparable generic type"));
                    }
                } else {
                    conditionEvents.add(new SimpleConditionEvent(javaMethod, false, "Method " + javaMethod.getFullName() + " is not a parameterized type"));
                }
            }
        };
    }

}
