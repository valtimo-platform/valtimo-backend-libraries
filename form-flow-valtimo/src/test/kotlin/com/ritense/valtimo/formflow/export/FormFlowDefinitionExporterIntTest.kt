package com.ritense.valtimo.formflow.export

import com.ritense.export.request.FormDefinitionExportRequest
import com.ritense.export.request.FormFlowDefinitionExportRequest
import com.ritense.valtimo.formflow.BaseIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ResourceLoader
import org.springframework.core.io.support.ResourcePatternUtils
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.StreamUtils

@Transactional(readOnly = true)
class FormFlowDefinitionExporterIntTest @Autowired constructor(
    private val resourceLoader: ResourceLoader,
    private val formFlowDefinitionExporter: FormFlowDefinitionExporter
): BaseIntegrationTest() {

    @Test
    fun `should export form flow with forms`() {
        val formFlowKey = "loan"
        val result = formFlowDefinitionExporter.export(
            FormFlowDefinitionExportRequest("${formFlowKey}:latest")
        )

        val exportFile = result.exportFiles.singleOrNull {
            it.path == PATH.format(formFlowKey)
        }

        assertThat(exportFile).isNotNull

        val exportJson = exportFile!!.content.toString(Charsets.UTF_8)
        val expectedJson = ResourcePatternUtils.getResourcePatternResolver(resourceLoader)
            .getResource("classpath:${PATH.format(formFlowKey)}")
            .inputStream
            .use { inputStream ->
                StreamUtils.copyToString(inputStream, Charsets.UTF_8)
            }
        JSONAssert.assertEquals(
            expectedJson,
            exportJson,
            JSONCompareMode.NON_EXTENSIBLE
        )

        assertThat(result.relatedRequests).contains(
            FormDefinitionExportRequest("my-form-definition")
        )
    }

    companion object {
        private const val PATH = "config/form-flow/%s.json"
    }
}