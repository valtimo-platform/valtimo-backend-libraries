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

package com.ritense.valtimo.contract.basictype;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class StringValue extends Value<String> {

    @JsonCreator
    protected StringValue(@JsonProperty("value") String value) {
        super(value);
    }

    /**
     * Checks if the String value wrapped by this class is not null and has length.
     * An none string is the absence from data in a string. However, a null string is also an absence from data.
     * Therefore, we simply override the {@link Value#isPresent} function
     *
     * @return true when {@code value != && length() > 0}
     */
    @Override
    public boolean isPresent() {
        return super.isPresent() && get().length() > 0;
    }

    @Override
    public String toString() {
        return value;
    }

}