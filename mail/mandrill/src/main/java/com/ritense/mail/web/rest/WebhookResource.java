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

package com.ritense.mail.web.rest;

import com.ritense.mail.domain.webhook.MandrillWebhookRequest;
import com.ritense.mail.service.WebhookService;
import com.ritense.valtimo.contract.annotation.SkipComponentScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import static com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE;

@RestController
@SkipComponentScan
@RequestMapping(value = "/api", produces = APPLICATION_JSON_UTF8_VALUE)
public class WebhookResource {

    private static final Logger logger = LoggerFactory.getLogger(WebhookResource.class);
    private final WebhookService webhookService;

    public WebhookResource(WebhookService webhookService) {
        this.webhookService = webhookService;
    }

    @GetMapping("/v1/mandrill/webhook")
    public ResponseEntity<Void> exists() {
        // Exists for Mandrill's check whether or not the endpoint exists.
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/v1/mandrill/webhook", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Void> mandrillWebhook(
        @RequestHeader("X-Mandrill-Signature") String authenticationKey,
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
