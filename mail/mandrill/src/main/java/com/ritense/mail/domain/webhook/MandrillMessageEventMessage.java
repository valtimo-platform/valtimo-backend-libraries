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

package com.ritense.mail.domain.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MandrillMessageEventMessage {

    @JsonProperty("subject")
    private String subject;

    @JsonProperty("email")
    private String email;

    @JsonProperty("sender")
    private String sender;

    @JsonProperty("state")
    private String state;

    @JsonProperty("bounce_description")
    private String bounceDescription;

    public MandrillMessageEventMessage(String subject, String email, String sender, String state, String bounceDescription) {
        this.subject = subject;
        this.email = email;
        this.sender = sender;
        this.state = state;
        this.bounceDescription = bounceDescription;
    }

    public MandrillMessageEventMessage() {
    }

    public String getSubject() {
        return this.subject;
    }

    public String getEmail() {
        return this.email;
    }

    public String getSender() {
        return this.sender;
    }

    public String getState() {
        return this.state;
    }

    public String getBounceDescription() {
        return this.bounceDescription;
    }

    @JsonProperty("subject")
    public void setSubject(String subject) {
        this.subject = subject;
    }

    @JsonProperty("email")
    public void setEmail(String email) {
        this.email = email;
    }

    @JsonProperty("sender")
    public void setSender(String sender) {
        this.sender = sender;
    }

    @JsonProperty("state")
    public void setState(String state) {
        this.state = state;
    }

    @JsonProperty("bounce_description")
    public void setBounceDescription(String bounceDescription) {
        this.bounceDescription = bounceDescription;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MandrillMessageEventMessage that = (MandrillMessageEventMessage) o;
        return Objects.equals(getSubject(), that.getSubject()) && Objects.equals(getEmail(), that.getEmail()) && Objects.equals(getSender(), that.getSender()) && Objects.equals(getState(), that.getState()) && Objects.equals(getBounceDescription(), that.getBounceDescription());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSubject(), getEmail(), getSender(), getState(), getBounceDescription());
    }

    @Override
    public String toString() {
        return "MandrillMessageEventMessage{" +
            "subject='" + subject + '\'' +
            ", email='" + email + '\'' +
            ", sender='" + sender + '\'' +
            ", state='" + state + '\'' +
            ", bounceDescription='" + bounceDescription + '\'' +
            '}';
    }
}