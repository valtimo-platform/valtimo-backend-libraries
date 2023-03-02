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

package com.ritense.valtimo.emailnotificationsettings.domain.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public interface EmailNotificationSettingsRequest {

    @JsonProperty(value = "emailNotifications")
    boolean reminderNotificationsEnabled();

    @JsonProperty(value = "emailNotificationOnMonday")
    boolean monday();

    @JsonProperty(value = "emailNotificationOnTuesday")
    boolean tuesday();

    @JsonProperty(value = "emailNotificationOnWednesday")
    boolean wednesday();

    @JsonProperty(value = "emailNotificationOnThursday")
    boolean thursday();

    @JsonProperty(value = "emailNotificationOnFriday")
    boolean friday();

    @JsonProperty(value = "emailNotificationOnSaturday")
    boolean saturday();

    @JsonProperty(value = "emailNotificationOnSunday")
    boolean sunday();

    @JsonProperty(value = "taskNotifications")
    boolean taskNotificationsEnabled();

}
