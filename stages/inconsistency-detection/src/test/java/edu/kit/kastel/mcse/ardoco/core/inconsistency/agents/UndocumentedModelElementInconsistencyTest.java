/* Licensed under MIT 2022-2023. */
package edu.kit.kastel.mcse.ardoco.core.inconsistency.agents;

import edu.kit.kastel.mcse.ardoco.core.api.models.Entity;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureComponent;

import edu.kit.kastel.mcse.ardoco.core.api.models.arcotl.architecture.ArchitectureInterface;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.inconsistency.informants.UndocumentedModelElementInconsistencyInformant;

import java.util.HashSet;

/**
 * Tests for the {@link UndocumentedModelElementInconsistencyInformant}.
 */
class UndocumentedModelElementInconsistencyTest {
    private MutableList<Entity> modelInstances;

    @BeforeEach
    void beforeEach() {
        modelInstances = Lists.mutable.empty();
        modelInstances.add(new ArchitectureComponent("DummyRecommender",  "1", new HashSet<>(), new HashSet<>(), new HashSet<>()));
        modelInstances.add(new ArchitectureComponent("ExpertRecommender", "2", new HashSet<>(), new HashSet<>(), new HashSet<>()));
        modelInstances.add(new ArchitectureInterface("Cache", "3", new HashSet<>()));
        modelInstances.add(new ArchitectureComponent("WebUI", "4", new HashSet<>(), new HashSet<>(), new HashSet<>()));
        modelInstances.add(new ArchitectureComponent("Only Suffix", "5", new HashSet<>(), new HashSet<>(), new HashSet<>()));
    }

    @Test
    void filterWithWhitelistTest() {
        var whitelist = Lists.mutable.<String>empty();
        var simpleWhitelistEntry = "Cache";
        whitelist.add(simpleWhitelistEntry);
        var spaceContainingWhitelistEntry = "Only Suffix";
        whitelist.add(spaceContainingWhitelistEntry);
        var regexWhitelistEntry = "\\w*Recommender";
        whitelist.add(regexWhitelistEntry);

        var filteredList = UndocumentedModelElementInconsistencyInformant.filterWithWhitelist(modelInstances, whitelist);
        Assertions.assertAll(//
                () -> Assertions.assertEquals(1, filteredList.size()), //
                () -> Assertions.assertEquals("4", filteredList.get(0).getId()));
    }

    @Test
    void modelInstanceHasTargetedTypeTest() {
        MutableList<String> exactTypes = Lists.mutable.of("ArchitectureComponent");

        Assertions.assertAll(//
                () -> Assertions.assertTrue(UndocumentedModelElementInconsistencyInformant.modelInstanceHasTargetedType(modelInstances.get(0), exactTypes),
                        "Instance 0 with 'exactTypes'"),
                () -> Assertions.assertTrue(UndocumentedModelElementInconsistencyInformant
                                .modelInstanceHasTargetedType(modelInstances.get(1), exactTypes), "Instance 1 with 'exactTypes'"),
                () -> Assertions.assertFalse(UndocumentedModelElementInconsistencyInformant.modelInstanceHasTargetedType(modelInstances.get(2), exactTypes),
                                        "Instance 2 with 'exactTypes'"), () -> Assertions.assertTrue(UndocumentedModelElementInconsistencyInformant
                                                .modelInstanceHasTargetedType(modelInstances.get(3), exactTypes), "Instance 3 with 'exactTypes'"),
                () -> Assertions.assertTrue(UndocumentedModelElementInconsistencyInformant.modelInstanceHasTargetedType(modelInstances.get(4), exactTypes),
                        "Instance 4 with 'types'")
        );
    }

}
