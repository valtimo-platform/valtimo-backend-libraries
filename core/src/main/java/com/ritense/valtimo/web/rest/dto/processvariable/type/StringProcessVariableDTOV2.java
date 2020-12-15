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

import com.ritense.valtimo.web.rest.dto.processvariable.ProcessVariableDTOV2;

import java.util.Objects;

public class StringProcessVariableDTOV2 extends ProcessVariableDTOV2 {
    public String value;

    public StringProcessVariableDTOV2() {
    }

    public StringProcessVariableDTOV2(String name, String value) {
        setName(name);
        Objects.requireNonNull(value, "value cannot be null");
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
