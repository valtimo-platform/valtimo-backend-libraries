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

package com.ritense.mail.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ritense.mail.config.MandrillProperties;
import com.ritense.mail.domain.webhook.MandrillMessageEvent;
import com.ritense.mail.domain.webhook.MandrillSyncEvent;
import com.ritense.mail.domain.webhook.MandrillWebhookRequest;
import com.ritense.valtimo.contract.basictype.EmailAddress;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Base64Utils;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static com.ritense.mail.domain.webhook.SyncEventEnum.BLACKLIST;

public class WebhookService {

    private static final Logger logger = LoggerFactory.getLogger(WebhookService.class);
    private final MandrillProperties mandrillProperties;
    private final BlacklistService blacklistService;

    public WebhookService(MandrillProperties mandrillProperties, BlacklistService blacklistService) {
        this.mandrillProperties = mandrillProperties;
        this.blacklistService = blacklistService;
    }

    public boolean isRequestValid(String authenticationKey, MultiValueMap<String, String> body) {
        String url = mandrillProperties.getWebhookUrl();
        SortedSet<String> sortedKeys = new TreeSet<>(body.keySet());
        final String urlWithQueryParams = url + sortedKeys.stream()
            .map(key -> String.join("", key + body.getFirst(key)))
            .collect(Collectors.joining());

        final String base64Signature = Base64Utils.encodeToString(
            new HmacUtils(HmacAlgorithms.HMAC_SHA_1, mandrillProperties.getWebhookAuthenticationKey()).hmac(urlWithQueryParams)
        );
        return base64Signature.equals(authenticationKey);
    }

    public MandrillWebhookRequest getMandrillEventsFromJson(String json) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(json);
        MandrillWebhookRequest events = new MandrillWebhookRequest();

        for (JsonNode node : jsonNode) {
            if (node.has("event")) {
                MandrillMessageEvent messageEvent = objectMapper.treeToValue(node, MandrillMessageEvent.class);
                events.addMessageEvent(messageEvent);
            } else if (node.has("type") && node.get("type").asText().equals(BLACKLIST.name())) {
                MandrillSyncEvent syncEvent = objectMapper.treeToValue(node, MandrillSyncEvent.class);
                events.addSyncEvent(syncEvent);
            }
        }
        return events;
    }

    public void handleMandrillEvents(MandrillWebhookRequest mandrillWebhookRequest) {
        List<MandrillMessageEvent> messageEvents = mandrillWebhookRequest.getMessageEvents();
        messageEvents.forEach(messageEvent -> {
            if (messageEvent.triggersBlacklisting()) {
                EmailAddress mailToBlacklist = EmailAddress.from(messageEvent.getMessage().getEmail());
                String cause = String.format("Mandrill{%s}", messageEvent);
                blacklistService.blacklist(mailToBlacklist.get(), LocalDateTime.now(), cause);
                logger.debug("{} added to mandrill blacklist with cause: {}", mailToBlacklist, cause);
            }
        });

        List<MandrillSyncEvent> syncEvents = mandrillWebhookRequest.getSyncEvents();
        syncEvents.forEach(syncEvent -> {
            if (syncEvent.triggersBlacklisting()) {
                EmailAddress mailToBlacklist = EmailAddress.from(syncEvent.getReject().getEmail());
                String cause = String.format("Mandrill{%s}", syncEvent);
                blacklistService.blacklist(mailToBlacklist.get(), LocalDateTime.now(), cause);
                logger.debug("{} added to mandrill blacklist with cause: {}", mailToBlacklist, cause);
            }
        });
    }

}
