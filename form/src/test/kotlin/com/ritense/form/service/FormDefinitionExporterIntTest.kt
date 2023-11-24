package com.ritense.form.service

import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.export.request.FormExportRequest
import com.ritense.form.BaseIntegrationTest
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.support.ResourcePatternUtils
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.StreamUtils

@Transactional(readOnly = true)
class FormDefinitionExporterIntTest @Autowired constructor(
    private val resourceLoader: ResourceLoader,
    private val formDefinitionExportService:FormDefinitionExporter
): BaseIntegrationTest() {

    @Test
    fun `should export form`(): Unit = runWithoutAuthorization {
        val formName = "form-example"
        val request = FormExportRequest(formName)
        val exportFiles = formDefinitionExportService.export(request).exportFiles

        val path = PATH.format(formName)
        val formExport = exportFiles.singleOrNull {
            it.path == path
        }
        requireNotNull(formExport)
        val exportJson = formExport.content.toString(Charsets.UTF_8)
        val expectedJson = ResourcePatternUtils.getResourcePatternResolver(resourceLoader)
            .getResource("classpath:$path")
            .inputStream
            .use { inputStream ->
                StreamUtils.copyToString(inputStream, Charsets.UTF_8)
            }
        JSONAssert.assertEquals(
            expectedJson,
            exportJson,
            JSONCompareMode.NON_EXTENSIBLE
        )
    }

    companion object {
        private const val PATH = "config/form/%s.json"
    }
}