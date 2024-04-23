package com.ritense.besluitenapi.client

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import java.io.IOException


class EmptyStringToVervalredenDeserializer :  JsonDeserializer<Vervalreden?>() {
    @Throws(IOException::class, JsonProcessingException::class)

    override fun deserialize(jsonParser: JsonParser, context: DeserializationContext?): Vervalreden? {
        val node: JsonNode = jsonParser.readValueAsTree()
        if (node.asText().isEmpty()) {
            return null
        }
        return Vervalreden.valueOf(node.asText().uppercase())
    }
}