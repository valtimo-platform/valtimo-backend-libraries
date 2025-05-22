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

package com.ritense.zakenapi.resolver

import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.valueresolver.ValueResolverOption
import com.ritense.zakenapi.service.ZaakDocumentService
import org.camunda.bpm.engine.delegate.VariableScope
import java.util.UUID
import java.util.function.Function

class ZaakValueResolverFactory(
    private val zaakDocumentService: ZaakDocumentService,
    processDocumentService: ProcessDocumentService,
) : BaseFieldValueResolverFactory(processDocumentService) {
    override fun supportedPrefix(): String {
        return "zaak"
    }

    override fun createResolver(documentId: String): Function<String, Any?> {
        val zaak = zaakDocumentService.getZaakByDocumentIdOrThrow(UUID.fromString(documentId))
        return Function { field ->
            return@Function getField(zaak, field)
        }
    }

    override fun handleValues(
        processInstanceId: String,
        variableScope: VariableScope?,
        values: Map<String, Any?>
    ) {
        TODO()
    }

    @Deprecated("Deprecated since 12.6.0, Use getResolvableKeyOptions(documentDefinitionName: String, version: Long) instead")
    override fun getResolvableKeys(documentDefinitionName: String, version: Long): List<String> {
        return ZAAK_FIELD_LIST
    }

    @Deprecated("Deprecated since 12.6.0, Use getResolvableKeyOptions(documentDefinitionName: String) instead")
    override fun getResolvableKeys(documentDefinitionName: String): List<String> {
        return ZAAK_FIELD_LIST
    }

    override fun getResolvableKeyOptions(documentDefinitionName: String, version: Long): List<ValueResolverOption> {
        return createFieldList(ZAAK_FIELD_LIST)
    }

    override fun getResolvableKeyOptions(documentDefinitionName: String): List<ValueResolverOption> {
        return createFieldList(ZAAK_FIELD_LIST)
    }

    companion object {
        val ZAAK_FIELD_LIST = listOf(
            "bronorganisatie",
            "identificatie",
            "omschrijving",
            "toelichting",
            "zaaktype",
            "registratiedatum",
            "verantwoordelijkeOrganisatie",
            "startdatum",
            "einddatum",
            "einddatumGepland",
            "uuid"
        ).sorted()
    }
}
