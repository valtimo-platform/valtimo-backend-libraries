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

package com.ritense.processdocument.domain.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.ritense.valtimo.contract.audit.AuditEvent;
import com.ritense.valtimo.contract.audit.AuditMetaData;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class TestEvent extends AuditMetaData implements AuditEvent {
    private String name;
    private Integer age;
    private String country;
    private String gender;
    private String processInstanceId;

    @JsonCreator
    public TestEvent(
        UUID id,
        String origin,
        LocalDateTime occurredOn,
        String user,
        String name,
        Integer age,
        String country,
        String gender,
        String processInstanceId
    ) {
        super(id, origin, occurredOn, user);
        this.name = name;
        this.age = age;
        this.country = country;
        this.gender = gender;
        this.processInstanceId = processInstanceId;
    }

    public String getName() {
        return name;
    }

    public Integer getAge() {
        return age;
    }

    public String getCountry() {
        return country;
    }

    public String getGender() {
        return gender;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TestEvent)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        TestEvent testEvent = (TestEvent) o;
        return getName().equals(testEvent.getName()) &&
            getAge().equals(testEvent.getAge()) &&
            getCountry().equals(testEvent.getCountry()) &&
            getGender().equals(testEvent.getGender()) &&
            getProcessInstanceId().equals(testEvent.getProcessInstanceId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getName(), getAge(), getCountry(), getGender(), getProcessInstanceId());
    }
}