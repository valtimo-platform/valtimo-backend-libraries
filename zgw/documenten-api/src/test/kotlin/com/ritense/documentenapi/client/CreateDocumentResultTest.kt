package com.ritense.documentenapi.client

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class CreateDocumentResultTest {

    @Test
    fun `should get uuid from url`() {
        val result = CreateDocumentResult(
            "https://www.example.com/847789d3-a8b3-469a-ae01-a49a6bd21783",
            "",
            "",
            0L,
            LocalDateTime.now(),
            listOf()
        )

        assertEquals("847789d3-a8b3-469a-ae01-a49a6bd21783", result.getDocumentUUIDFromUrl())
    }

    @Test
    fun `should get UUID from bestanddelen`() {
        val bestandsdeel = Bestandsdelen(
            "https://www.example.com/847789d3-a8b3-469a-ae01-a49a6bd21783",
            0,
            0,
            true,
            ""
        )
        val result = CreateDocumentResult(
            "",
            "",
            "",
            0L,
            LocalDateTime.now(),
            listOf(bestandsdeel)
        )

        assertEquals("847789d3-a8b3-469a-ae01-a49a6bd21783", result.getBestandsdelenIdFromUrl())
    }

    @Test
    fun `should fail gracefully when there are no bestandsdelen`() {
        val result = CreateDocumentResult(
            "",
            "",
            "",
            0L,
            LocalDateTime.now(),
            listOf()
        )

        assertEquals("", result.getBestandsdelenIdFromUrl())
    }

    @Test
    fun `should get lock from bestanddelen`() {
        val bestandsdeel = Bestandsdelen(
            "https://www.example.com/",
            0,
            0,
            true,
            "847789d3-a8b3-469a-ae01-a49a6bd21783"
        )
        val result = CreateDocumentResult(
            "",
            "",
            "",
            0L,
            LocalDateTime.now(),
            listOf(bestandsdeel)
        )

        assertEquals("847789d3-a8b3-469a-ae01-a49a6bd21783", result.getLockFromBestanddelen())
    }

    @Test
    fun `should fail gracefully for lock when there are no bestandsdelen`() {
        val result = CreateDocumentResult(
            "",
            "",
            "",
            0L,
            LocalDateTime.now(),
            listOf()
        )

        assertEquals("", result.getLockFromBestanddelen())
    }

}