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

package com.ritense.valtimo.viewconfigurator.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonView;
import com.ritense.valtimo.viewconfigurator.domain.type.BooleanVariableType;
import com.ritense.valtimo.viewconfigurator.domain.type.DateVariableType;
import com.ritense.valtimo.viewconfigurator.domain.type.EnumVariableType;
import com.ritense.valtimo.viewconfigurator.domain.type.FileUploadVariableType;
import com.ritense.valtimo.viewconfigurator.domain.type.LongVariableType;
import com.ritense.valtimo.viewconfigurator.domain.type.StringVariableType;
import com.ritense.valtimo.viewconfigurator.web.rest.view.Views;
import org.hibernate.annotations.GenericGenerator;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = "process_definition_variable")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type_name", discriminatorType = DiscriminatorType.STRING, columnDefinition = "VARCHAR(50)")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes( {
    @JsonSubTypes.Type(value = StringVariableType.class, name = "string"),
    @JsonSubTypes.Type(value = DateVariableType.class, name = "date"),
    @JsonSubTypes.Type(value = BooleanVariableType.class, name = "boolean"),
    @JsonSubTypes.Type(value = EnumVariableType.class, name = "enum"),
    @JsonSubTypes.Type(value = LongVariableType.class, name = "long"),
    @JsonSubTypes.Type(value = FileUploadVariableType.class, name = "fileUpload")
})
@JsonRootName(value = "var")
public abstract class ProcessDefinitionVariable {

    @JsonView(Views.ViewConfig.class)
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    @Column(name = "process_definition_variable_id")
    private Long id;

    @JsonView(Views.ViewConfig.class)
    @Column(name = "reference_id", nullable = false)
    private String referenceId;

    @JsonView(Views.ViewConfig.class)
    @Column(name = "label", nullable = false)
    private String label;

    public ProcessDefinitionVariable() {
    }

    public ProcessDefinitionVariable(String referenceId, String label) {
        Objects.requireNonNull(referenceId, "referenceId cannot be null");
        Objects.requireNonNull(label, "label cannot be null");
        this.referenceId = referenceId;
        this.label = label;
    }

    public void changeLabel(String label) {
        Objects.requireNonNull(label, "label cannot be null");
        this.label = label;
    }

    public Long getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public abstract String getTypeName();

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ProcessDefinitionVariable)) {
            return false;
        }
        ProcessDefinitionVariable that = (ProcessDefinitionVariable) o;
        return
            Objects.equals(getReferenceId(), that.getReferenceId())
                &&
                Objects.equals(getTypeName(), that.getTypeName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getReferenceId(), getTypeName());
    }
}