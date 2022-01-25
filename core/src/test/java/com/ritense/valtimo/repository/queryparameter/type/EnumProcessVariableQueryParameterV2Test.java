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

package com.ritense.valtimo.repository.queryparameter.type;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnumProcessVariableQueryParameterV2Test {
    private static final String NAME = "some-name";
    private static List<String> enumValues = new ArrayList<>();

    @BeforeEach
    public void setup() {
        enumValues.add("value1");
        enumValues.add("value2");
        enumValues.add("value3");
        enumValues.add("value4");
        enumValues.add("value5");
    }

    @Test
    public void enumProcessVariableQueryParameter() {
        EnumProcessVariableQueryParameterV2 enumProcessVariableQueryParameter = enumProcessVariableQueryParameterV2();
        assertEquals(NAME, enumProcessVariableQueryParameter.name);
        assertEquals(enumValues, enumProcessVariableQueryParameter.values);
        assertEquals(enumValues.size(), enumProcessVariableQueryParameter.values.size());
    }

    @Test
    public void booleanProcessVariableQueryParameterNull() {
        assertThrows(NullPointerException.class, () -> new EnumProcessVariableQueryParameterV2(NAME, null));
    }

    @Test
    public void enumType() {
        EnumProcessVariableQueryParameterV2 enumProcessVariableQueryParameter = enumProcessVariableQueryParameterV2();
        assertTrue(enumProcessVariableQueryParameter.isVariableEnum());
    }

    private EnumProcessVariableQueryParameterV2 enumProcessVariableQueryParameterV2() {
        return new EnumProcessVariableQueryParameterV2(NAME, enumValues);
    }
}