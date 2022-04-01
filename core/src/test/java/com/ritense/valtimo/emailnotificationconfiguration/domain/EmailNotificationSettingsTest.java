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

package com.ritense.valtimo.emailnotificationconfiguration.domain;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ritense.valtimo.emailnotificationsettings.domain.request.impl.EmailNotificationSettingsRequestImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EmailNotificationSettingsTest {

    private final String correctJson = " {" +
        "\"emailNotifications\": true, " +
        "\"emailNotificationOnMonday\": true, " +
        "\"emailNotificationOnTuesday\": false, " +
        "\"emailNotificationOnWednesday\": false, " +
        "\"emailNotificationOnThursday\": false, " +
        "\"emailNotificationOnFriday\": false, " +
        "\"emailNotificationOnSaturday\": false, " +
        "\"emailNotificationOnSunday\": false, " +
        "\"taskNotifications\": true" +
        "}";

    private final String inCorrectJson = " {" +
        "\"emailNotifications\": true, " +
        "\"emailNotificationOnMonday\": true, " +
        "\"emailNotificationOnTuesday\": false, " +
        "\"emailNotificationOnWednesday\": false, " +
        "\"emailNotificationOnThursday\": false, " +
        "\"emailNotificationOnFriday\": false, " +
        "\"emailNotificationOnSaturday\": false, " +
        "\"emailNotificationOnSunday\": false, " +
        "\"taskNotifications\":  " +
        "}";

    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
    }

    @Test
    void shouldDeserializeCorrectJson() throws IOException {
        mapper.reader().forType(EmailNotificationSettingsRequestImpl.class).readValue(correctJson);
    }

    @Test
    void shouldNotDeserializeCorruptJson() {
        assertThrows(JsonParseException.class, () -> {
            mapper.reader().forType(EmailNotificationSettingsRequestImpl.class).readValue(inCorrectJson);
        });
    }

}