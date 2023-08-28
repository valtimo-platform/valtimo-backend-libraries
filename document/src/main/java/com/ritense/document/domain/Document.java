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

package com.ritense.document.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.ritense.document.domain.relation.DocumentRelation;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface Document {

    @JsonProperty
    Id id();

    @JsonProperty
    LocalDateTime createdOn();

    @JsonProperty
    String createdBy();

    @JsonProperty
    Optional<LocalDateTime> modifiedOn();

    @JsonProperty
    DocumentDefinition.Id definitionId();

    @JsonProperty
    String assigneeId();

    @JsonProperty
    String assigneeFullName();

    @JsonProperty
    DocumentContent content();

    @JsonProperty
    DocumentVersion version();

    @JsonProperty
    Long sequence();

    @JsonProperty
    Set<? extends DocumentRelation> relations();

    @JsonProperty
    Set<? extends RelatedFile> relatedFiles();

    interface Id {

        @JsonValue
        String toString();

        @JsonIgnore
        UUID getId();

    }

}
