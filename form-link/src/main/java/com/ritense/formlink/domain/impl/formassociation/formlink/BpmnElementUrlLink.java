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
import com.ritense.formlink.domain.FormLink;
import java.util.UUID;
import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentLength;
import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotEmpty;

public class BpmnElementUrlLink extends CamundaBpmnElement implements FormLink {

    private final String url;

    @JsonCreator
    public BpmnElementUrlLink(String id, String url) {
        super(id);
        assertArgumentNotEmpty(url, "url cannot be empty");
        assertArgumentLength(url, 512, "url max length is 512");
        this.url = url;
    }

    @Override
    @JsonProperty
    public String getId() {
        return super.elementId;
    }

    @Override
    @JsonIgnore
    public UUID getFormId() {
        return null;
    }

    @Override
    @JsonProperty
    public String getUrl() {
        return url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BpmnElementUrlLink)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        BpmnElementUrlLink that = (BpmnElementUrlLink) o;

        return url.equals(that.url);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + url.hashCode();
        return result;
    }

}
