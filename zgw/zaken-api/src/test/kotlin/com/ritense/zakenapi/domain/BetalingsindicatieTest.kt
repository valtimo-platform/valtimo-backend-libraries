package com.ritense.zakenapi.domain

import com.fasterxml.jackson.databind.node.TextNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.treeToValue
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class BetalingsindicatieTest {

    private val objectMapper = jacksonObjectMapper()

    @Test
    fun `should deserialize to null when empty`() {
        val result: Betalingsindicatie = objectMapper.treeToValue(TextNode(""))

        assertNull(result)
    }

    @Test
    fun `should deserialize to null when value does not exist`() {
        val result: Betalingsindicatie = objectMapper.treeToValue(TextNode("invalid key"))

        assertNull(result)
    }

    @Test
    fun `should deserialize to enum when key exists`() {
        val result: Betalingsindicatie = objectMapper.treeToValue(TextNode(Betalingsindicatie.NVT.key))

        assertEquals(Betalingsindicatie.NVT, result)
    }
}