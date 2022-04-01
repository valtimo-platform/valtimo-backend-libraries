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

import com.fasterxml.jackson.annotation.JsonView;
import com.ritense.valtimo.viewconfigurator.web.rest.view.Views;
import org.hibernate.annotations.GenericGenerator;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = "enum_variable_type_item")
public class EnumVariableTypeItem {

    @JsonView(Views.ViewConfig.class)
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "native")
    @GenericGenerator(name = "native", strategy = "native")
    @Column(name = "enum_variable_type_item_id")
    private Long id;

    @JsonView(Views.ViewConfig.class)
    @Column(name = "reference_id", nullable = false)
    private String referenceId;

    @JsonView(Views.ViewConfig.class)
    @Column(name = "value", nullable = false)
    private String value;

    private EnumVariableTypeItem() {
    }

    public EnumVariableTypeItem(String referenceId, String value) {
        Objects.requireNonNull(referenceId, "referenceId cannot be null");
        Objects.requireNonNull(value, "value cannot be null");
        this.referenceId = referenceId;
        this.value = value;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public String getValue() {
        return value;
    }

    public Long getId() {
        return id;
    }
}
