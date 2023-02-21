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

package com.ritense.form.domain.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

public class ModifyFormDefinitionRequest {

    @JsonProperty
    private final UUID id;

    @JsonProperty
    private final String name;

    @JsonProperty
    private final String formDefinition;

    @JsonCreator
    public ModifyFormDefinitionRequest(
        @JsonProperty(value = "id", required = true) UUID id,
        @JsonProperty(value = "name") String name,
        @JsonProperty(value = "formDefinition") String formDefinition
    ) {
        this.id = id;
        this.name = name;
        this.formDefinition = formDefinition;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getFormDefinition() {
        return formDefinition;
    }

}