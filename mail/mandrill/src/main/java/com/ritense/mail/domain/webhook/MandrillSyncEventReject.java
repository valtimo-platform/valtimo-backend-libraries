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

package com.ritense.mail.domain.webhook;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MandrillSyncEventReject {
    private String reason;
    private String detail;
    private String email;

    public MandrillSyncEventReject(String reason, String detail, String email) {
        this.reason = reason;
        this.detail = detail;
        this.email = email;
    }

    public MandrillSyncEventReject() {
    }

    public String getReason() {
        return this.reason;
    }

    public String getDetail() {
        return this.detail;
    }

    public String getEmail() {
        return this.email;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MandrillSyncEventReject that = (MandrillSyncEventReject) o;
        return Objects.equals(getReason(), that.getReason()) && Objects.equals(getDetail(), that.getDetail()) && Objects.equals(getEmail(), that.getEmail());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getReason(), getDetail(), getEmail());
    }

    public String toString() {
        return "MandrillSyncEventReject(reason=" + this.getReason() + ", detail=" + this.getDetail() + ", email=" + this.getEmail() + ")";
    }
}