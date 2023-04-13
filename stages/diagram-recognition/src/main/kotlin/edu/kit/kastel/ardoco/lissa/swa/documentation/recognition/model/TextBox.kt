package edu.kit.kastel.ardoco.lissa.swa.documentation.recognition.model

import com.fasterxml.jackson.annotation.JsonProperty
import kotlin.math.abs

data class TextBox(
    @JsonProperty val x: Int,
    @JsonProperty val y: Int,
    @JsonProperty val w: Int,
    @JsonProperty val h: Int,
    @JsonProperty val confidence: Double,
    @JsonProperty val text: String,
    @Transient var dominatingColor: Int? = null,
) {
    fun absoluteBox(): List<Double> = listOf(x, y, x + w, y + h).map { it.toDouble() }
    fun area(): Int {
        val box = absoluteBox()
        return abs(box[0] - box[2]).toInt() * abs(box[1] - box[3]).toInt()
    }
}
