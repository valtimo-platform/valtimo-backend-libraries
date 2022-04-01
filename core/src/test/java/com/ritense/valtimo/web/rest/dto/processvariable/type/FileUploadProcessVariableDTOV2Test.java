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

package com.ritense.valtimo.web.rest.dto.processvariable.type;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FileUploadProcessVariableDTOV2Test {
    private static final String NAME = "some-name";
    private static final Boolean VALUE = true;

    @Test
    void fileUploadProcessVariableDTOV2() {
        FileUploadProcessVariableDTOV2 fileUploadProcessVariable = new FileUploadProcessVariableDTOV2(NAME, VALUE);
        assertEquals(NAME, fileUploadProcessVariable.getName());
        assertEquals(VALUE, fileUploadProcessVariable.getValue());
    }

    @Test
    void fileUploadProcessVariableDTOV2ValueNull() {
        assertThrows(NullPointerException.class, () -> new FileUploadProcessVariableDTOV2(NAME, null));
    }

    @Test
    void fileUploadProcessVariableDTOV2NameNull() {
        assertThrows(NullPointerException.class, () -> new FileUploadProcessVariableDTOV2(null, VALUE));
    }

}