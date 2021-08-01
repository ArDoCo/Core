package edu.kit.kastel.mcse.ardoco.core.ontology;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.vocabulary.XSD;

/**
 * This class represents an ordered list that is saved and backed in an ontology. Therefore, all operations read and/or
 * write from/to the ontology.
 *
 * To create an {@link OrderedOntologyList}, use {@link OrderedOntologyList.Factory}. For this, you need an existing
 * {@link OntologyConnector}.
 *
 * @author Jan Keim
 *
 */
public class OrderedOntologyList implements List<Individual> {
    public static final String LIST_BASE_URI = "https://informalin.github.io/knowledgebases/external/olo/orderedlistontology.owl#";
    public static final String LIST_PREFIX = "olo";
    protected static final String LIST_CLASS = "OrderedList";
    private static final String LIST_SLOT_CLASS = "Slot";
    private static final String LIST_PROPERTY_ORDERED_LIST = "ordered_list";
    private static final String LIST_PROPERTY_LENGTH = "length";
    private static final String LIST_PROPERTY_SLOT = "slot";
    private static final String LIST_PROPERTY_NEXT = "next";
    private static final String LIST_PROPERTY_PREVIOUS = "previous";
    private static final String LIST_PROPERTY_INDEX = "index";
    private static final String LIST_PROPERTY_ITEM = "item";

    private final OntologyInterface oc;
    private final Individual listIndividual;

    private final String label;

    private int slotIdCounter = 0;

    /**
     * Factory to create {@link OrderedOntologyList}s that are backed by an ontology. Therefore, the {@link Factory}
     * needs access to an existing {@link OntologyConnector} with an ontology
     *
     * @author Jan Keim
     *
     */
    protected static class Factory {
        private OntologyInterface oc;

        protected static Factory get(OntologyInterface oc) {
            return new Factory(oc);
        }

        private Factory(OntologyInterface oc) {
            this.oc = oc;
            oc.setNsPrefix(LIST_PREFIX, LIST_BASE_URI);
        }

        public OrderedOntologyList createFromListIndividual(Individual listIndividual) {
            return new OrderedOntologyList(oc, listIndividual);
        }

        /**
         * Create a new {@link OrderedOntologyList} using the label for the label of the list individual. If the label
         * already belongs to an individual, then gets the individual and uses it as list individual. Caution: if the
         * label belongs to an existing individual that is no ordered list, an {@link IllegalArgumentException} is
         * thrown.
         *
         * @param label Label of the list individual
         */
        protected OrderedOntologyList createFromLabel(String label) {
            return new OrderedOntologyList(oc, label);
        }

        protected Optional<OrderedOntologyList> getOrderedListOntologyFromIndividual(Individual listIndividual) {
            oc.setNsPrefix(LIST_PREFIX, LIST_BASE_URI);

            if (oc.hasOntClass(listIndividual, LIST_PREFIX, LIST_CLASS)) {
                return Optional.of(createFromListIndividual(listIndividual));
            }
            return Optional.empty();
        }

        protected void checkListImport() {
            // check imports
            if (!oc.hasImport(LIST_BASE_URI)) {
                oc.addOntologyImport(LIST_BASE_URI);
            }

            // set prefix map
            oc.setNsPrefix(LIST_PREFIX, LIST_BASE_URI);
        }

    }

    private OrderedOntologyList(OntologyInterface oc, Individual listIndividual) {
        this.oc = oc;
        this.listIndividual = listIndividual;
        var potLabel = oc.getLabel(listIndividual);
        if (potLabel == null) {
            label = oc.getLocalName(listIndividual);
        } else {
            label = potLabel;
        }
    }

    private OrderedOntologyList(OntologyInterface oc, String label) {
        this.oc = oc;
        this.label = label;
        var listClassUri = oc.createUri(LIST_PREFIX, LIST_CLASS);
        var listOpt = oc.getIndividual(label);
        if (listOpt.isPresent()) {
            listIndividual = listOpt.get();
            if (!oc.hasOntClass(listIndividual, listClassUri)) {
                throw new IllegalArgumentException("Provided a label of an invalid individual");
            }
        } else {
            var listClass = oc.getClassByIri(listClassUri).orElseThrow();
            var list = oc.addIndividualToClass(label, listClass);
            listIndividual = list;
            setLength(0);
        }

    }

    public Individual getListIndividual() {
        return listIndividual;
    }

    public List<Individual> toList() {
        return readElements();
    }

    private List<Individual> readElements() {
        var headSlot = getHead();
        if (headSlot.isEmpty()) {
            return new ArrayList<>();
        }
        return collectListElements(headSlot.get());
    }

    private Optional<Individual> getHead() {
        var headSlotNode = oc.getPropertyValue(listIndividual, getSlotProperty());
        return oc.transformIntoIndividual(headSlotNode);
    }

    private Optional<Individual> getLastSlot() {
        var lastIndex = size() - 1;
        if (lastIndex < 0) {
            return Optional.empty();
        }
        var lastSlot = getSlot(lastIndex);
        return Optional.ofNullable(lastSlot);
    }

    private void setHead(Individual individual) {
        listIndividual.setPropertyValue(getSlotProperty(), individual);
    }

    private List<Individual> collectListElements(Individual headSlot) {
        List<Individual> individuals = new ArrayList<>();
        var currSlot = headSlot;
        while (currSlot != null) {
            var curr = extractItemOutOfSlot(currSlot);
            if (curr.isPresent()) {
                individuals.add(curr.get());
            }
            var nextNode = currSlot.getPropertyValue(getNextProperty());
            if (nextNode != null && nextNode.canAs(Individual.class)) {
                currSlot = nextNode.as(Individual.class);
            } else {
                currSlot = null;
            }
        }
        return individuals;
    }

    private Optional<Individual> extractItemOutOfSlot(Individual slot) {
        if (slot == null) {
            return Optional.empty();
        }
        var itemNode = slot.getPropertyValue(getItemProperty());
        if (itemNode == null) {
            return Optional.empty();
        }
        return Optional.of(itemNode.as(Individual.class));
    }

    private void setLength(int length) {
        oc.setPropertyToIndividual(listIndividual, getLengthProperty(), length, XSD.nonNegativeInteger.toString());
    }

    private Individual getNext(Individual individual) {
        if (individual == null) {
            return null;
        }
        return getIndividualFromProperty(getNextProperty(), individual);
    }

    private Individual getPrevious(Individual individual) {
        if (individual == null) {
            return null;
        }
        return getIndividualFromProperty(getPreviousProperty(), individual);
    }

    private Individual getIndividualFromProperty(OntProperty property, Individual individual) {
        if (property == null) {
            return null;
        }
        var propertyNode = individual.getPropertyValue(property);
        if (propertyNode != null && propertyNode.canAs(Individual.class)) {
            return propertyNode.as(Individual.class);
        } else {
            return null;
        }
    }

    private void setNext(Individual prev, Individual next) {
        if (prev == null || next == null) {
            return;
        }
        prev.setPropertyValue(getNextProperty(), next);
    }

    private void setPrevious(Individual prev, Individual next) {
        if (prev == null || next == null) {
            return;
        }
        next.setPropertyValue(getPreviousProperty(), prev);
    }

    @Override
    public int size() {
        return oc.getPropertyIntValue(listIndividual, getLengthProperty()).orElse(-1);
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean add(Individual individual) {
        add(size(), individual);
        return true;
    }

    @Override
    public void add(int index, Individual individual) {
        if (index > size() || index < 0) {
            throw new IndexOutOfBoundsException();
        }
        // create slot
        var slotName = getSlotName();
        var newSlot = oc.addIndividualToClass(slotName, getSlotClass());
        oc.setPropertyToIndividual(newSlot, getOrderedListProperty(), listIndividual);
        oc.setPropertyToIndividual(newSlot, getItemProperty(), individual);
        oc.setPropertyToIndividual(newSlot, getIndexProperty(), index, XSD.positiveInteger.getURI());

        Individual prev = null;
        var curr = getHead().orElse(null);
        var i = 0;
        while (i < index && curr != null) {
            prev = curr;
            curr = getNext(curr);
            i++;
        }
        if (i < index) {
            throw new IllegalStateException("Could not traverse list far enough although the list should contain enough elements.");
        }

        // add slot to list
        if (prev != null) {
            setNext(prev, newSlot);
            setPrevious(prev, newSlot);
        } else {
            setHead(newSlot);
        }

        if (curr != null) {
            setNext(newSlot, curr);
            setPrevious(newSlot, curr);
        }

        updateList(curr, i);
        setLength(size() + 1);
    }

    @Override
    public boolean addAll(Collection<? extends Individual> individuals) {
        if (Objects.isNull(individuals)) {
            return false;
        }
        var startSize = size();
        var index = startSize - 1;
        Individual lastSlot = null;
        if (index >= 0) {
            lastSlot = getSlot(index);
        }

        for (var individual : individuals) {
            index++;
            var slotName = getSlotName();
            var newSlot = oc.addIndividualToClass(slotName, getSlotClass());
            oc.setPropertyToIndividual(newSlot, getOrderedListProperty(), listIndividual);
            oc.setPropertyToIndividual(newSlot, getItemProperty(), individual);
            oc.setPropertyToIndividual(newSlot, getIndexProperty(), XSD.positiveInteger.getURI());

            if (index == 0) {
                setHead(newSlot);
            } else {
                setNext(lastSlot, newSlot);
                setPrevious(lastSlot, newSlot);
            }
            lastSlot = newSlot;
        }

        setLength(index + 1);

        return true;
    }

    private String getSlotName() {
        return label + "_slot_" + slotIdCounter++;
    }

    @Override
    public boolean addAll(int index, Collection<? extends Individual> c) {
        if (index > size() || index < 0) {
            throw new IndexOutOfBoundsException();
        }
        var currIndex = index;
        for (var individual : c) {
            add(currIndex++, individual);
        }
        return !c.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        if (o instanceof Individual individual) {
            var slot = getSlotWhoseItemEquals(individual);
            return slot.isPresent();
        }
        return false;
    }

    private Optional<Individual> getSlotWhoseItemEquals(Individual individual) {
        var headOpt = getHead();
        if (headOpt.isEmpty()) {
            return Optional.empty();
        }
        var currSlot = headOpt.get();
        while (currSlot != null) {
            var curr = extractItemOutOfSlot(currSlot);
            if (curr.isPresent()) {
                var currIndividual = curr.get();
                if (currIndividual.equals(individual)) {
                    return Optional.of(currSlot);
                }
            }

            currSlot = getNext(currSlot);
        }
        return Optional.empty();
    }

    @Override
    public Iterator<Individual> iterator() {
        return toList().iterator();
    }

    @Override
    public Object[] toArray() {
        return toList().toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return toList().toArray(a);
    }

    @Override
    public boolean remove(Object o) {
        if (!contains(o)) {
            return false;
        }
        if (o instanceof Individual individual) {
            var slot = getSlotWhoseItemEquals(individual);
            if (slot.isPresent()) {
                return removeSlot(slot.get());
            }
        }
        return false;
    }

    private boolean removeSlot(Individual individual) {
        if (individual == null) {
            return false;
        }
        var removedIndex = oc.getPropertyIntValue(individual, getIndexProperty()).orElse(-1);
        var prevNode = oc.getPropertyValue(individual, getPreviousProperty());
        var nextNode = oc.getPropertyValue(individual, getNextProperty());

        var prev = oc.transformIntoIndividual(prevNode);
        var next = oc.transformIntoIndividual(nextNode);

        // set the next of previous node
        if (prev.isPresent()) {
            if (next.isEmpty()) {
                oc.removeAllOfProperty(prev.get(), getNextProperty());
            } else {
                oc.setPropertyToIndividual(prev.get(), getNextProperty(), next.get());
            }
        }

        // set the previous of next node
        if (next.isPresent()) {
            if (prev.isEmpty()) {
                oc.removeAllOfProperty(next.get(), getPreviousProperty());
            } else {
                oc.setPropertyToIndividual(next.get(), getPreviousProperty(), prev.get());
            }
        }

        updateList(next.orElse(null), removedIndex);
        oc.removeIndividual(individual);
        return true;
    }

    private void setSlotIndex(Individual slot, int index) {
        oc.setPropertyToIndividual(slot, getIndexProperty(), index, XSD.nonNegativeInteger.toString());
    }

    private void updateList(Individual start, int startIndex) {
        var index = startIndex;
        var curr = start;
        while (curr != null) {
            setSlotIndex(curr, index);

            index++;
            curr = getNext(curr);
        }
        setLength(index);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for (var item : c) {
            if (!contains(item)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        var status = true;
        for (var item : c) {
            var successful = remove(item);
            status &= successful;
        }
        return status;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        var changed = false;
        if (c == null || c.isEmpty()) {
            return changed;
        }

        var curr = getLastSlot().orElse(null);
        while (curr != null) {
            var item = extractItemOutOfSlot(curr);
            if (c.contains(item)) {
                curr = getNext(curr);
            } else {
                var toDelete = curr;
                curr = getPrevious(curr);
                removeSlot(toDelete);
                changed = true;
            }
        }

        return changed;
    }

    @Override
    public void clear() {
        var lastSlotOpt = getLastSlot();
        if (lastSlotOpt.isEmpty()) {
            return;
        }
        var currSlot = lastSlotOpt.get();
        while (currSlot != null) {
            var prev = getPrevious(currSlot);
            removeSlot(currSlot);
            currSlot = prev;
        }
    }

    @Override
    public Individual get(int index) {
        if (index > size()) {
            throw new IndexOutOfBoundsException();
        }
        var slot = getSlot(index);
        return extractItemOutOfSlot(slot).orElseThrow();
    }

    private Individual getSlot(int index) {
        if (index > size() || index < 0) {
            throw new IndexOutOfBoundsException();
        }
        var curr = getHead().orElse(null);
        var i = 0;
        while (i < index && curr != null) {
            curr = getNext(curr);
            i++;
        }
        if (i < index) {
            throw new IllegalStateException("Could not traverse list far enough although the list should contain enough elements.");
        }
        return curr;
    }

    @Override
    public Individual set(int index, Individual element) {

        var slot = getSlot(index);
        if (slot == null) {
            return null;
        }

        var oldItemPropertyValue = oc.getPropertyValue(slot, getItemProperty());
        var oldItemIndividual = oc.transformIntoIndividual(oldItemPropertyValue);

        oc.setPropertyToIndividual(slot, getItemProperty(), element);
        return oldItemIndividual.orElse(null);
    }

    @Override
    public Individual remove(int index) {
        var slot = getSlot(index);
        var individual = extractItemOutOfSlot(slot);
        removeSlot(slot);
        return individual.orElse(null);
    }

    @Override
    public int indexOf(Object o) {
        if (o instanceof Individual individual) {
            var slotOpt = getSlotWhoseItemEquals(individual);
            if (slotOpt.isPresent()) {
                var slot = slotOpt.get();
                return oc.getPropertyIntValue(slot, getIndexProperty()).orElse(-1);
            }
        }
        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        var index = -1;
        if (!(o instanceof Individual)) {
            return index;
        }
        var individual = (Individual) o;
        var headOpt = getHead();
        if (headOpt.isEmpty()) {
            return index;
        }

        var currSlot = headOpt.get();
        var counter = 0;
        while (currSlot != null) {
            var curr = extractItemOutOfSlot(currSlot);
            if (curr.isPresent()) {
                var currIndividual = curr.get();
                if (currIndividual.equals(individual)) {
                    index = counter;
                }
            }

            currSlot = getNext(currSlot);
            counter++;
        }

        return index;
    }

    @Override
    public ListIterator<Individual> listIterator() {
        return toList().listIterator();
    }

    @Override
    public ListIterator<Individual> listIterator(int index) {
        return toList().listIterator(index);
    }

    @Override
    public List<Individual> subList(int fromIndex, int toIndex) {
        throw new NotImplementedException("The OrderedOntologyList does not support subLists.");
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", listIndividual.toString(), listIndividual.getLabel(null));
    }

    private OntProperty getSlotProperty() {
        return oc.getProperty(LIST_PROPERTY_SLOT, LIST_PREFIX).orElseThrow();
    }

    private OntProperty getItemProperty() {
        return oc.getProperty(LIST_PROPERTY_ITEM, LIST_PREFIX).orElseThrow();
    }

    private OntProperty getNextProperty() {
        return oc.getProperty(LIST_PROPERTY_NEXT, LIST_PREFIX).orElseThrow();
    }

    private OntProperty getPreviousProperty() {
        return oc.getProperty(LIST_PROPERTY_PREVIOUS, LIST_PREFIX).orElseThrow();
    }

    private OntProperty getLengthProperty() {
        return oc.getProperty(LIST_PROPERTY_LENGTH, LIST_PREFIX).orElseThrow();
    }

    private OntProperty getIndexProperty() {
        return oc.getProperty(LIST_PROPERTY_INDEX, LIST_PREFIX).orElseThrow();
    }

    private OntProperty getOrderedListProperty() {
        return oc.getProperty(LIST_PROPERTY_ORDERED_LIST, LIST_PREFIX).orElseThrow();
    }

    private OntClass getSlotClass() {
        return oc.getClass(LIST_SLOT_CLASS, LIST_PREFIX).orElseThrow();
    }

}
