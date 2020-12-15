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

package com.ritense.formlink.domain.impl.formassociation.formlink;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Objects;

import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentLength;
import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotEmpty;

public abstract class CamundaBpmnElement implements Serializable {

    @JsonIgnore
    protected String elementId;

    protected Boolean isPublic;

    @JsonCreator
    public CamundaBpmnElement(String elementId, Boolean isPublic) {
        assertArgumentNotEmpty(elementId, "elementId cannot be empty");
        assertArgumentLength(elementId, 64, "elementId max length is 64");
        this.elementId = elementId;
        this.isPublic = isPublic;
    }

    @JsonProperty
    public boolean isPublic() {
        return isPublic != null ? isPublic : false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CamundaBpmnElement)) {
            return false;
        }
        CamundaBpmnElement that = (CamundaBpmnElement) o;
        return elementId.equals(that.elementId) &&
            Objects.equals(isPublic, that.isPublic);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elementId, isPublic);
    }

}
