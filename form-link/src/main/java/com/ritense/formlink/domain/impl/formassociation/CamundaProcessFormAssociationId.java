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
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ritense.formlink.domain.ProcessFormAssociation;
import com.ritense.valtimo.contract.domain.AbstractId;

import java.util.UUID;

import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotNull;

public class CamundaProcessFormAssociationId extends AbstractId<CamundaProcessFormAssociationId>
    implements ProcessFormAssociation.Id {

    private UUID id;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    private CamundaProcessFormAssociationId(UUID id) {
        assertArgumentNotNull(id, "id is required");
        this.id = id;
    }

    public static CamundaProcessFormAssociationId existingId(UUID id) {
        return new CamundaProcessFormAssociationId(id);
    }

    public static CamundaProcessFormAssociationId newId(UUID id) {
        return new CamundaProcessFormAssociationId(id).newIdentity();
    }

    public UUID getId() {
        return id;
    }

    @Override
    @JsonProperty
    public String toString() {
        return id.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CamundaProcessFormAssociationId)) {
            return false;
        }

        CamundaProcessFormAssociationId that = (CamundaProcessFormAssociationId) o;

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

}
