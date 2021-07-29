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

package com.ritense.valtimo.emailnotificationsettings.repository;

import com.ritense.valtimo.emailnotificationsettings.domain.request.impl.EmailNotificationSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmailNotificationSettingsRepository extends JpaRepository<EmailNotificationSettings, String> {

    Optional<EmailNotificationSettings> findByEmailAddress(String emailAddress);

    @Query(" SELECT ens " +
        "        FROM EmailNotificationSettings ens " +
        "        WHERE ens.emailAddress = :emailAddress " +
        "        AND ens.reminderNotificationsEnabled = true")
    Optional<EmailNotificationSettings> findByEmailAddressAndNotificationsEnabled(@Param("emailAddress") String emailAddress);

    @Query(" SELECT ens " +
        "        FROM EmailNotificationSettings ens " +
        "    JOIN ens.days d " +
        "        WHERE ens.reminderNotificationsEnabled = true " +
        "        AND d = :today ")
    List<EmailNotificationSettings> findAllReminderNotificationsEnabled(@Param("today") DayOfWeek today);

    @Query(" SELECT CASE WHEN COUNT(ens)> 0 then true else false end " +
        "       FROM EmailNotificationSettings ens " +
        "       WHERE ens.emailAddress = :emailAddress " +
        "       AND ens.taskNotificationsEnabled = true")
    boolean existsByEmailAddressAndTaskNotificationsEnabled(@Param("emailAddress") String emailAddress);
}
