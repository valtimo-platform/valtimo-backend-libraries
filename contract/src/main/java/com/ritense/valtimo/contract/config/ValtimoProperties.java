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

package com.ritense.valtimo.contract.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConstructorBinding
@ConfigurationProperties(prefix = "valtimo")
public class ValtimoProperties {

    private final App app;

    private final Mandrill mandrill;

    private final JWT jwt;

    private final Portal portal;

    private final Process process;

    public ValtimoProperties(
        App app,
        Mandrill mandrill,
        JWT jwt,
        Portal portal,
        Process process
    ) {
        this.app = app != null ? app : new App();
        this.mandrill = mandrill != null ? mandrill : new Mandrill();
        this.jwt = jwt != null ? jwt : new JWT();
        this.portal = portal != null ? portal : new Portal();
        this.process = process != null ? process : new Process();
    }

    public App getApp() {
        return app;
    }

    public Mandrill getMandrill() {
        return mandrill;
    }

    public JWT getJwt() {
        return jwt;
    }

    public Portal getPortal() {
        return portal;
    }

    public Process getProcess() {
        return process;
    }

    public static class App {

        private String hostname;
        private String scheme;

        public String getHostname() {
            return hostname;
        }

        public void setHostname(String hostname) {
            this.hostname = hostname;
        }

        public String getScheme() {
            return scheme;
        }

        public void setScheme(String scheme) {
            this.scheme = scheme;
        }

        public String getBaselUrl() {
            return String.format("%s://%s/", scheme, hostname);
        }
    }

    public static class Mandrill {
        private String apiKey;
        private String notificationTemplate;
        private String completionTemplate;
        private String reminderTemplate;
        private String sender;

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

        public String getReminderTemplate() {
            return reminderTemplate;
        }

        public void setReminderTemplate(String reminderTemplate) {
            this.reminderTemplate = reminderTemplate;
        }

        public String getSender() {
            return sender;
        }

        public void setSender(String sender) {
            this.sender = sender;
        }
    }

    public static class JWT {
        private String secret;
        private long tokenValidityInSeconds = 180000;

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public long getTokenValidityInSeconds() {
            return tokenValidityInSeconds;
        }

        public void setTokenValidityInSeconds(long tokenValidityInSeconds) {
            this.tokenValidityInSeconds = tokenValidityInSeconds;
        }
    }

    public static class Portal {

        private String hostname;
        private String scheme;

        public String getHostname() {
            return hostname;
        }

        public void setHostname(String hostname) {
            this.hostname = hostname;
        }

        public String getScheme() {
            return scheme;
        }

        public void setScheme(String scheme) {
            this.scheme = scheme;
        }

        public String getBaselUrl() {
            return String.format("%s://%s/", scheme, hostname);
        }
    }

    public static class Process {
        private boolean systemProcessUpdatable = false;

        public boolean isSystemProcessUpdatable() {
            return systemProcessUpdatable;
        }

        public void setSystemProcessUpdatable(boolean systemProcessUpdatable) {
            this.systemProcessUpdatable = systemProcessUpdatable;
        }
    }

}
