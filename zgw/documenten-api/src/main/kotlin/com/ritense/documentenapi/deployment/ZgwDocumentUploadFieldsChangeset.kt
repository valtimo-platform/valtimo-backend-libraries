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

package com.ritense.documentenapi.deployment

import com.fasterxml.jackson.annotation.JsonProperty
import com.ritense.documentenapi.domain.DocumentenApiUploadFieldKey

data class ZgwDocumentUploadFieldsChangeset(
    val changesetId: String,
    @JsonProperty("case-definitions")
    val caseDefinitions: List<ZgwDocumentCaseDefinitionUploadFields>
)

data class ZgwDocumentCaseDefinitionUploadFields(
    val key: String,
    val fields: List<ZgwDocumentUploadField> = emptyList()
)

data class ZgwDocumentUploadField(
    val key: DocumentenApiUploadFieldKey,
    val defaultValue: String = "",
    val visible: Boolean = true,
    val readonly: Boolean = false,
)