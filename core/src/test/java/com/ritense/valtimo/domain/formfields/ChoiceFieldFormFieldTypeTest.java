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

package com.ritense.valtimo.domain.formfields;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ChoiceFieldFormFieldTypeTest {

    private ChoiceFieldFormFieldType choiceFieldFormFieldType;

    @BeforeEach
    void setUp() {
        choiceFieldFormFieldType = new ChoiceFieldFormFieldType();
    }

    @Test
    void shouldConvertModelValueToStringWhenString() {
        String modelValue = "test value";
        assertEquals(modelValue, choiceFieldFormFieldType.convertModelValueToFormValue(modelValue));
    }

    @Test
    void shouldReturnNullWhenConvertingNonStringModelValue() {
        assertNull(choiceFieldFormFieldType.convertModelValueToFormValue(0));
    }

}