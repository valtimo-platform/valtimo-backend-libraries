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
import com.ritense.valtimo.contract.basictype.Value;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RecipientCollection extends Value<Collection<Recipient>> {

    @JsonCreator
    private RecipientCollection(@JsonProperty("value") Collection<Recipient> value) {
        super(value);
    }

    public static RecipientCollection from(Collection<Recipient> recipients) {
        Objects.requireNonNull(recipients, "Cannot create value collection from a 'null' collection");
        return new RecipientCollection(Collections.unmodifiableCollection(recipients));
    }

    public static RecipientCollection fromSingle(Recipient recipient) {
        Objects.requireNonNull(recipient, "Cannot create value collection from a 'null' Recipient");
        return new RecipientCollection(Collections.singletonList(recipient));
    }

    public Collection<Recipient> filterTo() {
        return filterType(Recipient.Type.To);
    }

    public Collection<Recipient> filterCc() {
        return filterType(Recipient.Type.Cc);
    }

    public Collection<Recipient> filterBcc() {
        return filterType(Recipient.Type.Bcc);
    }

    @Override
    public String toString() {
        return value.stream().map(Recipient::toString).collect(Collectors.joining(", "));
    }

    /**
     * Checks if the value wrapped by RecipientCollection is not null and is not none.
     *
     * @return true if {@code value != null} and {@code value.size > 0}
     */
    @Override
    public boolean isPresent() {
        return super.isPresent() && super.get().size() > 0;
    }

    private Collection<Recipient> filterType(Recipient.Type typeToFilterOn) {
        return value.stream().filter(x -> x.type == typeToFilterOn).collect(Collectors.toList());
    }

    public void filterBy(Predicate<Recipient> predicate) {
        value = value.stream().filter(predicate).collect(Collectors.toList());
    }
}
