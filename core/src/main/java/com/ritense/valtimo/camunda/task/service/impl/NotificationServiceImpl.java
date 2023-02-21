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

import com.ritense.valtimo.camunda.task.domain.notification.AssignmentNotification;
import com.ritense.valtimo.camunda.task.service.NotificationService;
import com.ritense.valtimo.contract.authentication.ManageableUser;
import com.ritense.valtimo.contract.authentication.UserManagementService;
import com.ritense.valtimo.contract.config.ValtimoProperties;
import com.ritense.valtimo.contract.mail.MailSender;
import com.ritense.valtimo.contract.mail.model.value.MailTemplateIdentifier;
import com.ritense.valtimo.emailnotificationsettings.service.EmailNotificationSettingsService;
import com.ritense.valtimo.helper.DelegateTaskHelper;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotificationServiceImpl implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);
    private final EmailNotificationSettingsService emailNotificationService;
    private final MailSender mailSender;
    private final ValtimoProperties valtimoProperties;
    private final DelegateTaskHelper delegateTaskHelper;
    private final UserManagementService userManagementService;

    public NotificationServiceImpl(EmailNotificationSettingsService emailNotificationService, MailSender mailSender, ValtimoProperties valtimoProperties, DelegateTaskHelper delegateTaskHelper, UserManagementService userManagementService) {
        this.emailNotificationService = emailNotificationService;
        this.mailSender = mailSender;
        this.valtimoProperties = valtimoProperties;
        this.delegateTaskHelper = delegateTaskHelper;
        this.userManagementService = userManagementService;
    }

    @Override
    public void sendNotification(DelegateTask task) {
        sendNotification(task, defaultNotificationTemplate());
    }

    @Override
    public void sendNotification(DelegateTask task, String template) {
        logger.info("send notification for task: {} using template: {}", task.getName(), template);

        if (delegateTaskHelper.isTaskBeingAssigned(task)) {
            final String emailAddress = task.getAssignee();
            userManagementService.findByEmail(emailAddress).ifPresent(
                user -> notifyUserAboutTaskAssignment(user, task, template, "nl")
            );
        } else if (delegateTaskHelper.isTaskBeingCreated(task)) {
            notifyCandidateGroupAboutTaskAssignment(task, template);
        }
    }

    private boolean allowsToBeNotified(String emailAddress) {
        return emailNotificationService.existsByEmailAddressAndTaskNotificationsEnabled(emailAddress)
            || emailNotificationService.userNotConfiguredYet(emailAddress);
    }

    private String baselUrl() {
        return valtimoProperties.getApp().getBaselUrl();
    }

    private String defaultNotificationTemplate() {
        return valtimoProperties.getMandrill().getNotificationTemplate();
    }

    private void notifyCandidateGroupAboutTaskAssignment(DelegateTask task, String template) {
        delegateTaskHelper.findCandidateUsers(task).forEach(
            user -> notifyUserAboutTaskAssignment(user, task, template, "nl")
        );
    }

    private void notifyUserAboutTaskAssignment(ManageableUser user, DelegateTask task, String template, String languageKey) {
        if (allowsToBeNotified(user.getEmail())) {
            AssignmentNotification notification = new AssignmentNotification(
                task,
                MailTemplateIdentifier.from(template),
                user,
                baselUrl(),
                languageKey
            );
            notification.asTemplatedMailMessage().ifPresent(mailSender::send);
        }
    }

}