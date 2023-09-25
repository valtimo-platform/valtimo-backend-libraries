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

package com.ritense.verzoek

import com.ritense.document.service.impl.JsonSchemaDocumentDefinitionService
import com.ritense.notificatiesapi.NotificatiesApiPlugin
import com.ritense.plugin.annotation.Plugin
import com.ritense.plugin.annotation.PluginEvent
import com.ritense.plugin.annotation.PluginProperty
import com.ritense.plugin.domain.EventType
import com.ritense.verzoek.domain.CopyStrategy
import com.ritense.verzoek.domain.VerzoekProperties
import com.ritense.zgw.Rsin
import java.util.UUID
import javax.validation.Valid
import javax.validation.ValidationException

@Plugin(
    key = "verzoek",
    title = "Verzoek",
    description = "Handles verzoeken"
)
class VerzoekPlugin(
    private val documentDefinitionService: JsonSchemaDocumentDefinitionService,
) {

    @PluginProperty(key = "notificatiesApiPluginConfiguration", secret = false)
    lateinit var notificatiesApiPluginConfiguration: NotificatiesApiPlugin

    @PluginProperty(key = "processToStart", secret = false)
    lateinit var processToStart: String

    @PluginProperty(key = "rsin", secret = false)
    lateinit var rsin: Rsin

    @Valid
    @PluginProperty(key = "verzoekProperties", secret = false)
    lateinit var verzoekProperties: List<VerzoekProperties>

    @PluginEvent(invokedOn = [EventType.CREATE, EventType.UPDATE])
    fun validateProperties() {
        verzoekProperties
            .filter { it.copyStrategy == CopyStrategy.SPECIFIED }
            .forEach { property ->
                property.mapping?.forEach {
                    if (!it.target.startsWith("doc:")) {
                        throw ValidationException("Failed to set mapping. Unknown prefix '${it.target.substringBefore(":")}:'.")
                    }
                    val documentPath = it.target.substringAfter(delimiter = ":")
                    documentDefinitionService.validateJsonPointer(property.caseDefinitionName, documentPath)
                }
            }
    }
}
