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

import com.ritense.valtimo.camunda.domain.CamundaTask;
import com.ritense.valtimo.camunda.task.domain.reminder.AssignedTask;
import com.ritense.valtimo.camunda.task.domain.reminder.ReminderNotification;
import com.ritense.valtimo.camunda.task.domain.reminder.RoleBasedTask;
import com.ritense.valtimo.camunda.task.service.ReminderService;
import com.ritense.valtimo.contract.authentication.UserManagementService;
import com.ritense.valtimo.contract.mail.MailSender;
import com.ritense.valtimo.emailnotificationsettings.service.EmailNotificationSettingsService;
import com.ritense.valtimo.service.CamundaTaskService;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

import static com.ritense.valtimo.camunda.repository.CamundaTaskSpecificationHelper.byAssignee;
import static com.ritense.valtimo.camunda.repository.CamundaTaskSpecificationHelper.byCandidateGroups;
import static com.ritense.valtimo.camunda.repository.CamundaTaskSpecificationHelper.byUnassigned;
import static java.util.stream.Collectors.toList;

public class ReminderServiceImpl implements ReminderService {

    private final CamundaTaskService taskService;
    private final EmailNotificationSettingsService emailNotificationService;
    private final MailSender mailSender;
    private final UserManagementService userManagementService;
    private final String reminderTemplate;

    public ReminderServiceImpl(CamundaTaskService taskService, EmailNotificationSettingsService emailNotificationService, MailSender mailSender, UserManagementService userManagementService, String reminderTemplate) {
        this.taskService = taskService;
        this.emailNotificationService = emailNotificationService;
        this.mailSender = mailSender;
        this.userManagementService = userManagementService;
        this.reminderTemplate = reminderTemplate;
    }

    @Override
    @Scheduled(cron = "${scheduling.job.cron.taskNotificationReminder:-}")
    @SchedulerLock(
        name = "ReminderServiceImpl_notifyUsersWithOpenTasks", lockAtLeastFor = "PT4S", lockAtMostFor = "PT60M"
    )
    public void notifyUsersWithOpenTasks() {
        final List<String> users = userToNotifyToday();

        if (users != null) {
            userToNotifyToday().forEach(userEmail ->
                userManagementService.findByEmail(userEmail).ifPresent(
                    authUser -> {
                        ReminderNotification reminderNotification = new ReminderNotification(
                            userEmail,
                            reminderTemplate,
                            authUser.getFirstName()
                        );
                        authUser.getRoles().forEach(role ->
                            reminderNotification.assignRoleBasedTasks(findRoleBasesTasksFor(role)));
                        reminderNotification.assignAssignedTasks(findAssignedTasksFor(userEmail));
                        reminderNotification.asTemplatedMailMessage().ifPresent(mailSender::send);
                    })
            );
        }
    }

    private List<String> userToNotifyToday() {
        return emailNotificationService.findAllUsersWithReminderNotificationsEnabledForToday();
    }

    private List<RoleBasedTask> findRoleBasesTasksFor(String role) {
        final List<CamundaTask> tasks = tasksFor(role);
        if (tasks != null) {
            return tasks.stream()
                .map(task ->
                    new RoleBasedTask(
                        role,
                        new com.ritense.valtimo.camunda.task.domain.reminder.Task(
                            task.getId(),
                            task.getName(),
                            task.getCreateTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                        )
                    )
                )
                .collect(toList());
        }
        return null;
    }

    private List<CamundaTask> tasksFor(String role) {
        return taskService.findTasks(
            byCandidateGroups(role)
                .and(byUnassigned())
        );
    }

    private List<AssignedTask> findAssignedTasksFor(String assignee) {
        final List<CamundaTask> tasks = assignedTasks(assignee);
        if (tasks != null) {
            return tasks.stream()
                .map(task ->
                    new AssignedTask(
                        new com.ritense.valtimo.camunda.task.domain.reminder.Task(
                            task.getId(),
                            task.getName(),
                            task.getCreateTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
                        )
                    )
                ).collect(Collectors.toList());
        }
        return null;
    }

    private List<CamundaTask> assignedTasks(String assignee) {
        return taskService.findTasks(
            byAssignee(assignee)
        );
    }

}