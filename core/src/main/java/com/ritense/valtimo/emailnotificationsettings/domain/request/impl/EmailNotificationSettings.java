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

package com.ritense.valtimo.emailnotificationsettings.domain.request.impl;

import com.ritense.valtimo.emailnotificationsettings.domain.request.EmailNotificationSettingsRequest;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.DayOfWeek;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotEmpty;
import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotNull;
import static java.time.DayOfWeek.FRIDAY;
import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;
import static java.time.DayOfWeek.THURSDAY;
import static java.time.DayOfWeek.TUESDAY;
import static java.time.DayOfWeek.WEDNESDAY;
import static javax.persistence.FetchType.EAGER;

@Entity
@Table(name = "email_notification_settings")
public class EmailNotificationSettings {

    @Id
    @Column(name = "email_address", columnDefinition = "VARCHAR(255)")
    private String emailAddress;

    @Column(name = "reminder_notifications_enabled", columnDefinition = "BOOLEAN")
    private boolean reminderNotificationsEnabled;

    @Column(name = "task_notifications_enabled", columnDefinition = "BOOLEAN")
    private boolean taskNotificationsEnabled;

    @ElementCollection(fetch = EAGER)
    @Enumerated(EnumType.STRING)
    private Set<DayOfWeek> days;

    private EmailNotificationSettings() {
    }

    public EmailNotificationSettings(EmailNotificationSettingsRequest request, String emailAddress) {
        assertArgumentNotNull(request, "request cannot be null");
        assertArgumentNotEmpty(emailAddress, "emailAddress cannot be null");
        this.emailAddress = emailAddress;
        this.reminderNotificationsEnabled = request.reminderNotificationsEnabled();
        this.taskNotificationsEnabled = request.taskNotificationsEnabled();
        this.days = new HashSet<>();

        if (request.monday()) {
            days.add(MONDAY);
        }
        if (request.tuesday()) {
            days.add(TUESDAY);
        }
        if (request.wednesday()) {
            days.add(WEDNESDAY);
        }
        if (request.thursday()) {
            days.add(THURSDAY);
        }
        if (request.friday()) {
            days.add(FRIDAY);
        }
        if (request.saturday()) {
            days.add(SATURDAY);
        }
        if (request.sunday()) {
            days.add(SUNDAY);
        }
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    private boolean isReminderNotificationsEnabled() {
        return reminderNotificationsEnabled;
    }

    private boolean isTaskNotificationsEnabled() {
        return taskNotificationsEnabled;
    }

    public Set<DayOfWeek> getDays() {
        return days;
    }

    public JsonViewResult asJson() {
        return new JsonViewResult(isReminderNotificationsEnabled(), isTaskNotificationsEnabled(), getDays());
    }

    public static class JsonViewResult extends EmailNotificationSettingsRequestImpl {
        JsonViewResult(boolean reminderNotificationsEnabled, boolean taskNotificationsEnabled, Set<DayOfWeek> days) {
            super(
                reminderNotificationsEnabled,
                days.contains(MONDAY),
                days.contains(TUESDAY),
                days.contains(WEDNESDAY),
                days.contains(THURSDAY),
                days.contains(FRIDAY),
                days.contains(SATURDAY),
                days.contains(SUNDAY),
                taskNotificationsEnabled
            );
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EmailNotificationSettings)) {
            return false;
        }
        EmailNotificationSettings that = (EmailNotificationSettings) o;
        return Objects.equals(getEmailAddress(), that.getEmailAddress());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEmailAddress());
    }

}