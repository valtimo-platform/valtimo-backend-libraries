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

package com.ritense.valtimo.accessandentitlement.domain;

import com.ritense.valtimo.accessandentitlement.domain.event.AuthorityCreatedEvent;
import com.ritense.valtimo.accessandentitlement.domain.event.AuthorityHourlyRateChangedEvent;
import com.ritense.valtimo.accessandentitlement.domain.event.AuthorityNameChangedEvent;
import com.ritense.valtimo.accessandentitlement.domain.listener.AuthorityDeletedEventListener;
import com.ritense.valtimo.contract.audit.utils.AuditHelper;
import com.ritense.valtimo.contract.utils.RequestHelper;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.domain.AbstractAggregateRoot;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentLength;
import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotNull;

@Entity
@Table(name = "jhi_authority")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@EntityListeners(AuthorityDeletedEventListener.class)
public class Authority extends AbstractAggregateRoot implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "name", length = 50, columnDefinition = "VARCHAR(50)", nullable = false)
    private String name;

    @Column(name = "system_authority", columnDefinition = "BIT", nullable = false)
    private Boolean systemAuthority = false;

    @Column(name = "hourly_rate", columnDefinition = "NUMERIC", nullable = false)
    private BigDecimal hourlyRate;

    private Authority() {
    }

    public Authority(String name, BigDecimal hourlyRate, boolean systemAuthority) {
        assertArgumentNotNull(name, "name is required");
        assertArgumentLength(name, 50, "name max length is 50");
        assertArgumentNotNull(hourlyRate, "hourlyRate is required");
        this.name = name;
        this.hourlyRate = hourlyRate;
        this.systemAuthority = systemAuthority;
        registerEvent(new AuthorityCreatedEvent(
            UUID.randomUUID(),
            RequestHelper.getOrigin(),
            LocalDateTime.now(),
            AuditHelper.getActor(),
            getName(),
            getSystemAuthority(),
            getHourlyRate()
        ));
    }

    public void changeName(String name) {
        if (!this.name.equals(name)) {
            final String oldName = getName();
            this.name = name;
            registerEvent(new AuthorityNameChangedEvent(
                UUID.randomUUID(),
                RequestHelper.getOrigin(),
                LocalDateTime.now(),
                AuditHelper.getActor(),
                getName(),
                getSystemAuthority(),
                getHourlyRate(),
                oldName
            ));
        }
    }

    public void changeHourlyRate(BigDecimal hourlyRate) {
        if (!this.hourlyRate.equals(hourlyRate)) {
            final Money oldHourlyRate = getHourlyRate();
            this.hourlyRate = hourlyRate;
            registerEvent(new AuthorityHourlyRateChangedEvent(
                UUID.randomUUID(),
                RequestHelper.getOrigin(),
                LocalDateTime.now(),
                AuditHelper.getActor(),
                getName(),
                getSystemAuthority(),
                getHourlyRate(),
                oldHourlyRate
            ));
        }
    }

    public Boolean getSystemAuthority() {
        return systemAuthority;
    }

    public String getName() {
        return name;
    }

    public Money getHourlyRate() {
        if (hourlyRate == null) {
            return null;
        }
        return new Money(hourlyRate);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Authority)) {
            return false;
        }
        Authority authority = (Authority) o;
        return getName().equals(authority.getName()) &&
            getSystemAuthority().equals(authority.getSystemAuthority()) &&
            getHourlyRate().equals(authority.getHourlyRate());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getSystemAuthority(), getHourlyRate());
    }

    @Override
    public String toString() {
        return "Authority{" +
            "name='" + name + '\'' +
            "systemAuthority='" + systemAuthority + '\'' +
            "hourlyRate='" + hourlyRate + '\'' +
            "}";
    }

}