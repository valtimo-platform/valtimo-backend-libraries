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

import com.ritense.valtimo.domain.choicefield.ChoiceFieldValue;
import java.util.List;

public class ChoiceFieldDTO {
    private Long id;
    private String keyName;

    private List<ChoiceFieldValue> choiceFieldValues;

    public ChoiceFieldDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKeyName() {
        return keyName;
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }

    public List<ChoiceFieldValue> getChoiceFieldValues() {
        return choiceFieldValues;
    }

    public void setChoiceFieldValues(List<ChoiceFieldValue> choiceFieldValues) {
        this.choiceFieldValues = choiceFieldValues;
    }
}
