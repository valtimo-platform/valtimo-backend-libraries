/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
 *
 * Licensed under EUPL, Version 1.2 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ritense.smartdocuments.client

import com.ritense.smartdocuments.BaseSmartDocumentsIntegrationTest
import com.ritense.smartdocuments.domain.DocumentFormatOption.DOCX
import com.ritense.smartdocuments.domain.SmartDocumentsRequest
import com.ritense.smartdocuments.domain.SmartDocumentsRequest.Selection
import com.ritense.smartdocuments.domain.SmartDocumentsRequest.SmartDocument
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import java.util.Base64

internal class SmartDocumentsClientIntTest : BaseSmartDocumentsIntegrationTest() {

    @Autowired
    lateinit var smartDocumentsClient: SmartDocumentsClient

    @Test
    fun `should generate file from response even though base64 contains escaped unicode characters that are split by buffering`() {
        val request = SmartDocumentsRequest(
            emptyMap(),
            SmartDocument(Selection("templateGroup", "template"))
        )

        val response = smartDocumentsClient.generateDocumentStream(request, DOCX)

        val docxAsBytes = response.documentData.readAllBytes()
        assertThat(docxAsBytes).hasSize(12284)
    }

    @Test
    fun `unfortunately fails to decode file because of incomplete escaped unicode character due to buffering`() {
        val request = SmartDocumentsRequest(
            emptyMap(),
            SmartDocument(Selection("templateGroup", "template"))
        )
        val response = smartDocumentsClient.generateDocument(request)
        val docxResponse = response.file.first { it.outputFormat == "DOCX" }

        val exception = assertThrows<IllegalArgumentException> {
            Base64.getDecoder().decode(docxResponse.document.data)
        }
        assertThat(exception.message).isEqualTo("Input byte array has incorrect ending byte at 16380")
        assertThat(docxResponse.document.data.substring(16380 - 1)).isEqualTo("\u003d\u003d")
    }

}
