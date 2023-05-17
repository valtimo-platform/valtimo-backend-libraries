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

package com.ritense.valtimo.service;

import com.ritense.valtimo.BaseIntegrationTest;
import com.ritense.valtimo.contract.authentication.model.ValtimoUser;
import com.ritense.valtimo.domain.user.UserSettings;
import com.ritense.valtimo.repository.UserSettingsRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class UserSettingsServiceIntTest extends BaseIntegrationTest {
    @Autowired
    private UserSettingsService userSettingsService;

    @Autowired
    private UserSettingsRepository userSettingsRepository;

    @Test
    void getCurrentUserSettings() {
        // given
        ValtimoUser user = new ValtimoUser();
        user.setId("12345");

        UserSettings userSettings = new UserSettings(
            "12345",
            Map.of(
                "key1", "value1",
                "key2", "value2"
            )
        );

        // when
        userSettingsRepository.save(userSettings);

        // then
        Optional<UserSettings> foundUserSettings = userSettingsService.findUserSettings(user);
        assertThat(foundUserSettings.isPresent());
        assertThat(foundUserSettings.get().getUserId()).isEqualTo("12345");
        assertThat(foundUserSettings.get().getSettings()).isEqualTo(userSettings.getSettings());

    }

    @Test
    void saveUserSettings() {
        // given
        ValtimoUser user = new ValtimoUser();
        user.setId("12345");

        UserSettings userSettings = new UserSettings(
            "12345",
            Map.of(
                "key1", "value1",
                "key2", "value2"
            )
        );

        // when
        userSettingsService.saveUserSettings(user, userSettings.getSettings());

        // then
        Optional<UserSettings> foundUserSettings = userSettingsRepository.findById("12345");
        assertThat(foundUserSettings.isPresent());
        assertThat(foundUserSettings.get().getUserId()).isEqualTo("12345");
        assertThat(foundUserSettings.get().getSettings()).isEqualTo(userSettings.getSettings());
    }
}
