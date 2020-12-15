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

public class MailTemplateIdentifier extends StringValue {
    @JsonCreator
    private MailTemplateIdentifier(@JsonProperty("value") String value) {
        super(value);
    }

    public static MailTemplateIdentifier from(String value) {
        MailTemplateIdentifier mailTemplateIdentifier = new MailTemplateIdentifier(value);
        mailTemplateIdentifier.assertPresentOrThrow("Identifier from Mail template cannot be null or none");
        return mailTemplateIdentifier;
    }

    /**
     * TODO: Decide if this is base functionality, or should we subclass this for Mandrill.
     * Or have a LanguageEnabledMailTemplateIdentifier with some flags (lang set yes/no -> set default lang if no etc)
     */
    public MailTemplateIdentifier withLanguageKey(String languageKey) {
        if (languageKey == null || languageKey.isEmpty()) {
            return this;
        }

        return new MailTemplateIdentifier(String.format("%s-%s", get(), languageKey));
    }
}
