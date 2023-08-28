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

package com.ritense.valtimo.emailnotificationsettings.web.rest;

import com.ritense.valtimo.contract.utils.SecurityUtils;
import com.ritense.valtimo.emailnotificationsettings.domain.request.impl.EmailNotificationSettings;
import com.ritense.valtimo.emailnotificationsettings.domain.request.impl.EmailNotificationSettingsRequestImpl;
import com.ritense.valtimo.emailnotificationsettings.service.EmailNotificationSettingsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE;

@RestController
@RequestMapping(value = "/api/v1", produces = APPLICATION_JSON_UTF8_VALUE)
public class EmailNotificationSettingsResource {

    private final EmailNotificationSettingsService emailNotificationService;

    public EmailNotificationSettingsResource(EmailNotificationSettingsService emailNotificationService) {
        this.emailNotificationService = emailNotificationService;
    }

    @GetMapping("/email-notification-settings")
    public ResponseEntity<EmailNotificationSettings.JsonViewResult> getSettingsFor() {
        final String emailAddress = SecurityUtils.getCurrentUserLogin();
        return emailNotificationService.getSettingsFor(emailAddress)
            .map(settings -> ResponseEntity.ok(settings.asJson()))
            .orElse(ResponseEntity.noContent().build());
    }

    @PutMapping("/email-notification-settings")
    public ResponseEntity<EmailNotificationSettings.JsonViewResult> process(
        @RequestBody @Valid EmailNotificationSettingsRequestImpl request
    ) {
        final String emailAddress = SecurityUtils.getCurrentUserLogin();
        final EmailNotificationSettings emailNotificationSettings = emailNotificationService.process(request, emailAddress);
        return ResponseEntity.ok(emailNotificationSettings.asJson());
    }

}
