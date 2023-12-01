package com.ritense.export

import java.io.ByteArrayInputStream
import java.util.zip.ZipInputStream
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class ExportServiceIntTest @Autowired constructor(
    private val exportService: ExportService
): BaseIntegrationTest() {

    @Test
    fun `should export a zip`() {
        val bytes = exportService.export(TestExportRequest()).toByteArray()
        val zipStream = ZipInputStream(ByteArrayInputStream(bytes))
        val entries = generateSequence { zipStream.nextEntry }
            .toList()

        assertThat(entries.singleOrNull { it.name == "test.txt" }).isNotNull
        assertThat(entries.singleOrNull { it.name == "nested.txt" }).isNotNull
    }

    @Test
    fun `should not result in a stackoverflow`() {
        val bytes = exportService.export(TestStackOverflowExportRequest()).toByteArray()
        val zipStream = ZipInputStream(ByteArrayInputStream(bytes))
        val entries = generateSequence { zipStream.nextEntry }
            .toList()
        assertThat(entries).isEmpty()
    }
}