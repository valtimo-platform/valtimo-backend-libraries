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
import com.ritense.valtimo.contract.audit.AuditEvent;
import com.ritense.valtimo.contract.audit.AuditMetaData;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotNull;

public abstract class AuthorityEvent extends AuditMetaData implements AuditEvent {

    private String name;
    private boolean systemAuthority;
    private Money hourlyRate;

    /**
     * @deprecated - This method will be removed in 11.0.0
     * Use {@link #AuthorityEvent(UUID, String, LocalDateTime, String, String, boolean)} instead.
     */
    @Deprecated(forRemoval = true, since = "9.4.0")
    @JsonCreator
    public AuthorityEvent(
        UUID id,
        String origin,
        LocalDateTime occurredOn,
        String user,
        String name,
        boolean systemAuthority,
        Money hourlyRate
    ) {
        super(id, origin, occurredOn, user);
        assertArgumentNotNull(name, "name is required");
        assertArgumentNotNull(hourlyRate, "hourlyRate is required");
        this.name = name;
        this.systemAuthority = systemAuthority;
        this.hourlyRate = hourlyRate;
    }

    @JsonCreator
    public AuthorityEvent(
        UUID id,
        String origin,
        LocalDateTime occurredOn,
        String user,
        String name,
        boolean systemAuthority
    ) {
        super(id, origin, occurredOn, user);
        assertArgumentNotNull(name, "name is required");
        this.name = name;
        this.systemAuthority = systemAuthority;
    }

    public String getName() {
        return name;
    }

    public boolean getSystemAuthority() {
        return systemAuthority;
    }

    /**
     * @deprecated - This method will be removed in 11.0.0
     */
    @Deprecated(forRemoval = true, since = "9.4.0")
    public Money getHourlyRate() {
        return hourlyRate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AuthorityEvent)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        AuthorityEvent that = (AuthorityEvent) o;
        return getName().equals(that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getName());
    }

}