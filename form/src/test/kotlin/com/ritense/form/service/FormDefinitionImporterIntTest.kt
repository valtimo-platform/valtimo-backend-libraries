/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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

package com.ritense.form.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.form.BaseIntegrationTest
import com.ritense.form.domain.FormIoFormDefinition
import com.ritense.form.repository.FormDefinitionRepository
import com.ritense.importer.ImportRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class FormDefinitionImporterIntTest @Autowired constructor(
    private val formDefinitionImporter: FormDefinitionImporter,
    private val formDefinitionService: FormDefinitionService,
    private val formDefinitionRepository: FormDefinitionRepository,
    private val objectMapper: ObjectMapper,
): BaseIntegrationTest() {
    private val formDefinition = """
        {
            "display": "form",
            "settings": {
                "pdf": {
                    "id": "1ec0f8ee-6685-5d98-a847-26f67b67d6f0",
                    "src": "https://files.form.io/pdf/5692b91fd1028f01000407e3/file/1ec0f8ee-6685-5d98-a847-26f67b67d6f0"
                }
            },
            "components": [
                {
                    "label": "Naam",
                    "key": "person.firstName",
                    "type": "textfield",
                    "input": true,
                    "attributes": {
                        "data-testid": "form-example2-person.firstName"
                    }
                },
                {
                    "type": "button",
                    "label": "Submit",
                    "key": "submit",
                    "disableOnInvalid": true,
                    "input": true
                }
            ]
        }
    """.trimIndent()

    @Test
    fun `shouldImportFormDefinition`() {
        val validPath = "config/form/importer-example.json"
        val request = ImportRequest(validPath, formDefinition.encodeToByteArray())

        formDefinitionImporter.import(request)
        val formIoFormDefinition = formDefinitionService.getFormDefinitionByName("importer-example")
            .map { it as? FormIoFormDefinition }.orElse(null)

        assertThat(formIoFormDefinition).isNotNull
        assertThat(formIoFormDefinition!!.asJson()).isEqualTo(objectMapper.readTree(formDefinition))
        assertThat(formIoFormDefinition.isReadOnly).isFalse()
    }
}