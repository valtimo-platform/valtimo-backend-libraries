package com.ritense.form.service

import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.export.request.FormExportRequest
import com.ritense.form.BaseIntegrationTest
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.support.ResourcePatternUtils
import org.springframework.util.StreamUtils

class FormDefinitionExporterIntTest @Autowired constructor(
    private val resourceLoader: ResourceLoader,
    private val formDefinitionExportService:FormDefinitionExporter
): BaseIntegrationTest() {

    @Test
    fun `should export form`(): Unit = runWithoutAuthorization {
        val formName = "form-example"
        val exportFiles = formDefinitionExportService.export(FormExportRequest(formName));

        val path = FormDefinitionExporter.PATH.format(formName)
        val formExport = exportFiles.singleOrNull {
            it.path == path
        }
        Assertions.assertThat(formExport).isNotNull
        requireNotNull(formExport)
        val exportContent = formExport.content.toString(Charsets.UTF_8)
        val expectedContent = ResourcePatternUtils.getResourcePatternResolver(resourceLoader)
            .getResource("classpath:$path")
            .inputStream
            .use { inputStream ->
                StreamUtils.copyToString(inputStream, Charsets.UTF_8)
            }
        JSONAssert.assertEquals(
            expectedContent,
            exportContent,
            JSONCompareMode.NON_EXTENSIBLE
        )
    }
}