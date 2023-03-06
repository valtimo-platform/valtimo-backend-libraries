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


public class EmailNotificationSettingsRequestImpl implements EmailNotificationSettingsRequest {

    private boolean reminderNotificationsEnabled;
    private boolean taskNotificationsEnabled;
    private boolean monday;
    private boolean tuesday;
    private boolean wednesday;
    private boolean thursday;
    private boolean friday;
    private boolean saturday;
    private boolean sunday;

    private EmailNotificationSettingsRequestImpl() {
    }

    public EmailNotificationSettingsRequestImpl(
        boolean reminderNotificationsEnabled,
        boolean monday,
        boolean tuesday,
        boolean wednesday,
        boolean thursday,
        boolean friday,
        boolean saturday,
        boolean sunday,
        boolean taskNotifications
    ) {
        this.reminderNotificationsEnabled = reminderNotificationsEnabled;
        this.monday = monday;
        this.tuesday = tuesday;
        this.wednesday = wednesday;
        this.thursday = thursday;
        this.friday = friday;
        this.saturday = saturday;
        this.sunday = sunday;
        this.taskNotificationsEnabled = taskNotifications;
    }

    @Override
    public boolean reminderNotificationsEnabled() {
        return reminderNotificationsEnabled;
    }

    @Override
    public boolean monday() {
        return monday;
    }

    @Override
    public boolean tuesday() {
        return tuesday;
    }

    @Override
    public boolean wednesday() {
        return wednesday;
    }

    @Override
    public boolean thursday() {
        return thursday;
    }

    @Override
    public boolean friday() {
        return friday;
    }

    @Override
    public boolean saturday() {
        return saturday;
    }

    @Override
    public boolean sunday() {
        return sunday;
    }

    @Override
    public boolean taskNotificationsEnabled() {
        return taskNotificationsEnabled;
    }

}
