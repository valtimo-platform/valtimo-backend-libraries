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

import static com.microtripit.mandrillapp.lutung.view.MandrillWebhook.HARD_BOUNCE;
import static com.microtripit.mandrillapp.lutung.view.MandrillWebhook.REJECT;
import static com.microtripit.mandrillapp.lutung.view.MandrillWebhook.SPAM;
import static com.microtripit.mandrillapp.lutung.view.MandrillWebhook.UNSUB;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MandrillMessageEvent {

    @JsonProperty("event")
    private String event;

    @JsonProperty("ts")
    private Long timestamp;

    @JsonProperty("msg")
    private MandrillMessageEventMessage message;

    public MandrillMessageEvent(String event, Long timestamp, MandrillMessageEventMessage message) {
        this.event = event;
        this.timestamp = timestamp;
        this.message = message;
    }

    public MandrillMessageEvent() {
    }

    public boolean triggersBlacklisting() {
        return HARD_BOUNCE.equals(event)
            || REJECT.equals(event)
            || UNSUB.equals(event)
            || SPAM.equals(event);
    }

    public String getEvent() {
        return this.event;
    }

    public Long getTimestamp() {
        return this.timestamp;
    }

    public MandrillMessageEventMessage getMessage() {
        return this.message;
    }

    @JsonProperty("event")
    public void setEvent(String event) {
        this.event = event;
    }

    @JsonProperty("ts")
    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    @JsonProperty("msg")
    public void setMessage(MandrillMessageEventMessage message) {
        this.message = message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MandrillMessageEvent that = (MandrillMessageEvent) o;
        return Objects.equals(getEvent(), that.getEvent()) && Objects.equals(getTimestamp(), that.getTimestamp()) && Objects.equals(getMessage(), that.getMessage());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEvent(), getTimestamp(), getMessage());
    }

    @Override
    public String toString() {
        return "MandrillMessageEvent{" +
            "event='" + event + '\'' +
            ", timestamp=" + timestamp +
            ", message=" + message +
            '}';
    }
}