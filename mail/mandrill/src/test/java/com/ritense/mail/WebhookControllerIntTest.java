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

package com.ritense.mail;

import com.ritense.mail.domain.webhook.MandrillWebhookRequest;
import com.ritense.mail.service.WebhookService;
import com.ritense.mail.web.rest.WebhookResource;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
public class WebhookControllerIntTest {

    private WebhookResource webhookResource;
    private WebhookService webhookService;
    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        webhookService = mock(WebhookService.class);
        webhookResource = new WebhookResource(webhookService);
        mockMvc = MockMvcBuilders
            .standaloneSetup(webhookResource)
            .build();
    }

    @Test
    public void mandrillWebhookValidKey() throws Exception {
        when(webhookService.isRequestValid(anyString(), any(MultiValueMap.class))).thenReturn(true);
        doNothing().when(webhookService).handleMandrillEvents(isA(MandrillWebhookRequest.class));

        mockMvc.perform(post("/api/v1/mandrill/webhook")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .header("X-Mandrill-Signature", "something")
            .content(String.format("mandrill_events=%s", UriUtils.encode(getWebhookJsonAsString(), "UTF-8"))))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    public void mandrillWebhookInvalidKey() throws Exception {
        when(webhookService.isRequestValid(anyString(), any(MultiValueMap.class))).thenReturn(false);
        doNothing().when(webhookService).handleMandrillEvents(isA(MandrillWebhookRequest.class));

        mockMvc.perform(post("/api/v1/mandrill/webhook")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .header("X-Mandrill-Signature", "something")
            .content(String.format("mandrill_events=%s", UriUtils.encode(getWebhookJsonAsString(), "UTF-8"))))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    private String getWebhookJsonAsString() {
        ClassLoader classLoader = getClass().getClassLoader();
        File webhookJson = new File(classLoader.getResource("webhook.json").getFile());
        try {
            FileInputStream fis = new FileInputStream(webhookJson);
            String webhookAsString = IOUtils.toString(fis, "UTF-8");
            return webhookAsString;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}