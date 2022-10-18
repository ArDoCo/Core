/* Licensed under MIT 2022. */
package edu.kit.kastel.mcse.ardoco.core.inconsistency.agents;

import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.MutableList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelInstance;
import edu.kit.kastel.mcse.ardoco.core.inconsistency.informants.UndocumentedModelElementInconsistencyInformant;
import edu.kit.kastel.mcse.ardoco.core.model.ModelInstanceImpl;

/**
 * Tests for the {@link UndocumentedModelElementInconsistencyInformant}.
 */
class UndocumentedModelElementInconsistencyTest {
    private MutableList<ModelInstance> modelInstances;

    @BeforeEach
    void beforeEach() {
        modelInstances = Lists.mutable.empty();
        modelInstances.add(new ModelInstanceImpl("DummyRecommender", "BasicComponent", "1"));
        modelInstances.add(new ModelInstanceImpl("ExpertRecommender", "CompositeComponent", "2"));
        modelInstances.add(new ModelInstanceImpl("Cache", "Interface", "3"));
        modelInstances.add(new ModelInstanceImpl("WebUI", "BasicComponent", "4"));
        modelInstances.add(new ModelInstanceImpl("Only Suffix", "Component", "5"));
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
                () -> Assertions.assertEquals("4", filteredList.get(0).getUid()));
    }

    @Test
    void modelInstanceHasTargetedTypeTest() {
        MutableList<String> exactTypes = Lists.mutable.of("BasicComponent", "CompositeComponent");
        MutableList<String> shortenedType = Lists.mutable.of("Component");

        Assertions.assertAll(//
                () -> Assertions.assertTrue(UndocumentedModelElementInconsistencyInformant.modelInstanceHasTargetedType(modelInstances.get(0), exactTypes),
                        "Instance 0 with 'exactTypes'"), () -> Assertions.assertTrue(UndocumentedModelElementInconsistencyInformant
                                .modelInstanceHasTargetedType(modelInstances.get(1), exactTypes), "Instance 1 with 'exactTypes'"), () -> Assertions.assertFalse(
                                        UndocumentedModelElementInconsistencyInformant.modelInstanceHasTargetedType(modelInstances.get(2), exactTypes),
                                        "Instance 2 with 'exactTypes'"), () -> Assertions.assertTrue(UndocumentedModelElementInconsistencyInformant
                                                .modelInstanceHasTargetedType(modelInstances.get(3), exactTypes), "Instance 3 with 'exactTypes'"),
                () -> Assertions.assertFalse(UndocumentedModelElementInconsistencyInformant.modelInstanceHasTargetedType(modelInstances.get(4), exactTypes),
                        "Instance 4 with 'types'"), () -> Assertions.assertTrue(UndocumentedModelElementInconsistencyInformant.modelInstanceHasTargetedType(
                                modelInstances.get(0), shortenedType), "Instance 0 with 'shortenedType'"), () -> Assertions.assertTrue(
                                        UndocumentedModelElementInconsistencyInformant.modelInstanceHasTargetedType(modelInstances.get(1), shortenedType),
                                        "Instance 1 with 'shortenedType'"), () -> Assertions.assertFalse(UndocumentedModelElementInconsistencyInformant
                                                .modelInstanceHasTargetedType(modelInstances.get(2), shortenedType), "Instance 2 with 'shortenedType'"),
                () -> Assertions.assertTrue(UndocumentedModelElementInconsistencyInformant.modelInstanceHasTargetedType(modelInstances.get(3), shortenedType),
                        "Instance 3 with 'shortenedType'"), () -> Assertions.assertTrue(UndocumentedModelElementInconsistencyInformant
                                .modelInstanceHasTargetedType(modelInstances.get(4), shortenedType), "Instance 4 with 'shortenedType'"));
    }

}
