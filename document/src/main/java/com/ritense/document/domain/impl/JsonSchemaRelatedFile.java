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
import java.time.LocalDateTime;
import java.util.UUID;

public class JsonSchemaRelatedFile implements RelatedFile {

    private final UUID fileId;
    private final String fileName;
    private final Long sizeInBytes;
    private final LocalDateTime createdOn;
    private String createdBy;

    @JsonCreator
    public JsonSchemaRelatedFile(
        final UUID fileId,
        final String fileName,
        final Long sizeInBytes,
        final LocalDateTime createdOn,
        final String createdBy
    ) {
        this.fileId = fileId;
        this.fileName = fileName;
        this.sizeInBytes = sizeInBytes;
        this.createdOn = createdOn;
        this.createdBy = createdBy;
    }

    public static JsonSchemaRelatedFile from(final RelatedFile relatedFile) {
        return new JsonSchemaRelatedFile(
            relatedFile.getFileId(),
            relatedFile.getFileName(),
            relatedFile.getSizeInBytes(),
            relatedFile.getCreatedOn(),
            relatedFile.getCreatedBy()
        );
    }

    public static JsonSchemaRelatedFile from(final Resource resource) {
        return new JsonSchemaRelatedFile(
            resource.id(),
            resource.name(),
            resource.sizeInBytes(),
            resource.createdOn(),
            null
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