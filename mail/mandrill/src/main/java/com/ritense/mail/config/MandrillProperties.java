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

package com.ritense.mail.config;

import com.microtripit.mandrillapp.lutung.MandrillApi;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;
@Configuration
@ConfigurationProperties(prefix = "valtimo.mandrill")
public class MandrillProperties {
    private String apiKey;
    private String apiTestKey;
    private String dateFormat;
    private String notificationTemplate;
    private String completionTemplate;
    private Optional<String> fromEmailAddress = Optional.empty();
    private Optional<String> fromName = Optional.empty();
    private String webhookAuthenticationKey;
    private String webhookUrl;

    public MandrillProperties() {
    }

    public Optional<String> getFromEmailAddress() {
        return fromEmailAddress;
    }

    public void setFromEmailAddress(Optional<String> fromEmailAddress) {
        this.fromEmailAddress = fromEmailAddress;
    }

    public Optional<String> getFromName() {
        return fromName;
    }

    public void setFromName(Optional<String> fromName) {
        this.fromName = fromName;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getNotificationTemplate() {
        return notificationTemplate;
    }

    public void setNotificationTemplate(String notificationTemplate) {
        this.notificationTemplate = notificationTemplate;
    }

    public String getCompletionTemplate() {
        return completionTemplate;
    }

    public void setCompletionTemplate(String completionTemplate) {
        this.completionTemplate = completionTemplate;
    }

    public String getApiTestKey() {
        return apiTestKey;
    }

    public void setApiTestKey(String apiTestKey) {
        this.apiTestKey = apiTestKey;
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public MandrillApi createMandrillApi() {
        return new MandrillApi(getApiKey());
    }

    public String getWebhookAuthenticationKey() {
        return webhookAuthenticationKey;
    }

    public void setWebhookAuthenticationKey(String webhookAuthenticationKey) {
        this.webhookAuthenticationKey = webhookAuthenticationKey;
    }

    public String getWebhookUrl() {
        return webhookUrl;
    }

    public void setWebhookUrl(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }
}
