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

package com.ritense.valtimo.camunda.task.service.impl;

import com.ritense.valtimo.camunda.task.service.NotificationService;
import com.ritense.valtimo.contract.authentication.UserManagementService;
import com.ritense.valtimo.contract.config.ValtimoProperties;
import com.ritense.valtimo.contract.mail.MailSender;
import com.ritense.valtimo.contract.mail.model.TemplatedMailMessage;
import com.ritense.valtimo.emailnotificationsettings.service.EmailNotificationSettingsService;
import com.ritense.valtimo.helper.DelegateTaskHelper;
import java.util.List;
import java.util.Optional;
import org.camunda.community.mockito.delegate.DelegateTaskFake;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import static com.ritense.valtimo.camunda.task.service.NotificationTestHelper.mockTask;
import static com.ritense.valtimo.camunda.task.service.NotificationTestHelper.user;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationServiceImplTest {

    private EmailNotificationSettingsService emailNotificationService;
    private MailSender mailSender;
    private ValtimoProperties valtimoProperties;
    private DelegateTaskHelper delegateTaskHelper;
    private UserManagementService userManagementService;
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        emailNotificationService = mock(EmailNotificationSettingsService.class);
        mailSender = mock(MailSender.class);
        valtimoProperties = mock(ValtimoProperties.class, RETURNS_DEEP_STUBS);
        delegateTaskHelper = mock(DelegateTaskHelper.class);
        userManagementService = mock(UserManagementService.class);

        notificationService = new NotificationServiceImpl(
            emailNotificationService,
            mailSender,
            valtimoProperties,
            delegateTaskHelper,
            userManagementService
        );
    }

    @Test
    void shouldSendNotification() {
        when(delegateTaskHelper.isTaskBeingAssigned(ArgumentMatchers.any())).thenReturn(true);
        when(userManagementService.findByEmail(null))
            .thenReturn(Optional.of(user("test1@test.com", List.of("dev"))));
        when(emailNotificationService.existsByEmailAddressAndTaskNotificationsEnabled(anyString())).thenReturn(true);
        when(valtimoProperties.getApp().getBaselUrl()).thenReturn("http://baseUrl");

        DelegateTaskFake task = mockTask("id");
        notificationService.sendNotification(task, "mail-template-test");

        verify(mailSender).send(ArgumentMatchers.any(TemplatedMailMessage.class));
    }

}