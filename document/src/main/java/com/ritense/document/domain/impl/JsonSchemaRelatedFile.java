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

package com.ritense.document.domain.impl;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.ritense.document.domain.RelatedFile;
import com.ritense.valtimo.contract.resource.Resource;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class JsonSchemaRelatedFile implements RelatedFile {

    private final UUID fileId;
    private final String fileName;
    private final Long sizeInBytes;
    private final LocalDateTime createdOn;
    private String createdBy;
    private final String author;
    private final String title;
    private final String status;
    private final String language;
    private final String identification;
    private final String description;
    private final String informatieobjecttype;
    private final List<String> keywords;
    private final String format;

    private final LocalDate sendDate;

    private final LocalDate receiptDate;

    private final String confidentialityLevel;

    @JsonCreator
    public JsonSchemaRelatedFile(
        final UUID fileId,
        final String fileName,
        final Long sizeInBytes,
        final LocalDateTime createdOn,
        final String createdBy,
        final String author,
        final String title,
        final String status,
        final String language,
        final String identification,
        final String description,
        final String informatieobjecttype,
        final List<String> keywords,
        final String format,
        final LocalDate sendDate,
        final LocalDate receiptDate,
        final String confidentialityLevel
    ) {
        this.fileId = fileId;
        this.fileName = fileName;
        this.sizeInBytes = sizeInBytes;
        this.createdOn = createdOn;
        this.createdBy = createdBy;
        this.author = author;
        this.title = title;
        this.status = status;
        this.language = language;
        this.identification = identification;
        this.description = description;
        this.informatieobjecttype = informatieobjecttype;
        this.keywords = keywords;
        this.format = format;
        this.sendDate = sendDate;
        this.receiptDate = receiptDate;
        this.confidentialityLevel = confidentialityLevel;
    }

    public static JsonSchemaRelatedFile from(final RelatedFile relatedFile) {
        return new JsonSchemaRelatedFile(
            relatedFile.getFileId(),
            relatedFile.getFileName(),
            relatedFile.getSizeInBytes(),
            relatedFile.getCreatedOn(),
            relatedFile.getCreatedBy(),
            relatedFile.getAuthor(),
            relatedFile.getTitle(),
            relatedFile.getStatus(),
            relatedFile.getLanguage(),
            relatedFile.getIdentification(),
            relatedFile.getDescription(),
            relatedFile.getInformatieobjecttype(),
            relatedFile.getKeywords(),
            relatedFile.getFormat(),
            relatedFile.getSendDate(),
            relatedFile.getReceiptDate(),
            relatedFile.getConfidentialityLevel()
        );
    }

    public static JsonSchemaRelatedFile from(final Resource resource) {
        return new JsonSchemaRelatedFile(
            resource.id(),
            resource.name(),
            resource.sizeInBytes(),
            resource.createdOn(),
            null,
            null,
            resource.title(),
            resource.status(),
            resource.language(),
            resource.identification(),
            resource.description(),
            resource.informatieobjecttype(),
            resource.keywords(),
            resource.extension(),
            resource.sendDate(),
            resource.receiptDate(),
            resource.confidentialityLevel()
        );
    }

    public JsonSchemaRelatedFile withCreatedBy(final String createdBy) {
        this.createdBy = createdBy;
        return this;
    }

    @Override
    public UUID getFileId() {
        return fileId;
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    @Override
    public Long getSizeInBytes() {
        return sizeInBytes;
    }

    @Override
    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    @Override
    public String getCreatedBy() {
        return createdBy;
    }
    public String getAuthor() { return author; }

    public String getTitle() { return title; }

    public String getStatus() { return status; }

    public String getLanguage() { return language; }

    public String getIdentification() { return identification; }

    public String getDescription() { return description; }

    public String getInformatieobjecttype() { return informatieobjecttype; }

    public List<String> getKeywords() { return keywords; }

    public String getFormat() { return format; }

    public LocalDate getSendDate() { return sendDate; }

    public LocalDate getReceiptDate() { return receiptDate; }

    public String getConfidentialityLevel() { return confidentialityLevel; }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof JsonSchemaRelatedFile)) {
            return false;
        }

        JsonSchemaRelatedFile that = (JsonSchemaRelatedFile) o;

        return fileId.equals(that.fileId);
    }

    @Override
    public int hashCode() {
        return fileId.hashCode();
    }

}