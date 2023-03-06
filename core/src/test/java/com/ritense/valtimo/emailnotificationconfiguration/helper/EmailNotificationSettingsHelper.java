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

package com.ritense.valtimo.emailnotificationconfiguration.helper;

import com.ritense.valtimo.emailnotificationsettings.domain.request.EmailNotificationSettingsRequest;
import com.ritense.valtimo.emailnotificationsettings.domain.request.impl.EmailNotificationSettings;
import com.ritense.valtimo.emailnotificationsettings.domain.request.impl.EmailNotificationSettingsRequestImpl;
import java.util.Optional;

public class EmailNotificationSettingsHelper {

    private static final String emailAddress = "test@test.com";

    public static Optional<EmailNotificationSettings> enabledEmailNotificationSettings() {
        return Optional.of(new EmailNotificationSettings(requestEnabled(), emailAddress));
    }

    public static Optional<EmailNotificationSettings> disabledEmailNotificationSettings() {
        return Optional.of(new EmailNotificationSettings(requestDisabled(), emailAddress));
    }

    public static EmailNotificationSettingsRequest requestEnabled() {
        return new EmailNotificationSettingsRequestImpl(
            true,
            true,
            true,
            true,
            true,
            true,
            true,
            true,
            true
        );
    }

    private static EmailNotificationSettingsRequest requestDisabled() {
        return new EmailNotificationSettingsRequestImpl(
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            false,
            true
        );
    }

}