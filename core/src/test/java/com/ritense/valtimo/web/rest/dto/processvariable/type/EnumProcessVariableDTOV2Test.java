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

package com.ritense.valtimo.web.rest.dto.processvariable.type;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EnumProcessVariableDTOV2Test {
    private static final String NAME = "some-name";
    private static final List<String> VALUES = new ArrayList<>();

    @BeforeEach
    void setup() {
        VALUES.add("value1");
        VALUES.add("value2");
        VALUES.add("value3");
        VALUES.add("value4");
        VALUES.add("value5");
    }

    @Test
    void enumProcessVariableDTOV2() {
        EnumProcessVariableDTOV2 enumProcessVariableDTOV2 = new EnumProcessVariableDTOV2(NAME, VALUES);
        assertEquals(NAME, enumProcessVariableDTOV2.getName());
        assertEquals(VALUES, enumProcessVariableDTOV2.getValues());
    }

    @Test
    void enumProcessVariableDTOV2ValuesNull() {
        assertThrows(NullPointerException.class, () -> new EnumProcessVariableDTOV2(NAME, null));
    }

    @Test
    void enumProcessVariableDTOV2NameNull() {
        assertThrows(NullPointerException.class, () -> new EnumProcessVariableDTOV2(null, VALUES));
    }

}