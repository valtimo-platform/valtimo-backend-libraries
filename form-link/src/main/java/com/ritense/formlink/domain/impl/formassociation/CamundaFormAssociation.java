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

package com.ritense.formlink.domain.impl.formassociation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.ritense.formlink.domain.FormAssociation;
import com.ritense.formlink.domain.FormLink;
import kotlin.jvm.Transient;

import java.io.Serializable;
import java.util.UUID;

import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotNull;

@Deprecated(since = "10.6.0", forRemoval = true)
public abstract class CamundaFormAssociation implements FormAssociation, Serializable {

    protected UUID id;

    @Transient
    protected FormLink formLink;

    @JsonCreator
    public CamundaFormAssociation(UUID id, FormLink formLink) {
        assertArgumentNotNull(id, "id is required");
        assertArgumentNotNull(formLink, "formLink is required");
        this.id = id;
        this.formLink = formLink;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public FormLink getFormLink() {
        return formLink;
    }

    public ObjectNode toJson() {
        return Mapper.INSTANCE.objectMapper().convertValue(this, ObjectNode.class);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CamundaFormAssociation that = (CamundaFormAssociation) o;

        if (!id.equals(that.id)) return false;
        return formLink.equals(that.formLink);
    }

    @Override
    public int hashCode() {
        return formLink.getId().hashCode();
    }
}
