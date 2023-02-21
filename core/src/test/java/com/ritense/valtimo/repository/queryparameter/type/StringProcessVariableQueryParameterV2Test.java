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

package com.ritense.valtimo.repository.queryparameter.type;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StringProcessVariableQueryParameterV2Test {
    private static final String NAME = "some-name";
    private static final String VALUE = "some-value";

    @Test
    void stringProcessVariableQueryParameter() {
        StringProcessVariableQueryParameterV2 stringProcessVariableQueryParameter = stringProcessVariableQueryParameterV2();
        assertEquals(NAME, stringProcessVariableQueryParameter.getName());
        assertEquals(VALUE, stringProcessVariableQueryParameter.value);
    }

    @Test
    void stringProcessVariableQueryParameterNull() {
        assertThrows(NullPointerException.class, () -> new StringProcessVariableQueryParameterV2(NAME, null));
    }

    @Test
    void stringType() {
        StringProcessVariableQueryParameterV2 stringProcessVariableQueryParameter = stringProcessVariableQueryParameterV2();
        assertTrue(stringProcessVariableQueryParameter.isVariableString());
    }

    private StringProcessVariableQueryParameterV2 stringProcessVariableQueryParameterV2() {
        return new StringProcessVariableQueryParameterV2(NAME, VALUE);
    }
}
