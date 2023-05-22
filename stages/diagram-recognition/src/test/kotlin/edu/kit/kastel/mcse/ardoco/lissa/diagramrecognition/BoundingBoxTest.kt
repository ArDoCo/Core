package edu.kit.kastel.mcse.ardoco.lissa.diagramrecognition

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class BoundingBoxTest {
    @Test
    fun testIOU() {
        val box1 = BoundingBox(0.0, 0.0, 10.0, 10.0)
        val box2 = BoundingBox(7.0, 7.0, 11.0, 11.0)
        val iou = box1.iou(box2)

        Assertions.assertEquals(9.0, iou.areaIntersect)
        Assertions.assertEquals(100 + 16.0 - 9.0, iou.areaUnion)
        Assertions.assertEquals(9.0 / (100 + 16 - 9), iou.iou)
    }

    @Test
    fun testIOUNoIntersect() {
        val box1 = BoundingBox(0.0, 0.0, 5.0, 5.0)
        val box2 = BoundingBox(7.0, 7.0, 11.0, 11.0)
        val iou = box1.iou(box2)

        Assertions.assertEquals(0.0, iou.areaIntersect)
        Assertions.assertEquals(25.0 + 16.0, iou.areaUnion)
        Assertions.assertEquals(0.0, iou.iou)
    }
}
