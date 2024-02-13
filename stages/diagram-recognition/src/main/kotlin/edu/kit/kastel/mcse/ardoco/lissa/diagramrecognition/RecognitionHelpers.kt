package edu.kit.kastel.mcse.ardoco.lissa.diagramrecognition

import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Box
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Classification
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.Diagram
import edu.kit.kastel.mcse.ardoco.core.api.diagramrecognition.TextBox
import edu.kit.kastel.mcse.ardoco.lissa.DiagramRecognition
import org.apache.hc.client5.http.classic.methods.HttpPost
import org.apache.hc.client5.http.config.RequestConfig
import org.apache.hc.client5.http.impl.classic.BasicHttpClientResponseHandler
import org.apache.hc.client5.http.impl.classic.HttpClients
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.Shape
import java.awt.font.GlyphVector
import java.awt.geom.AffineTransform
import java.awt.image.AffineTransformOp
import java.awt.image.BufferedImage
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO
import kotlin.math.max
import kotlin.math.min

private var upscaleFactor = 1.0
private val maxSize = 4096
private val colors = listOf(Color.RED, Color.GREEN, Color(5, 93, 209), Color.YELLOW, Color.BLACK, Color.ORANGE)
private val logger: Logger = LoggerFactory.getLogger("${DiagramRecognition::class.java.packageName}.Helpers")

fun executeRequest(
    postRequest: HttpPost,
    modifyTimeout: Boolean = true
): String {
    HttpClients.createDefault().use {
        try {
            if (modifyTimeout) {
                val basicConfig = postRequest.config ?: RequestConfig.custom().build()
                val newConfig =
                    RequestConfig.copy(basicConfig).setResponseTimeout(5, TimeUnit.MINUTES).build()
                postRequest.config = newConfig
            }
            val content = it.execute(postRequest, BasicHttpClientResponseHandler())
            return content ?: ""
        } catch (e: IOException) {
            logger.error(e.message, e)
            return ""
        }
    }
}

fun visualize(
    imageStream: InputStream,
    diagram: Diagram,
    destination: OutputStream,
    overlayScale: Float = 1F
) {
    val imageUnscaled = ImageIO.read(imageStream)
    upscaleFactor = determineUpscaleFactor(imageUnscaled)

    var image = BufferedImage((imageUnscaled.width * upscaleFactor).toInt(), (imageUnscaled.height * upscaleFactor).toInt(), BufferedImage.TYPE_INT_ARGB)
    val at = AffineTransform()
    at.scale(upscaleFactor, upscaleFactor)
    val scaleOp = AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR)
    image = scaleOp.filter(imageUnscaled, image)

    val g2d: Graphics2D = image.createGraphics()
    g2d.stroke = BasicStroke((3F * overlayScale))
    g2d.font = g2d.font.deriveFont((g2d.font.size * 4 * overlayScale))

    val colorMap = mutableMapOf<Classification, Color>()
    var currentColor = 0

    // Draw Box
    for (box in diagram.boxes + diagram.textBoxes.map { it.toBox(diagram, true) } + diagram.boxes.flatMap { it.texts.map { tb -> tb.toBox(diagram, false) } }) {
        if (!colorMap.containsKey(box.classification)) {
            colorMap[box.classification] = colors[currentColor]!!
            currentColor++
        }
        val col = colorMap[box.classification]!!
        g2d.color = Color(col.red, col.green, col.blue, 255)
        val coordinates = box.box
        g2d.drawRect(toPixels(coordinates[0]), toPixels(coordinates[1]), toPixels(coordinates[2] - coordinates[0]), toPixels(coordinates[3] - coordinates[1]))
    }

    // Draw Text
    for (box in diagram.boxes + diagram.textBoxes.map { it.toBox(diagram, true) } + diagram.boxes.flatMap { it.texts.map { tb -> tb.toBox(diagram, false) } }) {
        g2d.color = colorMap[box.classification]
        val coordinates = box.box
        if (box.classification == Classification.TEXT || box.classification == Classification.RAWTEXT) {
            paintTextWithOutline(
                g2d,
                box.texts.joinToString { it.text },
                toPixels(coordinates[0]),
                toPixels(coordinates[1] - 2),
                colorMap[box.classification]
            )
        } else {
            paintTextWithOutline(
                g2d,
                Box.getBoundingBoxConcat(box.boundingBox.toCoordinates()),
                toPixels(coordinates[0]),
                toPixels(coordinates[1] - 2),
                colorMap[box.classification]
            )
        }
    }

    g2d.dispose()
    ImageIO.write(image, "png", destination)
}

private fun TextBox.toBox(
    diagram: Diagram,
    rawBox: Boolean
): Box =
    Box(
        diagram,
        this.absoluteBox().map { value -> value }.toIntArray(),
        1.0,
        if (rawBox) "RAWTEXT" else "TEXT",
        mutableListOf(this),
        null
    )

data class BoundingBox(val x1: Double, val y1: Double, val x2: Double, val y2: Double) {
    fun iou(bb: BoundingBox) = intersectionOverUnion(this, bb)
}

/**
 * Calculate the intersection over union (IoU) of two bounding boxes.
 * @param bb1 the first bounding box
 * @param bb2 the second bounding box
 * @return the intersection over union information
 */
fun intersectionOverUnion(
    bb1: BoundingBox,
    bb2: BoundingBox
): IntersectionUnionData {
    val xIntersectRight = max(bb1.x1, bb2.x1)
    val yIntersectDown = max(bb1.y1, bb2.y1)

    val xIntersectLeft = min(bb1.x2, bb2.x2)
    val yIntersectUp = min(bb1.y2, bb2.y2)

    val widthIntersect = xIntersectLeft - xIntersectRight
    val heightIntersect = yIntersectUp - yIntersectDown
    val areaIntersect =
        if (xIntersectRight >= xIntersectLeft || yIntersectDown >= yIntersectUp) 0.0 else widthIntersect * heightIntersect

    val widthBox1 = bb1.x2 - bb1.x1
    val heightBox1 = bb1.y2 - bb1.y1
    val widthBox2 = bb2.x2 - bb2.x1
    val heightBox2 = bb2.y2 - bb2.y1

    val areaBox1 = widthBox1 * heightBox1
    val areaBox2 = widthBox2 * heightBox2

    val areaUnion = areaBox1 + areaBox2 - areaIntersect

    return IntersectionUnionData(areaIntersect, areaUnion, areaIntersect / areaUnion)
}

/**
 * The data class contains all information about the intersection over union.
 * @param areaIntersect the area of the intersection of the two bounding boxes
 * @param areaUnion the area of the union of the two bounding boxes
 * @param iou the intersection over union score of the two bounding boxes
 */
data class IntersectionUnionData(val areaIntersect: Double, val areaUnion: Double, val iou: Double)

/**
 * This method converts a list of exactly 4 numbers to a bounding box. The order of the coordinates in the list has to be x1,y1,x2,y2
 * @param relative defines whether x2 and y2 are relative to x1 and y1.
 * @return the bounding box of the coordinates
 */
fun IntArray.boundingBox(relative: Boolean = false): BoundingBox {
    if (this.size != 4) error("List has to contain 4 elements: x1,y1,x2,y2")
    if (relative) {
        return BoundingBox(
            this[0].toDouble(),
            this[1].toDouble(),
            this[2].toDouble() - this[0].toDouble(),
            this[3].toDouble() - this[1].toDouble()
        )
    }
    return BoundingBox(
        this[0].toDouble(),
        this[1].toDouble(),
        this[2].toDouble(),
        this[3].toDouble()
    )
}

private fun paintTextWithOutline(
    g: Graphics?,
    text: String?,
    x: Int,
    y: Int,
    fillColor: Color?
) {
    val outlineColor = Color.black
    val outlineStroke = BasicStroke(3.0f)
    if (g is Graphics2D) {
        val g2 = g

        // remember original settings
        val originalColor = g2.color
        val originalStroke = g2.stroke
        val originalHints = g2.renderingHints

        // create a glyph vector from your text
        val glyphVector: GlyphVector = g2.font.createGlyphVector(g2.fontRenderContext, text)
        // get the shape object
        val textShape: Shape = glyphVector.outline

        // activate anti aliasing for text rendering (if you want it to look nice)
        g2.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON
        )
        g2.setRenderingHint(
            RenderingHints.KEY_RENDERING,
            RenderingHints.VALUE_RENDER_QUALITY
        )

        // Position
        g2.translate(x, y)

        g2.color = outlineColor
        g2.stroke = outlineStroke
        g2.draw(textShape) // draw outline
        g2.color = fillColor
        g2.fill(textShape) // fill the shape

        // reset to original settings after painting
        g2.color = originalColor
        g2.stroke = originalStroke
        g2.setRenderingHints(originalHints)

        // Reset Position
        g2.translate(-x, -y)
    }
}

private fun toPixels(value: Int): Int {
    return (value * upscaleFactor).toInt()
}

private fun determineUpscaleFactor(image: BufferedImage): Double {
    val maxDim = max(image.width, image.height)
    if (maxSize <= maxDim) return 1.0
    return maxSize.toDouble() / maxDim
}
