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

package com.ritense.document.config.validator;

import org.everit.json.schema.FormatValidator;
import java.util.Optional;
import java.util.UUID;

public class UuidValidator implements FormatValidator {

    @Override
    public Optional<String> validate(String subject) {
        try {
            UUID.fromString(subject);
            return Optional.empty();
        } catch (Exception e) {
            return Optional.of(String.format("invalid uuid [%s]", subject));
        }
    }

    @Override
    public String formatName() {
        return "uuid";
    }

}