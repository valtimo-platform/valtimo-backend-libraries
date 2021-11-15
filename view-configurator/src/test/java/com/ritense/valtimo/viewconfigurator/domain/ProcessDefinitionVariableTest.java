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

import com.ritense.valtimo.viewconfigurator.domain.type.EnumVariableType;
import com.ritense.valtimo.viewconfigurator.domain.type.StringVariableType;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProcessDefinitionVariableTest {

    @Test
    public void createProcessDefinitionVariable() {
        final String referenceId = "reference-id";
        final String label = "label";
        ProcessDefinitionVariable variable = new StringVariableType(referenceId, label);

        assertEquals(referenceId, variable.getReferenceId());
        assertEquals(label, variable.getLabel());
    }

    @Test
    public void createProcessDefinitionVariableWithEnumItems() {
        final String referenceId = "reference-id";
        final String label = "label";
        final String enumItemReferenceId = "enumItemReference";
        final String enumItemValue = "enumItemValue";

        EnumVariableTypeItem enumVariableTypeItem = new EnumVariableTypeItem(enumItemReferenceId, enumItemValue);
        Set<EnumVariableTypeItem> enumVariableTypeItems = new HashSet<>();
        enumVariableTypeItems.add(enumVariableTypeItem);

        EnumVariableType variable = new EnumVariableType(referenceId, label, enumVariableTypeItems);

        assertEquals(referenceId, variable.getReferenceId());
        assertEquals(label, variable.getLabel());
        assertEquals(enumItemReferenceId, variable.getItems().iterator().next().getReferenceId());
        assertEquals(enumItemValue, variable.getItems().iterator().next().getValue());
    }

    @Test
    public void changeLabel() {
        final String referenceId = "reference-id";
        final String label = "label";
        ProcessDefinitionVariable variable = new StringVariableType(referenceId, label);

        assertEquals(label, variable.getLabel());

        final String newLabel = "new-label";
        variable.changeLabel(newLabel);

        assertEquals(newLabel, variable.getLabel());
    }
}