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

import com.ritense.valtimo.web.rest.dto.processvariable.ProcessVariableDTOV2;
import java.util.List;
import java.util.Objects;

public class EnumProcessVariableDTOV2 extends ProcessVariableDTOV2 {
    private List<String> values;

    public EnumProcessVariableDTOV2() {
    }

    public EnumProcessVariableDTOV2(String name, List<String> values) {
        setName(name);
        Objects.requireNonNull(values, "values cannot be null");
        this.values = values;
    }

    public List<String> getValues() {
        return values;
    }
}
