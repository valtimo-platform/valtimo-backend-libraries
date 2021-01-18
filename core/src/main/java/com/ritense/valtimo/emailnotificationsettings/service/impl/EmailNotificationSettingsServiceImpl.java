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

package com.ritense.valtimo.emailnotificationsettings.service.impl;

import com.ritense.valtimo.emailnotificationsettings.domain.request.EmailNotificationSettingsRequest;
import com.ritense.valtimo.emailnotificationsettings.domain.request.impl.EmailNotificationSettings;
import com.ritense.valtimo.emailnotificationsettings.repository.EmailNotificationSettingsRepository;
import com.ritense.valtimo.emailnotificationsettings.service.EmailNotificationSettingsService;
import lombok.RequiredArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class EmailNotificationSettingsServiceImpl implements EmailNotificationSettingsService {

    private final EmailNotificationSettingsRepository emailNotificationSettingsRepository;

    @Override
    public Optional<EmailNotificationSettings> getSettingsFor(String emailAddress) {
        return emailNotificationSettingsRepository.findByEmailAddress(emailAddress);
    }

    @Override
    public EmailNotificationSettings process(EmailNotificationSettingsRequest request, String emailAddress) {
        EmailNotificationSettings emailNotificationSettings = new EmailNotificationSettings(request, emailAddress);
        return emailNotificationSettingsRepository.saveAndFlush(emailNotificationSettings);
    }

    @Override
    public List<String> findAllUsersWithReminderNotificationsEnabledForToday() {
        final DayOfWeek today = DayOfWeek.from(LocalDateTime.now());
        return emailNotificationSettingsRepository.findAllReminderNotificationsEnabled(today)
            .stream()
            .map(EmailNotificationSettings::getEmailAddress)
            .collect(Collectors.toList());
    }

    @Override
    public boolean existsByEmailAddressAndTaskNotificationsEnabled(String emailAddress) {
        return emailNotificationSettingsRepository.existsByEmailAddressAndTaskNotificationsEnabled(emailAddress);
    }

    @Override
    public boolean userNotConfiguredYet(String emailAddress) {
        return !emailNotificationSettingsRepository.existsById(emailAddress);
    }

}