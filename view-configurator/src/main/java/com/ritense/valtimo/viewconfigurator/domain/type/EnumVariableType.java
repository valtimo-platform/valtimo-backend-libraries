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

package com.ritense.valtimo.viewconfigurator.domain.type;

import com.ritense.valtimo.viewconfigurator.domain.EnumVariableTypeItem;
import com.ritense.valtimo.viewconfigurator.domain.ProcessDefinitionVariable;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import java.util.Objects;
import java.util.Set;

@Entity
@DiscriminatorValue(value = "enum")
public class EnumVariableType extends ProcessDefinitionVariable {
    private static final String TYPE_NAME = "enum";

    @OneToMany(targetEntity = EnumVariableTypeItem.class, fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "process_definition_variable_id")
    private Set<EnumVariableTypeItem> items;

    private EnumVariableType() {
    }

    public EnumVariableType(String referenceId, String label, Set<EnumVariableTypeItem> items) {
        super(referenceId, label);
        Objects.requireNonNull(items, "items cannot be null");
        this.items = items;
    }

    public Set<EnumVariableTypeItem> getItems() {
        return items;
    }

    @Override
    public String getTypeName() {
        return TYPE_NAME;
    }
}
