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

package com.ritense.mail.web.rest;

import com.ritense.mail.domain.webhook.MandrillWebhookRequest;
import com.ritense.mail.service.WebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class WebhookResource {

    private final WebhookService webhookService;

    @GetMapping(value = "/mandrill/webhook")
    public ResponseEntity<Void> exists() {
        // Exists for Mandrill's check whether or not the endpoint exists.
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/mandrill/webhook", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Void> mandrillWebhook(
        @RequestHeader(value = "X-Mandrill-Signature") String authenticationKey,
        @RequestBody MultiValueMap<String, String> body
    ) throws IOException {
        if (!webhookService.isRequestValid(authenticationKey, body)) {
            return ResponseEntity.badRequest().build();
        }
        logger.debug("Mandrill webhook triggerd with authenticationKey: {}", authenticationKey);
        String json = body.getFirst("mandrill_events");
        MandrillWebhookRequest mandrillWebhookRequest = webhookService.getMandrillEventsFromJson(json);
        webhookService.handleMandrillEvents(mandrillWebhookRequest);
        return ResponseEntity.ok().build();
    }

}