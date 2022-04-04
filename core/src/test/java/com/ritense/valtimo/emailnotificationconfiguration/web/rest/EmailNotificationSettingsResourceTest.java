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

package com.ritense.valtimo.emailnotificationconfiguration.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ritense.valtimo.emailnotificationsettings.domain.request.impl.EmailNotificationSettings;
import com.ritense.valtimo.emailnotificationsettings.domain.request.impl.EmailNotificationSettingsRequestImpl;
import com.ritense.valtimo.emailnotificationsettings.service.impl.EmailNotificationSettingsServiceImpl;
import com.ritense.valtimo.emailnotificationsettings.web.rest.EmailNotificationSettingsResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import static com.ritense.valtimo.emailnotificationconfiguration.helper.EmailNotificationSettingsHelper.disabledEmailNotificationSettings;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
class EmailNotificationSettingsResourceTest {

    private EmailNotificationSettingsServiceImpl emailNotificationService;
    private EmailNotificationSettingsResource emailNotificationSettingsResource;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private static final String JOHN = "john@ritense.com";

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        emailNotificationService = mock(EmailNotificationSettingsServiceImpl.class);
        emailNotificationSettingsResource = new EmailNotificationSettingsResource(emailNotificationService);
        mockMvc = MockMvcBuilders
            .standaloneSetup(emailNotificationSettingsResource)
            .build();
    }

    @Test
    void shouldReturnNoContent() throws Exception {
        when(emailNotificationService.getSettingsFor(anyString())).thenReturn(
            Optional.empty()
        );
        mockMvc.perform(get("/api/email-notification-settings")
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isNoContent());
    }

    @Test
    public void shouldReturnOkAndConfiguration() throws Exception {
        when(emailNotificationService.getSettingsFor(any())).thenReturn(
            disabledEmailNotificationSettings()
        );
        mockMvc.perform(get("/api/email-notification-settings")
            .contentType(MediaType.APPLICATION_JSON_VALUE))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));
    }

    @Test
    public void shouldStoreSettingsForUser() throws Exception {
        final var request = objectMapper.readValue(jsonString(), EmailNotificationSettingsRequestImpl.class);
        final var settings = new EmailNotificationSettings(request, JOHN);

        when(emailNotificationService.process(any(), any())).thenReturn(settings);

        mockMvc.perform(put("/api/email-notification-settings")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(jsonString().getBytes(StandardCharsets.UTF_8)))
            .andDo(print())
            .andExpect(status().isOk());
    }

    private String jsonString() {
        return " {" +
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
    }

}