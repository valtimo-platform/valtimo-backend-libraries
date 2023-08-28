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

import com.ritense.valtimo.contract.basictype.StringValue;
import java.util.Objects;

public class MailBody {
    public final MailBodyText textBody;
    public final MailBodyHtml htmlBody;

    // Only used by Jackson
    private MailBody() {
        textBody = null;
        htmlBody = null;
    }

    private MailBody(MailBodyText textBody, MailBodyHtml htmlBody) {
        this.textBody = textBody;
        this.htmlBody = htmlBody;
    }

    public static MailBody of(MailBodyText mailBodyText) {
        return new MailBody(mailBodyText, MailBodyHtml.empty());
    }

    public static MailBody ofHtml(MailBodyHtml mailBodyHtml) {
        return new MailBody(MailBodyText.empty(), mailBodyHtml);
    }

    public static MailBody ofTextAndHtml(MailBodyText mailBodyText, MailBodyHtml mailBodyHtml) {
        Objects.requireNonNull(mailBodyText, "Cannot create MailBody with a 'null' MailBodyText");
        Objects.requireNonNull(mailBodyHtml, "Cannot create MailBody with a 'null' MailBodyHtml");

        return new MailBody(mailBodyText, mailBodyHtml);
    }

    public static class MailBodyText extends StringValue {
        private MailBodyText(String value) {
            super(value);
        }

        /**
         * Checks if the value wrapped by MailBodyText is not null. Empty string is allowed.
         *
         * @return true if value is not null
         */
        @Override
        public boolean isPresent() {
            return value != null;
        }

        public static MailBodyText of(String value) {
            if (value == null) {
                throw new IllegalArgumentException("Cannot create MailBodyText from a null, use MailBodyText.none() instead");
            }
            return new MailBodyText(value);
        }

        private static final MailBodyText empty = new MailBodyText(null);

        public static MailBodyText empty() {
            return empty;
        }
    }

    public static class MailBodyHtml extends StringValue {
        private MailBodyHtml(String value) {
            super(value);
        }

        /**
         * Checks if the value wrapped by MailBodyHtml is not null. Empty string is allowed.
         *
         * @return true if value is not null
         */
        @Override
        public boolean isPresent() {
            return value != null;
        }

        public static MailBodyHtml of(String value) {
            if (value == null) {
                throw new IllegalArgumentException("Cannot create MailBodyHtml from a null, use MailBodyHtml.none() instead");
            }
            return new MailBodyHtml(value);
        }

        private static final MailBodyHtml empty = new MailBodyHtml(null);

        public static MailBodyHtml empty() {
            return empty;
        }
    }
}
