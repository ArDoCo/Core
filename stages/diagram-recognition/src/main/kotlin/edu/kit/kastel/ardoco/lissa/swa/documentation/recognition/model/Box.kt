package edu.kit.kastel.ardoco.lissa.swa.documentation.recognition.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.UUID
import kotlin.math.abs

data class Box(
    @JsonProperty val uuid: String = UUID.randomUUID().toString(),
    @JsonProperty val box: List<Int>,
    @JsonProperty val confidence: Double,
    @JsonProperty("class") val classification: String,
    @Transient val texts: MutableList<TextBox> = mutableListOf(),
    @Transient var dominatingColor: Int? = null,
) {
    fun area() = abs(box[0] - box[2]) * abs(box[1] - box[3])
}
