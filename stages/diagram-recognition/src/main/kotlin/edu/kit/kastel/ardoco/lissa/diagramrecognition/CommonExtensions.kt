package edu.kit.kastel.ardoco.lissa.diagramrecognition

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.slf4j.Logger
import org.slf4j.spi.LocationAwareLogger
import java.lang.reflect.Field

private var debug = false

fun enableDebug() {
    debug = true
}

fun Logger.syncLogLevel() = setLogLevel(if (debug) LocationAwareLogger.DEBUG_INT else LocationAwareLogger.INFO_INT)

/**
 * Set the log level of a logger.
 * @param[level] the new log level
 */
fun Logger.setLogLevel(level: Int) {
    try {
        val f: Field = this.javaClass.getDeclaredField("currentLogLevel")
        f.isAccessible = true
        f.set(this, level)
    } catch (e: Exception) {
        println("Error while setting log level: ${e.message}")
    }
}

fun createObjectMapper(): ObjectMapper {
    val objectMapper: ObjectMapper = ObjectMapper().configure(SerializationFeature.INDENT_OUTPUT, true)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    objectMapper.setVisibility(
        objectMapper.serializationConfig.defaultVisibilityChecker //
            .withFieldVisibility(JsonAutoDetect.Visibility.ANY) //
            .withGetterVisibility(JsonAutoDetect.Visibility.NONE) //
            .withSetterVisibility(JsonAutoDetect.Visibility.NONE) //
            .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE),
    )
    return objectMapper.registerKotlinModule()
}

fun <E> List<E>.with(other: E): List<E> {
    val list = this.toMutableList()
    list.add(other)
    return list.toList()
}

fun <K, V> Map<K, V>.with(key: K, value: V): Map<K, V> {
    val map = this.toMutableMap()
    map[key] = value
    return map.toMap()
}
