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
        val zipEntry = ZipInputStream(ByteArrayInputStream(bytes)).nextEntry

        assertThat(zipEntry).isNotNull
    }

}