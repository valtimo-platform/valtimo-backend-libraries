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

package com.ritense.valtimo.emailnotificationconfiguration.service;

import com.ritense.valtimo.emailnotificationconfiguration.helper.EmailNotificationSettingsHelper;
import com.ritense.valtimo.emailnotificationsettings.domain.request.impl.EmailNotificationSettings;
import com.ritense.valtimo.emailnotificationsettings.repository.EmailNotificationSettingsRepository;
import com.ritense.valtimo.emailnotificationsettings.service.EmailNotificationSettingsService;
import com.ritense.valtimo.emailnotificationsettings.service.impl.EmailNotificationSettingsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Optional;
import static com.ritense.valtimo.emailnotificationconfiguration.helper.EmailNotificationSettingsHelper.requestEnabled;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.internal.util.JavaEightUtil.emptyOptional;

class EmailSettingsServiceImplTest {

    private EmailNotificationSettingsService emailNotificationService;
    private EmailNotificationSettingsRepository repository;

    private String emailAddress = "test@test.com";

    @BeforeEach
    void setUp() {
        repository = mock(EmailNotificationSettingsRepository.class);
        emailNotificationService = new EmailNotificationSettingsServiceImpl(repository);
    }

    @Test
    void shouldGetEmptyConfigurationForUser() {
        Optional<EmailNotificationSettings> configuration = emailNotificationService.getSettingsFor(emailAddress);
        assertThat(configuration, is(emptyOptional()));
    }

    @Test
    void shouldGetConfigurationForUser() {
        Optional<EmailNotificationSettings> configurationOfUser = EmailNotificationSettingsHelper.enabledEmailNotificationSettings();
        when(repository.findByEmailAddress(anyString())).thenReturn(configurationOfUser);
        Optional<EmailNotificationSettings> configuration = emailNotificationService.getSettingsFor(emailAddress);
        assertThat(configuration, is(configurationOfUser));
    }

    @Test
    void shouldStoreConfigurationAfterSettingChanges() {
        EmailNotificationSettings newSettings = EmailNotificationSettingsHelper.enabledEmailNotificationSettings().get();
        when(repository.saveAndFlush(any(EmailNotificationSettings.class))).thenReturn(newSettings);
        EmailNotificationSettings emailNotificationSettings = emailNotificationService.process(requestEnabled(), emailAddress);

        assertThat(emailNotificationSettings, is(newSettings));
    }

}