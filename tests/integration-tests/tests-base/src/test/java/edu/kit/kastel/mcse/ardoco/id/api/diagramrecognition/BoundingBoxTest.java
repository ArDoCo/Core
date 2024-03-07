/* Licensed under MIT 2023-2024. */
package edu.kit.kastel.mcse.ardoco.id.api.diagramrecognition;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.BoundingBox;

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
        assertEquals(100.0, new BoundingBox(5, 5, 15, 15).area(), epsilon);
        assertEquals(200.0, new BoundingBox(5, 5, 25, 15).area(), epsilon);
        assertEquals(200.0, new BoundingBox(5, 5, 15, 25).area(), epsilon);
    }

    @Test
    void intersect() {
        assertEquals(middle.area(), middle.intersect(middle).orElseThrow().area(), epsilon);
        assertEquals(25, middle.intersect(topLeft).orElseThrow().area(), epsilon);
        assertEquals(25, middle.intersect(topRight).orElseThrow().area(), epsilon);
        assertEquals(25, middle.intersect(bottomLeft).orElseThrow().area(), epsilon);
        assertEquals(25, middle.intersect(bottomRight).orElseThrow().area(), epsilon);
    }

    @Test
    void union() {
        assertEquals(middle.area(), middle.union(middle), epsilon);
        assertEquals(topLeft.area() + topRight.area(), topLeft.union(topRight), epsilon);
        assertEquals(175, topLeft.union(middle), epsilon);
    }

    @Test
    void intersectionOverUnion() {
        assertEquals(1, middle.intersectionOverUnion(middle), epsilon);
        assertEquals(0, topLeft.intersectionOverUnion(topRight), epsilon);
        assertEquals(25.0 / 175.0, topLeft.intersectionOverUnion(middle), epsilon);
    }

    @Test
    void contains() {
        assertEquals(1, middle.contains(middle), epsilon);
        assertEquals(1, middle.contains(subMiddle), epsilon);
        assertEquals(0.36, subMiddle.contains(middle), epsilon);
        assertEquals(0.0, subMiddle.contains(middle, true), epsilon);
        assertEquals(0, topLeft.contains(topRight), epsilon);
        assertEquals(0.25, topLeft.contains(middle), epsilon);
    }

    @Test
    void containsEntirely() {
        assertTrue(middle.containsEntirely(middle));
        assertTrue(middle.containsEntirely(subMiddle));
        assertFalse(topLeft.containsEntirely(topRight));
        assertFalse(topLeft.containsEntirely(middle));
    }
}
