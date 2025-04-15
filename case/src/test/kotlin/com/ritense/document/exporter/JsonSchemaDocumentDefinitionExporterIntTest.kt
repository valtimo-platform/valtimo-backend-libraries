///*
// * Copyright 2015-2024 Ritense BV, the Netherlands.
// *
// * Licensed under EUPL, Version 1.2 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" basis,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package com.ritense.document.exporter
//
//import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
//import com.ritense.document.BaseIntegrationTest
//import com.ritense.exporter.request.DocumentDefinitionExportRequest
//import org.junit.jupiter.api.Test
//import org.skyscreamer.jsonassert.JSONAssert
//import org.skyscreamer.jsonassert.JSONCompareMode
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.core.io.ResourceLoader
//import org.springframework.core.io.support.ResourcePatternUtils
//import org.springframework.transaction.annotation.Transactional
//import org.springframework.util.StreamUtils
//
//@Transactional(readOnly = true)
//class JsonSchemaDocumentDefinitionExporterIntTest @Autowired constructor(
//    private val resourceLoader: ResourceLoader,
//    private val documentDefinitionExportService: JsonSchemaDocumentDefinitionExporter
//) : BaseIntegrationTest() {
//
//    @Test
//    fun `should export document definition`(): Unit = runWithoutAuthorization {
//        val definition = documentDefinitionService.findLatestByName("person").orElseThrow()
//
//        val request = DocumentDefinitionExportRequest(definition.id().name(), definition.id().version())
//        val exportFiles = documentDefinitionExportService.export(request).exportFiles
//
//        val path = PATH.format(definition.id().name())
//        val personDefinitionExport = exportFiles.singleOrNull {
//            it.path == path
//        }
//        requireNotNull(personDefinitionExport)
//        val resultJson = personDefinitionExport.content.toString(Charsets.UTF_8)
//        val expectedJson = ResourcePatternUtils.getResourcePatternResolver(resourceLoader)
//            .getResource("classpath:$path")
//            .inputStream
//            .use { inputStream ->
//                StreamUtils.copyToString(inputStream, Charsets.UTF_8)
//            }
//        JSONAssert.assertEquals(
//            expectedJson,
//            resultJson,
//            JSONCompareMode.NON_EXTENSIBLE
//        )
//    }
//
//    companion object {
//        private const val PATH = "config/document/definition/%s.schema.json"
//    }
//}