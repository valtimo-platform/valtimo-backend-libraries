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

package com.ritense.case_.domain.definition

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.ritense.valtimo.contract.case_.CaseDefinitionId
import com.ritense.valtimo.contract.repository.SemverConverter
import com.ritense.valtimo.contract.serializer.SemverSerializer
import jakarta.persistence.Column
import jakarta.persistence.Convert
import jakarta.persistence.EmbeddedId
import jakarta.persistence.Entity
import jakarta.persistence.Table
import org.apache.commons.validator.routines.UrlValidator
import org.semver4j.Semver
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import java.time.LocalDateTime

@Entity
@Table(name = "case_definition")
data class CaseDefinition(
    @EmbeddedId
    val id: CaseDefinitionId,
    @Column(name = "case_definition_name")
    val name: String,
    @Column(name = "description")
    val description: String? = null,
    @CreatedBy
    @Column(name = "created_by", updatable = false) @JsonIgnore
    val createdBy: String? = null,
    @CreatedDate
    @Column(name = "created_date", updatable = false)
    @JsonIgnore
    val createdDate: LocalDateTime?,
    @Convert(converter = SemverConverter::class)
    @Column(name = "based_on_version_tag", updatable = false)
    @JsonSerialize(using = SemverSerializer::class)
    val basedOnVersionTag: Semver? = null,
    @Column(name = "is_final")
    val final: Boolean = false,
    @Column(name = "active")
    val active: Boolean = false,

    @Column(name = "can_have_assignee", nullable = false)
    val canHaveAssignee: Boolean = false,
    @Column(name = "auto_assign_tasks", nullable = false)
    val autoAssignTasks: Boolean = false,
    @Column(name = "has_external_start_form", nullable = false)
    val hasExternalStartForm: Boolean = false,
    @Column(name = "external_start_form_url", nullable = true, length = 512)
    val externalStartFormUrl: String? = null,
    @Column(name = "external_start_form_description", nullable = true, length = 512)
    val externalStartFormDescription: String? = null,
) {
    init {
        require(
            when (autoAssignTasks) {
                true -> canHaveAssignee
                else -> true
            }
        ) { "Case property [autoAssignTasks] can only be true when [canHaveAssignee] is true." }
        require(
            when (hasExternalStartForm) {
                true -> !externalStartFormUrl.isNullOrBlank()
                else -> true
            }
        ) {
            "Case property [hasExternalStartForm] can only be true when [externalStartFormUrl] is not null or blank."
        }
        require(
            when (hasExternalStartForm) {
                true -> UrlValidator(arrayOf("http", "https")).isValid(externalStartFormUrl)
                else -> true
            }
        ) {
            "Case property [externalStartFormUrl] is not a valid URL."
        }
        require(
            when (hasExternalStartForm && !externalStartFormUrl.isNullOrBlank()) {
                true -> externalStartFormUrl.length <= 512
                else -> true
            }
        ) {
            "Case property [externalStartFormUrl] exceeds the maximum length of 512 characters."
        }
        require(
            when (hasExternalStartForm && !externalStartFormDescription.isNullOrBlank()) {
                true -> externalStartFormDescription.length <= 512
                else -> true
            }
        ) {
            "Case property [externalStartFormDescription] exceeds the maximum length of 512 characters."
        }
    }
}