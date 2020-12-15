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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LongProcessVariableQueryParameterV2Test {

    private static final String NAME = "some-name";
    private static final Long VALUE = 1L;

    @Test
    public void longProcessVariableQueryParameter() {
        LongProcessVariableQueryParameterV2 longProcessVariableQueryParameter = longProcessVariableQueryParameterV2();
        assertEquals(NAME, longProcessVariableQueryParameter.name);
        assertEquals(VALUE, longProcessVariableQueryParameter.value);
    }

    @Test
    public void longType() {
        LongProcessVariableQueryParameterV2 longProcessVariableQueryParameter = longProcessVariableQueryParameterV2();
        assertTrue(longProcessVariableQueryParameter.isVariableLong());
    }

    private LongProcessVariableQueryParameterV2 longProcessVariableQueryParameterV2() {
        return new LongProcessVariableQueryParameterV2(NAME, VALUE);
    }
}