package edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class BoundingBoxTest {
    double epsilon = 0.000001d;
    BoundingBox topLeft = new BoundingBox(0, 0, 10, 10);
    BoundingBox bottomLeft = new BoundingBox(0, 10, 10, 20);
    BoundingBox middle = new BoundingBox(5, 5, 15, 15);
    BoundingBox subMiddle = new BoundingBox(7, 7, 13, 13);
    BoundingBox topRight = new BoundingBox(10, 0, 20, 10);
    BoundingBox bottomRight = new BoundingBox(10, 10, 20, 20);

    @Test
    void area() {
        assertEquals(new BoundingBox(5, 5, 15, 15).area(), 100.0, epsilon);
        assertEquals(new BoundingBox(5, 5, 25, 15).area(), 200.0, epsilon);
        assertEquals(new BoundingBox(5, 5, 15, 25).area(), 200.0, epsilon);
    }

    @Test
    void intersect() {
        assertEquals(middle.intersect(middle).orElseThrow().area(), middle.area(), epsilon);
        assertEquals(middle.intersect(topLeft).orElseThrow().area(), 25, epsilon);
        assertEquals(middle.intersect(topRight).orElseThrow().area(), 25, epsilon);
        assertEquals(middle.intersect(bottomLeft).orElseThrow().area(), 25, epsilon);
        assertEquals(middle.intersect(bottomRight).orElseThrow().area(), 25, epsilon);
    }

    @Test
    void union() {
        assertEquals(middle.union(middle), middle.area(), epsilon);
        assertEquals(topLeft.union(topRight), topLeft.area() + topRight.area(), epsilon);
        assertEquals(topLeft.union(middle), 175, epsilon);
    }

    @Test
    void intersectionOverUnion() {
        assertEquals(middle.intersectionOverUnion(middle), 1, epsilon);
        assertEquals(topLeft.intersectionOverUnion(topRight), 0, epsilon);
        assertEquals(topLeft.intersectionOverUnion(middle), 25.0 / 175.0, epsilon);
    }

    @Test
    void contains() {
        assertEquals(middle.contains(middle), 1, epsilon);
        assertEquals(middle.contains(subMiddle), 1, epsilon);
        assertEquals(subMiddle.contains(middle), 0.36, epsilon);
        assertEquals(subMiddle.contains(middle, true), 0.0, epsilon);
        assertEquals(topLeft.contains(topRight), 0, epsilon);
        assertEquals(topLeft.contains(middle), 0.25, epsilon);
    }

    @Test
    void containsEntirely() {
        assertTrue(middle.containsEntirely(middle));
        assertTrue(middle.containsEntirely(subMiddle));
        assertFalse(topLeft.containsEntirely(topRight));
        assertFalse(topLeft.containsEntirely(middle));
    }
}
