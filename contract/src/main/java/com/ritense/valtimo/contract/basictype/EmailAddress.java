/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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

package com.ritense.valtimo.contract.basictype;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class EmailAddress extends StringValue {

    @JsonCreator
    private EmailAddress(@JsonProperty("value") String value) {
        super(value);
    }

    public static EmailAddress from(String value) {
        return new EmailAddress(value);
    }

    public String getDomain() {
        if (!isPresent()) {
            return "";
        }

        String[] email = value.split("@");

        if (email.length < 2) {
            return "";
        }

        return email[1];
    }

    private static final EmailAddress empty = new EmailAddress(null);

    public static EmailAddress empty() {
        return empty;
    }
}