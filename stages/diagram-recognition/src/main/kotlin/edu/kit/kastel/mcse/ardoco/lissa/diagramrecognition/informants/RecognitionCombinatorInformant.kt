package edu.kit.kastel.mcse.ardoco.lissa.diagramrecognition.informants

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Box
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Classification
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Diagram
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.TextBox
import edu.kit.kastel.mcse.ardoco.core.common.util.DataRepositoryHelper
import edu.kit.kastel.mcse.ardoco.core.data.DataRepository
import edu.kit.kastel.mcse.ardoco.core.pipeline.agent.Informant
import edu.kit.kastel.mcse.ardoco.lissa.diagramrecognition.boundingBox
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.util.SortedMap
import java.util.stream.IntStream
import javax.imageio.ImageIO

class RecognitionCombinatorInformant(
    dataRepository: DataRepository
) :
    Informant(
            ID,
            dataRepository
        ) {
    companion object {
        const val ID = "RecognitionCombinatorInformant"
    }

    override fun delegateApplyConfigurationToInternalObjects(additionalConfiguration: SortedMap<String, String>?) {
        // Not needed
    }

    override fun process() {
        val diagramRecognitionState = DataRepositoryHelper.getDiagramRecognitionState(dataRepository)
        for (diagram in diagramRecognitionState.getUnprocessedDiagrams()) {
            val entities = diagram.boxes.filter { it.classification != Classification.LABEL }
            val texts = diagram.textBoxes
            removeLabelBoxes(diagram)
            combineBoxesAndText(entities, texts)
            calculateDominatingColors(diagram.location.readBytes(), entities)
            combineTextBoxesInBoxes(diagram)
        }
    }

    private fun removeLabelBoxes(diagram: Diagram) {
        diagram.boxes.filter { it.classification == Classification.LABEL }.forEach(diagram::removeBox)
    }

    private fun combineBoxesAndText(
        entities: List<Box>,
        textsUnfiltered: List<TextBox>
    ) {
        // Boxes without parents are tree roots
        var boxes = entities.filter { it.parent.isEmpty }
        var texts = textsUnfiltered.filter { it.text.length >= 2 }

        for (box in boxes) {
            // TODO Could also only pass the remaining texts here instead of passing all texts to each node, but will leave it like this for now, not sure of the impact
            combineBoxesAndTextForTree(box, texts)
        }
    }

    /**
     * Depth first search
     */
    private fun combineBoxesAndTextForTree(
        root: Box,
        texts: List<TextBox>
    ): List<TextBox> {
        val children = root.children

        var remainingTexts = texts
        for (child in children) {
            if (child is Box) {
                remainingTexts = combineBoxesAndTextForTree(child, remainingTexts)
            }
        }

        val mutableRemainingTexts = remainingTexts.toMutableList()

        val intersects =
            remainingTexts.map { it to root.box.boundingBox().iou(it.absoluteBox().boundingBox()) }

        val results = intersects.filter { it.second.areaIntersect / it.first.area() > 0.85 }

        results.forEach {
            logger.info("Found {} intersects with {}", intersects.size, it.first.text)
            root.addTextBox(it.first)
            mutableRemainingTexts.remove(it.first)
        }

        return mutableRemainingTexts
    }

    private fun calculateDominatingColors(
        imageData: ByteArray,
        boxes: List<Box>
    ) {
        val image = ByteArrayInputStream(imageData).use { ImageIO.read(it) }
        boxes.forEach { calculateDominatingColorForBox(image, it) }
    }

    private fun calculateDominatingColorForBox(
        image: BufferedImage,
        box: Box
    ) {
        val pixels = getPixels(image, box.box.toTypedArray())

        val count = pixels.size
        if (count == 0) return

        val pixelCount =
            pixels.groupingBy { it }.eachCount().toList().sortedByDescending { it.second }
        val mostPixel = pixelCount[0]
        if (mostPixel.second <= count / 2) return

        box.dominatingColor = Color(mostPixel.first)
        setColorsOfTexts(image, box)
    }

    private fun getPixels(
        image: BufferedImage,
        box: Array<Int>
    ): List<Int> {
        val result = mutableListOf<Int>()
        for (x in IntStream.range(box[0], box[2])) for (y in IntStream.range(
            box[1],
            box[3]
        )) result.add(
            image.getRGB(x, y)
        )
        return result
    }

    private fun setColorsOfTexts(
        image: BufferedImage,
        box: Box
    ) {
        for (text in box.texts) {
            val pixels = getPixels(image, text.absoluteBox().toTypedArray())
            val count = pixels.size
            if (count == 0) continue
            val pixelCount =
                pixels.groupingBy { it }.eachCount().toList().sortedByDescending { it.second }
            val textColor = pixelCount.find { (rgb, _) -> rgb != box.dominatingColor.rgb }
            if (textColor != null) text.dominatingColor = Color(textColor.first)
        }
    }

    private fun combineTextBoxesInBoxes(diagram: Diagram) {
        val boxes = diagram.boxes.filter { it.classification != Classification.LABEL }
        for (box in boxes) {
            if (box.texts.size <= 1) continue

            // Use ARGB(0,0,0,0) as replacement for null
            val textGroups = box.texts.sortedBy { it.xCoordinate }.sortedBy { it.yCoordinate }.groupBy { it.dominatingColor ?: 0 }

            for ((_, texts) in textGroups) {
                val text = texts.joinToString(" ") { it.text }.replace(Regex("\\s+"), " ")
                val x1 = texts.map { it.xCoordinate }.min()
                val y1 = texts.map { it.yCoordinate }.min()
                val x2 = texts.map { it.xCoordinate + it.width }.max()
                val y2 = texts.map { it.yCoordinate + it.height }.max()
                val width = x2 - x1
                val height = y2 - y1
                val confidence = texts.map { it.confidence }.average()
                val dominantColor = texts.firstNotNullOfOrNull { it.dominatingColor }

                val textBox = TextBox(x1, y1, width, height, confidence, text, dominantColor)
                texts.forEach { box.removeTextBox(it) }
                box.addTextBox(textBox)
            }
        }
    }
}
