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
import static org.junit.jupiter.api.Assertions.assertTrue;

class BooleanProcessVariableQueryParameterV2Test {

    private static final String NAME = "some-name";
    private static final Boolean VALUE = false;

    @Test
    void booleanProcessVariableQueryParameter() {
        BooleanProcessVariableQueryParameterV2 booleanProcessVariableQueryParameter = booleanProcessVariableQueryParameterV2();
        assertEquals(NAME, booleanProcessVariableQueryParameter.getName());
        assertEquals(VALUE, booleanProcessVariableQueryParameter.getValue());
    }

    @Test
    void booleanType() {
        BooleanProcessVariableQueryParameterV2 booleanProcessVariableQueryParameter = booleanProcessVariableQueryParameterV2();
        assertTrue(booleanProcessVariableQueryParameter.isVariableBoolean());
    }

    private BooleanProcessVariableQueryParameterV2 booleanProcessVariableQueryParameterV2() {
        return new BooleanProcessVariableQueryParameterV2(NAME, VALUE);
    }

}
