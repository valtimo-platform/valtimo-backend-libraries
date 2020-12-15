/*
 * Copyright 2015-2020 Ritense BV, the Netherlands.
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

package com.ritense.valtimo.accessandentitlement.domain.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.ritense.valtimo.accessandentitlement.domain.Money;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class AuthorityNameChangedEvent extends AuthorityEvent {

    private String oldName;

    @JsonCreator
    public AuthorityNameChangedEvent(
        UUID id,
        String origin,
        LocalDateTime occurredOn,
        String user,
        String name,
        Boolean systemAuthority,
        Money hourlyRate,
        String oldName
    ) {
        super(id, origin, occurredOn, user, name, systemAuthority, hourlyRate);
        this.oldName = oldName;
    }

    public String getOldName() {
        return oldName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AuthorityNameChangedEvent)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        AuthorityNameChangedEvent that = (AuthorityNameChangedEvent) o;
        return Objects.equals(getOldName(), that.getOldName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getOldName());
    }
}