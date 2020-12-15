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

package com.ritense.valtimo.domain.contexts;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "user_context")
public class UserContext implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "user_id", unique = true, nullable = false)
    private String userId;

    @Column(name = "context_id", length = 250, nullable = false)
    private Long contextId;

    private UserContext() {
    }

    public UserContext(Long contextId, String userId) {
        this.contextId = contextId;
        this.userId = userId;
    }

    private void setContextId(Long contextId) {
        this.contextId = contextId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Long getContextId() {
        return contextId;
    }

    public String getUserId() {
        return userId;
    }
}
