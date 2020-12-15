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

import com.ritense.valtimo.contract.mail.model.value.attachment.Content;
import com.ritense.valtimo.contract.mail.model.value.attachment.Name;
import com.ritense.valtimo.contract.mail.model.value.attachment.Type;

import java.util.Objects;

public class Attachment {

    public final Name name;
    public final Type type;
    public final Content content;

    // Only used by Jackson
    private Attachment() {
        name = null;
        type = null;
        content = null;
    }

    private Attachment(Name name, Type type, Content content) {
        this.name = name;
        this.type = type;
        this.content = content;
    }

    public static Attachment from(Name name, Type type, Content content) {
        Objects.requireNonNull(name, "Attachment cannot be missing name");
        Objects.requireNonNull(type, "Attachment cannot be missing type");
        Objects.requireNonNull(content, "Attachment cannot be missing content data");

        name.assertPresentOrThrow("Attachment cannot be missing name");
        type.assertPresentOrThrow("Attachment cannot be missing type");
        content.assertPresentOrThrow("Attachment cannot be missing content");

        return new Attachment(name, type, content);
    }
}
