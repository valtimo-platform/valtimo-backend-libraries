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

package com.ritense.valtimo.contract.audit;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PROTECTED_AND_PUBLIC;
import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotNull;

@JsonAutoDetect(fieldVisibility = PROTECTED_AND_PUBLIC)
@JsonPropertyOrder(value = {"id", "origin", "occurredOn", "user"})
public abstract class AuditMetaData {

    protected UUID id;
    protected String origin;
    protected LocalDateTime occurredOn;
    protected String user;

    @JsonCreator
    public AuditMetaData(UUID id, String origin, LocalDateTime occurredOn, String user) {
        assertArgumentNotNull(id, "id is required");
        assertArgumentNotNull(origin, "origin is required");
        assertArgumentNotNull(occurredOn, "occurredOn is required");
        assertArgumentNotNull(user, "user is required");
        this.id = id;
        this.origin = origin;
        this.occurredOn = occurredOn;
        this.user = user;
    }

    public AuditMetaData() {
    }

    public UUID getId() {
        return id;
    }

    public String getOrigin() {
        return origin;
    }

    public LocalDateTime getOccurredOn() {
        return occurredOn;
    }

    public String getUser() {
        return user;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AuditMetaData)) {
            return false;
        }
        AuditMetaData that = (AuditMetaData) o;
        return getId().equals(that.getId()) &&
            getOrigin().equals(that.getOrigin()) &&
            getOccurredOn().equals(that.getOccurredOn()) &&
            getUser().equals(that.getUser());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getOrigin(), getOccurredOn(), getUser());
    }
}
