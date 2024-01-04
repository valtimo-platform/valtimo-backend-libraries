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

package com.ritense.documentenapi.web.rest.dto

import com.ritense.document.domain.RelatedFile
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class RelatedFileDto(
    private val fileId: UUID,
    private val fileName: String?,
    private val sizeInBytes: Long?,
    private val createdOn: LocalDateTime,
    private val createdBy: String,
    private val pluginConfigurationId: UUID,
    private val author: String? = null,
    private val title: String? = null,
    private val status: String? = null,
    private val language: String? = null,
    private val identification: String? = null,
    private val description: String? = null,
    private val informatieobjecttype: String? = null,
    private val keywords: List<String>? = null,
    private val format: String? = null,
    private val sendDate: LocalDate? = null,
    private val receiptDate: LocalDate? = null,
    private val confidentialityLevel: String? = null,
    private val version: Int? = null,
    private val indicationUsageRights: Boolean? = null,
):  RelatedFile {
    override fun getFileId(): UUID {
        return fileId
    }

    override fun getFileName(): String? {
        return fileName
    }

    override fun getSizeInBytes(): Long? {
        return sizeInBytes
    }

    override fun getCreatedOn(): LocalDateTime {
        return createdOn
    }

    override fun getCreatedBy(): String {
        return createdBy
    }

    override fun getAuthor(): String? {
        return author
    }

    override fun getTitle(): String? {
        return title
    }

    override fun getStatus(): String? {
        return status
    }

    override fun getLanguage(): String? {
        return language
    }

    override fun getIdentification(): String? {
        return identification
    }

    override fun getDescription(): String? {
        return description
    }

    override fun getInformatieobjecttype(): String? {
        return informatieobjecttype
    }

    override fun getKeywords(): List<String>? {
        return keywords
    }

    override fun getFormat(): String? {
        return format
    }

    override fun getSendDate(): LocalDate? {
        return sendDate
    }

    override fun getReceiptDate(): LocalDate? {
        return receiptDate
    }

    override fun getConfidentialityLevel(): String? {
        return confidentialityLevel
    }
}