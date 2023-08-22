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

package com.ritense.processdocument.domain.impl;

import com.ritense.processdocument.domain.ProcessDefinitionKey;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.util.Objects;

import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentLength;
import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotNull;

@Embeddable
public class CamundaProcessDefinitionKey implements ProcessDefinitionKey {

    @Column(name = "camunda_process_definition_key", columnDefinition = "VARCHAR(255)")
    private String key;

    public CamundaProcessDefinitionKey(final String key) {
        assertArgumentNotNull(key, "key is required");
        assertArgumentLength(key, 255, "key max length is 255");
        this.key = key;
    }

    CamundaProcessDefinitionKey() {
    }

    @Override
    public String toString() {
        return key;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CamundaProcessDefinitionKey)) {
            return false;
        }
        CamundaProcessDefinitionKey that = (CamundaProcessDefinitionKey) o;
        return key.equals(that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }

}
