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

import com.ritense.valtimo.viewconfigurator.domain.ProcessDefinitionVariable;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "boolean")
public class BooleanVariableType extends ProcessDefinitionVariable {
    private static final String TYPE_NAME = "boolean";

    private BooleanVariableType() {
    }

    public BooleanVariableType(String referenceId, String label) {
        super(referenceId, label);
    }

    @Override
    public String getTypeName() {
        return TYPE_NAME;
    }
}
