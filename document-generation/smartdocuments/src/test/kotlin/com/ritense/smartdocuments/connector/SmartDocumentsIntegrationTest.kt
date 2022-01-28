/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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

package com.ritense.smartdocuments.connector

import com.ritense.smartdocuments.BaseSmartDocumentsIntegrationTest
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.http.MediaType.APPLICATION_PDF
import org.springframework.http.MediaType.APPLICATION_PDF_VALUE

@AutoConfigureWebTestClient(timeout = "36000")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SmartDocumentsIntegrationTest : BaseSmartDocumentsIntegrationTest() {

    @Test
    fun `should generate document`() {
        val generatedDocument = (smartDocumentsConnector as SmartDocumentsConnector).generateDocument(
            "template",
            mapOf("voornaam" to "Jan"),
            APPLICATION_PDF
        )

        Assertions.assertThat(generatedDocument.name).isEqualTo("integration-test.pdf")
        Assertions.assertThat(generatedDocument.extension).isEqualTo("pdf")
        Assertions.assertThat(generatedDocument.size).isEqualTo(23726)
        Assertions.assertThat(generatedDocument.contentType).isEqualTo(APPLICATION_PDF_VALUE)
        Assertions.assertThat(generatedDocument.asByteArray).startsWith("%PDF-1.4".toByteArray().toTypedArray())
    }

}