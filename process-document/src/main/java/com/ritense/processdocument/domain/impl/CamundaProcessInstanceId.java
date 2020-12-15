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

package com.ritense.processdocument.domain.impl;

import com.ritense.processdocument.domain.ProcessInstanceId;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.util.Objects;
import java.util.UUID;

import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentLength;
import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotNull;
import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentTrue;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class CamundaProcessInstanceId implements ProcessInstanceId {

    @Column(name = "camunda_process_instance_id", columnDefinition = "VARCHAR(64)")
    private String id;

    public CamundaProcessInstanceId(final String id) {
        assertArgumentNotNull(id, "id is required");
        assertArgumentLength(id, 64, "id max length is 64");
        assertArgumentTrue(UUID.fromString(id) != null, "id max length is 64");
        this.id = id;
    }

    public String toString() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CamundaProcessInstanceId)) {
            return false;
        }
        CamundaProcessInstanceId that = (CamundaProcessInstanceId) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}