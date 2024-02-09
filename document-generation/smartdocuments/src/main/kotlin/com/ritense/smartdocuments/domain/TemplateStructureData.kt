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

package com.ritense.smartdocuments.domain

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class SmartDocumentsTemplateData(
    @JsonProperty("DocumentsStructure")
    val documentsStructure: DocumentsStructure
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class DocumentsStructure(
    @JsonProperty("TemplatesStructure")
    val templatesStructure: TemplatesStructure,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TemplatesStructure(
    @JsonProperty("TemplateGroups")
    val templateGroups: List<TemplateGroup>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class TemplateGroup(
    @JsonProperty("Name")
    val name: String,
    @JsonProperty("TemplateGroups")
    val templateGroups: List<TemplateGroup>?,
    @JsonProperty("Templates")
    val templates: List<Template>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Template(
    @JsonProperty("ID")
    val id: String,
    @JsonProperty("Name")
    val name: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class GroupsAccess(
    @JsonProperty("TemplateGroups")
    val templateGroups: List<TemplateGroup>,
)