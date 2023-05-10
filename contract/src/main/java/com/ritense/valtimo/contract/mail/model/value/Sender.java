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

import com.ritense.valtimo.contract.basictype.EmailAddress;
import com.ritense.valtimo.contract.basictype.SimpleName;

public class Sender {
    public final EmailAddress email;
    public final SimpleName name;

    // Only used by Jackson
    private Sender() {
        email = null;
        name = null;
    }

    private Sender(EmailAddress email, SimpleName name) {
        this.email = email;
        this.name = name;
    }

    public static Sender from(EmailAddress emailAddress) {
        return Sender.from(emailAddress, SimpleName.none());
    }

    public static Sender from(EmailAddress emailAddress, SimpleName name) {
        if (!emailAddress.isPresent() || emailAddress.get().length() == 0) {
            throw new IllegalArgumentException("Sender email address cannot be none");
        }

        return new Sender(emailAddress, name);
    }

    private static final Sender empty = new Sender(EmailAddress.empty(), SimpleName.none());

    public static Sender empty() {
        return empty;
    }
}
