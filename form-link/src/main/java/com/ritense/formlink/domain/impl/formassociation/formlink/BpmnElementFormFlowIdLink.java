/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ritense.formlink.domain.FormLink;
import java.util.UUID;
import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BpmnElementFormFlowIdLink extends CamundaBpmnElement implements FormLink {

    protected String formFlowId;

    @JsonCreator
    public BpmnElementFormFlowIdLink(String id, String formFlowId) {
        super(id);
        assertArgumentNotNull(formFlowId, "formFlowId is required");
        this.formFlowId = formFlowId;
    }

    @Override
    public String getId() {
        return super.elementId;
    }

    @Override
    public UUID getFormId() {
        return null;
    }

    @Override
    @JsonIgnore
    public String getUrl() {
        return null;
    }

    @Override
    public String getFormFlowId() {
        return formFlowId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BpmnElementFormFlowIdLink)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        BpmnElementFormFlowIdLink that = (BpmnElementFormFlowIdLink) o;

        return formFlowId.equals(that.formFlowId);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + formFlowId.hashCode();
        return result;
    }
}