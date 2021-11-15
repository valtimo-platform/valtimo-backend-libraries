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

package com.ritense.valtimo.viewconfigurator.domain;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class GroupVarId implements Serializable {

    @Column(name = "group_id", columnDefinition = "INT")
    private Long groupId;

    @Column(name = "var_id", columnDefinition = "INT")
    private Long varId;

    private GroupVarId() {
    }

    public GroupVarId(Long groupId, Long varId) {
        this.groupId = groupId;
        this.varId = varId;
    }

    public Long getGroupId() {
        return groupId;
    }

    public Long getVarId() {
        return varId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GroupVarId)) {
            return false;
        }
        GroupVarId viewVarId = (GroupVarId) o;
        return
            Objects.equals(getGroupId(), viewVarId.getGroupId())
            &&
            Objects.equals(getVarId(), viewVarId.getVarId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getGroupId(), getVarId());
    }
}
