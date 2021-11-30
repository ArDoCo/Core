/* Licensed under MIT 2021. */
package edu.kit.kastel.mcse.ardoco.core.recommendationgenerator;


import edu.kit.kastel.mcse.ardoco.core.text.DependencyTag;
import edu.kit.kastel.mcse.ardoco.core.text.IWord;
import edu.kit.kastel.mcse.ardoco.core.text.POSTag;
import java.util.Collections;
import java.util.List;
import org.eclipse.collections.api.factory.Lists;
import org.eclipse.collections.api.list.ImmutableList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class InstanceRelationTest {

    protected IInstanceRelation relation1;
    protected IInstanceRelation relation2;
    protected IRecommendedInstance instance1;
    protected IRecommendedInstance instance2;
    protected IRecommendedInstance instance3;
    protected List<IWord> from1;
    protected List<IWord> to1;
    protected List<IWord> from2;
    protected List<IWord> to2;
    protected IWord relator1;
    protected IWord relator2;

    @BeforeEach
    void beforeEach() {
        instance1 = new RecommendedInstance("i1.name", "i1.type", 1, Lists.immutable.empty(), Lists.immutable.empty());
        instance2 = new RecommendedInstance("i2.name", "i2.type", 1, Lists.immutable.empty(), Lists.immutable.empty());
        instance3 = new RecommendedInstance("i3.name", "i3.type", 1, Lists.immutable.empty(), Lists.immutable.empty());
        relator1 = new Word();
        relator2 = new Word();
        from1 = Collections.singletonList(new Word());
        to1 = Collections.singletonList(new Word());
        from2 = Collections.singletonList(new Word());
        to2 = Collections.singletonList(new Word());
        relation1 = new InstanceRelation(instance1, instance2, relator1, from1, to1);
        relation2 = new InstanceRelation(instance2, instance3, relator2, from2, to2);
    }

    @AfterEach
    void afterEach() {
        instance1 = null;
        instance2 = null;
        instance3 = null;
        relator1 = null;
        relator2 = null;
        from1 = null;
        to1 = null;
        from2 = null;
        to2 = null;
        relation1 = null;
        relation2 = null;
    }

    @Test
    @DisplayName("Test adding new link to InstanceRelation/adding already present links")
    void addLinkTest() {
        int rel1Size = relation1.getSize();
        Assertions.assertFalse(relation1.addLink(relator1, from1, to1));
        Assertions.assertEquals(rel1Size, relation1.getSize());

        Assertions.assertTrue(relation1.addLink(relator2, from2, to2));
        Assertions.assertEquals(rel1Size + 1, relation1.getSize());
    }

    @Test
    @DisplayName("Test matching InstanceRelation with two RecommendedInstances")
    void matchesTest() {
        Assertions.assertTrue(relation1.matches(relation1.getFromInstance(), relation1.getToInstance()));
        Assertions.assertFalse(relation1.matches(relation2.getFromInstance(), relation2.getToInstance()));
    }

    @Test
    @DisplayName("Test checking if a relation already contains a link between specific Words")
    void isInTest() {
        Assertions.assertTrue(relation1.isIn(relator1, from1, to1));
        Assertions.assertFalse(relation1.isIn(relator2, from2, to2));
    }

    protected class Word implements IWord {

        @Override
        public int getSentenceNo() {
            return 0;
        }

        @Override
        public String getText() {
            return null;
        }

        @Override
        public POSTag getPosTag() {
            return null;
        }

        @Override
        public IWord getPreWord() {
            return null;
        }

        @Override
        public IWord getNextWord() {
            return null;
        }

        @Override
        public int getPosition() {
            return 0;
        }

        @Override
        public String getLemma() {
            return null;
        }

        @Override
        public ImmutableList<IWord> getWordsThatAreDependencyOfThis(DependencyTag dependencyTag) {
            return null;
        }

        @Override
        public ImmutableList<IWord> getWordsThatAreDependentOnThis(DependencyTag dependencyTag) {
            return null;
        }
    }
}
