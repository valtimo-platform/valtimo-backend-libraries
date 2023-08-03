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

package com.ritense.valtimo.web.rest.dto;

import javax.validation.constraints.NotNull;

public class ChoiceFieldValueUpdateRequestDTO {
    @NotNull
    private Long id;
    @NotNull
    private String name;
    @NotNull
    private Boolean deprecated;
    @NotNull
    private Long sortOrder;
    @NotNull
    private String value;

    public ChoiceFieldValueUpdateRequestDTO() {
        //Default constructor
    }

    public Long getId() { return id; }

    public String getName() {
        return name;
    }

    public Boolean getDeprecated() {
        return deprecated;
    }

    public Long getSortOrder() {
        return sortOrder;
    }

    public String getValue() {
        return value;
    }
}
