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

package com.ritense.audit.domain;

import com.fasterxml.jackson.annotation.JsonView;
import com.ritense.valtimo.contract.audit.view.AuditView;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.time.LocalDateTime;
import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentLength;
import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotEmpty;
import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentTrue;

@Embeddable
public class MetaData {

    @JsonView(AuditView.Internal.class)
    @Column(name = "origin", nullable = false, updatable = false)
    private String origin;

    @JsonView(AuditView.Public.class)
    @Column(name = "occurred_on", nullable = false, updatable = false)
    private LocalDateTime occurredOn;

    @JsonView(AuditView.Public.class)
    @Column(name = "`user`", nullable = false, updatable = false)
    private String user;

    public MetaData(String origin, LocalDateTime occurredOn, String user) {
        assertArgumentNotEmpty(origin, "origin cannot be empty");
        assertArgumentLength(origin, 255, "origin max length is 255");
        assertArgumentTrue(!occurredOn.isAfter(LocalDateTime.now()), "occurredOn cannot be in the future");
        assertArgumentNotEmpty(user, "user cannot be empty");
        assertArgumentLength(user, 255, "user max length is 255");
        this.origin = origin;
        this.occurredOn = occurredOn;
        this.user = user;
    }

    private MetaData() {
    }

    public String getOrigin() {
        return origin;
    }

    public LocalDateTime getOccurredOn() {
        return occurredOn;
    }

    public String getUser() {
        return user;
    }

    @Override
    public String toString() {
        return "MetaData{" +
            "origin='" + origin + '\'' +
            ", occurredOn=" + occurredOn +
            ", user='" + user + '\'' +
            '}';
    }
}