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

package com.ritense.formlink.domain.impl.formassociation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ritense.formlink.domain.ProcessFormAssociation;
import org.hibernate.annotations.Type;
import org.springframework.data.domain.Persistable;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import java.util.UUID;

import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentLength;
import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotEmpty;
import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotNull;

@Entity
@Table(name = "process_form_association",
    indexes = {
        @Index(name = "process_definition_key_index", columnList = "process_definition_key")
    }
)
public class CamundaProcessFormAssociation implements ProcessFormAssociation, Persistable<CamundaProcessFormAssociationId> {

    @EmbeddedId
    private CamundaProcessFormAssociationId id;

    @Column(name = "process_definition_key", columnDefinition = "VARCHAR(64)")
    private String processDefinitionKey;

    @Column(name = "form_associations", columnDefinition = "json")
    @Type(type = "com.vladmihalcea.hibernate.type.json.JsonStringType")
    private FormAssociations formAssociations;

    public CamundaProcessFormAssociation(CamundaProcessFormAssociationId id, String processDefinitionKey, FormAssociations formAssociations) {
        assertArgumentNotNull(id, "id is required");
        assertArgumentNotEmpty(processDefinitionKey, "processDefinitionKey cannot be empty");
        assertArgumentLength(processDefinitionKey, 64, "processDefinitionKey max length is 64");
        this.id = id;
        this.processDefinitionKey = processDefinitionKey;
        this.formAssociations = formAssociations;
    }

    private CamundaProcessFormAssociation() {
    }

    public void addFormAssociation(CamundaFormAssociation camundaFormAssociation) {
        assertArgumentNotNull(camundaFormAssociation, "camundaFormAssociation is required");
        this.formAssociations.add(camundaFormAssociation);
    }

    public void updateFormAssociation(CamundaFormAssociation camundaFormAssociation) {
        assertArgumentNotNull(camundaFormAssociation, "camundaFormAssociation is required");
        removeFormAssociation(camundaFormAssociation.getId());
        this.formAssociations.add(camundaFormAssociation);
    }

    public void removeFormAssociation(UUID formAssociationId) {
        assertArgumentNotNull(formAssociationId, "formAssociationId is required");
        if (!this.formAssociations.removeIf(formAssociation -> formAssociation.getId().equals(formAssociationId))) {
            throw new IllegalStateException("Form association not removed");
        }
    }

    @Override
    public CamundaProcessFormAssociationId getId() {
        return id;
    }

    @Override
    public String getProcessDefinitionKey() {
        return processDefinitionKey;
    }

    @Override
    public FormAssociations getFormAssociations() {
        return formAssociations;
    }

    @Override
    @JsonIgnore
    public boolean isNew() {
        return id.isNew();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CamundaProcessFormAssociation)) {
            return false;
        }

        CamundaProcessFormAssociation that = (CamundaProcessFormAssociation) o;

        if (!id.equals(that.id)) {
            return false;
        }
        if (!processDefinitionKey.equals(that.processDefinitionKey)) {
            return false;
        }
        return formAssociations.equals(that.formAssociations);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + processDefinitionKey.hashCode();
        result = 31 * result + formAssociations.hashCode();
        return result;
    }

}