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

package com.ritense.valtimo.contract.mail.model.value;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ritense.valtimo.contract.basictype.StringValue;
import java.util.Objects;

public class Subject extends StringValue {

    @JsonCreator
    private Subject(@JsonProperty("value") String value) {
        super(value);
    }

    public static Subject from(String value) {
        Objects.requireNonNull(value, "Subject line cannot be null or none");
        return new Subject(value);
    }

    private static final Subject none = new Subject(null);

    public static Subject none() {
        return none;
    }
}
