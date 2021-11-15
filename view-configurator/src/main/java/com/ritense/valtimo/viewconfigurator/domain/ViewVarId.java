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
public class ViewVarId implements Serializable {

    @Column(name = "view_id", columnDefinition = "INT")
    private Long viewId;

    @Column(name = "var_id", columnDefinition = "INT")
    private Long varId;

    private ViewVarId() {
    }

    public ViewVarId(Long viewId, Long varId) {
        this.viewId = viewId;
        this.varId = varId;
    }

    public Long getViewId() {
        return viewId;
    }

    public Long getVarId() {
        return varId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ViewVarId)) {
            return false;
        }
        ViewVarId viewVarId = (ViewVarId) o;
        return
            Objects.equals(getViewId(), viewVarId.getViewId())
            &&
            Objects.equals(getVarId(), viewVarId.getVarId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getViewId(), getVarId());
    }
}
