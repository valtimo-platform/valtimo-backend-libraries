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

package com.ritense.valtimo.contract.mail.model.value;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ritense.valtimo.contract.basictype.Value;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

public class AttachmentCollection extends Value<Collection<Attachment>> {
    @JsonCreator
    private AttachmentCollection(@JsonProperty("value") Collection<Attachment> value) {
        super(value);
    }

    public static AttachmentCollection from(Collection<Attachment> attachments) {
        Objects.requireNonNull(attachments, "Attachments cannot be empty");
        return new AttachmentCollection(Collections.unmodifiableCollection(attachments));
    }

    public static AttachmentCollection fromSingle(Attachment attachment) {
        Objects.requireNonNull(attachment, "Attachment cannot be empty");
        return new AttachmentCollection(Collections.singletonList(attachment));
    }

    private static final AttachmentCollection empty = new AttachmentCollection(null);

    public static AttachmentCollection empty() {
        return empty;
    }

    @Override
    public boolean isPresent() {
        return super.isPresent() && super.get().size() > 0;
    }
}