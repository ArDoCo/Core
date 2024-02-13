package edu.kit.kastel.mcse.ardoco.lissa.diagramrecognition

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

fun createObjectMapper(): ObjectMapper {
    val objectMapper: ObjectMapper =
        ObjectMapper().configure(SerializationFeature.INDENT_OUTPUT, true)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    objectMapper.setVisibility(
        objectMapper.serializationConfig.defaultVisibilityChecker //
            .withFieldVisibility(JsonAutoDetect.Visibility.ANY) //
            .withGetterVisibility(JsonAutoDetect.Visibility.NONE) //
            .withSetterVisibility(JsonAutoDetect.Visibility.NONE) //
            .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE)
    )
    return objectMapper.registerKotlinModule()
}

fun <E> List<E>.with(other: E): List<E> {
    val list = this.toMutableList()
    list.add(other)
    return list.toList()
}

fun <K, V> Map<K, V>.with(
    key: K,
    value: V
): Map<K, V> {
    val map = this.toMutableMap()
    map[key] = value
    return map.toMap()
}
