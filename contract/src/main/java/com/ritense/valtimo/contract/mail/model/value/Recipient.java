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
import java.util.Objects;

public class Recipient {
    public final EmailAddress email;
    public final SimpleName name;
    public final Type type;

    // Only used by Jackson
    private Recipient() {
        email = null;
        name = null;
        type = null;
    }

    private Recipient(EmailAddress email, SimpleName name, Type type) {
        this.email = email;
        this.name = name;
        this.type = type;
    }

    public enum Type {
        TO, CC, BCC
    }

    public static Recipient to(EmailAddress emailAddress, SimpleName name) {
        return Recipient.forType(emailAddress, name, Type.TO);
    }

    public static Recipient cc(EmailAddress emailAddress, SimpleName name) {
        return Recipient.forType(emailAddress, name, Type.CC);
    }

    public static Recipient bcc(EmailAddress emailAddress, SimpleName name) {
        return Recipient.forType(emailAddress, name, Type.BCC);
    }

    @Override
    public String toString() {
        return "Recipient{" + "email=" + email + ", name=" + name + ", type=" + type + '}';
    }

    private static Recipient forType(EmailAddress emailAddress, SimpleName name, Type type) {
        Objects.requireNonNull(emailAddress, "Recipient cannot be missing an email address");
        emailAddress.assertPresentOrThrow("Recipient cannot be missing an email address");

        return new Recipient(emailAddress, name, type);
    }
}
