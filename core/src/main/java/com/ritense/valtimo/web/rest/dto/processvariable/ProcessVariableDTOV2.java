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

package com.ritense.valtimo.web.rest.dto.processvariable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.ritense.valtimo.web.rest.dto.processvariable.type.BooleanProcessVariableDTOV2;
import com.ritense.valtimo.web.rest.dto.processvariable.type.DateProcessVariableDTOV2;
import com.ritense.valtimo.web.rest.dto.processvariable.type.EnumProcessVariableDTOV2;
import com.ritense.valtimo.web.rest.dto.processvariable.type.FileUploadProcessVariableDTOV2;
import com.ritense.valtimo.web.rest.dto.processvariable.type.LongProcessVariableDTOV2;
import com.ritense.valtimo.web.rest.dto.processvariable.type.StringProcessVariableDTOV2;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME)
@JsonSubTypes({
        @JsonSubTypes.Type(value = StringProcessVariableDTOV2.class, name = "string"),
        @JsonSubTypes.Type(value = DateProcessVariableDTOV2.class, name = "date"),
        @JsonSubTypes.Type(value = BooleanProcessVariableDTOV2.class, name = "boolean"),
        @JsonSubTypes.Type(value = EnumProcessVariableDTOV2.class, name = "enum"),
        @JsonSubTypes.Type(value = LongProcessVariableDTOV2.class, name = "long"),
        @JsonSubTypes.Type(value = FileUploadProcessVariableDTOV2.class, name = "fileUpload")
})
public abstract class ProcessVariableDTOV2 {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        Objects.requireNonNull(name, "name cannot be null");
        this.name = name;
    }
}
