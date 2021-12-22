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

import com.ritense.mail.config.MandrillProperties;
import com.ritense.mail.domain.webhook.MandrillMessageEvent;
import com.ritense.mail.domain.webhook.MandrillMessageEventMessage;
import com.ritense.mail.domain.webhook.MandrillWebhookRequest;
import com.ritense.mail.service.BlacklistService;
import com.ritense.mail.service.WebhookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.microtripit.mandrillapp.lutung.view.MandrillWebhook.HARD_BOUNCE;
import static com.microtripit.mandrillapp.lutung.view.MandrillWebhook.SOFT_BOUNCE;
import static com.microtripit.mandrillapp.lutung.view.MandrillWebhook.UNSUB;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class WebhookServiceTest {

    private BlacklistService blacklistService;
    private MandrillProperties mandrillProperties;
    private WebhookService webhookService;

    private static final String[] TEST_EMAIL_ADDRESSES = new String[]{
        "cremonini@yahoo.ca",
        "smallpaul@msn.com"
    };

    @BeforeEach
    public void setUp() {
        blacklistService = mock(BlacklistService.class);
        mandrillProperties = mock(MandrillProperties.class);
        webhookService = new WebhookService(mandrillProperties, blacklistService);
    }

    @Test
    public void shouldHandleSyncAndMessageEvents() throws IOException {
        doNothing().when(blacklistService).blacklist(anyString(), any(), anyString());

        webhookService.handleMandrillEvents(getMandrillEvents());

        verify(blacklistService, times(2)).blacklist(anyString(), any(), anyString());
    }

    private MandrillWebhookRequest getMandrillEvents() {
        MandrillWebhookRequest mandrillWebhookRequest = new MandrillWebhookRequest();

        //Message 1
        MandrillMessageEventMessage mandrillMessageEventMessageOne = mandrillMessageEventMessage(TEST_EMAIL_ADDRESSES[0]);
        MandrillMessageEvent mandrillMessageEventOne = new MandrillMessageEvent(HARD_BOUNCE, 1530783724L, mandrillMessageEventMessageOne);

        mandrillWebhookRequest.addMessageEvent(mandrillMessageEventOne);

        //Message 2
        MandrillMessageEventMessage mandrillMessageEventMessageTwo = mandrillMessageEventMessage(TEST_EMAIL_ADDRESSES[1]);
        MandrillMessageEvent mandrillMessageEventTwo = new MandrillMessageEvent(UNSUB, 1530783724L, mandrillMessageEventMessageTwo);

        mandrillWebhookRequest.addMessageEvent(mandrillMessageEventTwo);

        //Message 3
        MandrillMessageEventMessage mandrillMessageEventMessageThree = mandrillMessageEventMessage(TEST_EMAIL_ADDRESSES[1]);
        MandrillMessageEvent mandrillMessageEventThree = new MandrillMessageEvent(SOFT_BOUNCE, 1530783724L, mandrillMessageEventMessageThree);

        mandrillWebhookRequest.addMessageEvent(mandrillMessageEventThree);

        return mandrillWebhookRequest;
    }

    private MandrillMessageEventMessage mandrillMessageEventMessage(String emailAddress) {
        return new MandrillMessageEventMessage(
            "subject",
            emailAddress,
            "sender",
            "state",
            "description"
        );
    }

}