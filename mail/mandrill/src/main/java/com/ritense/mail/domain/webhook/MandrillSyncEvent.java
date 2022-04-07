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
import static com.ritense.mail.domain.webhook.SyncEventEnum.ADD;
import static com.ritense.mail.domain.webhook.SyncEventEnum.BLACKLIST;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MandrillSyncEvent {
    @JsonProperty("type")
    private String type;

    @JsonProperty("action")
    private String action;

    @JsonProperty("reject")
    private MandrillSyncEventReject reject;

    public MandrillSyncEvent(String type, String action, MandrillSyncEventReject reject) {
        this.type = type;
        this.action = action;
        this.reject = reject;
    }

    public MandrillSyncEvent() {
    }

    public boolean triggersBlacklisting() {
        return BLACKLIST.name().equals(type) && ADD.name().equals(action);
    }

    public String getType() {
        return this.type;
    }

    public String getAction() {
        return this.action;
    }

    public MandrillSyncEventReject getReject() {
        return this.reject;
    }

    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
    }

    @JsonProperty("action")
    public void setAction(String action) {
        this.action = action;
    }

    @JsonProperty("reject")
    public void setReject(MandrillSyncEventReject reject) {
        this.reject = reject;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MandrillSyncEvent that = (MandrillSyncEvent) o;
        return Objects.equals(getType(), that.getType()) && Objects.equals(getAction(), that.getAction()) && Objects.equals(getReject(), that.getReject());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType(), getAction(), getReject());
    }

    public String toString() {
        return "MandrillSyncEvent(type=" + this.getType() + ", action=" + this.getAction() + ", reject=" + this.getReject() + ")";
    }
}