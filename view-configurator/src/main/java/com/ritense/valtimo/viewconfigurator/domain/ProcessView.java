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

import com.fasterxml.jackson.annotation.JsonView;
import com.ritense.valtimo.viewconfigurator.web.rest.view.Views;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.util.LinkedHashSet;
import java.util.Objects;

@Entity
@DiscriminatorValue(value = "processView")
public class ProcessView extends View {

    @JsonView(Views.ViewConfig.class)
    @Column(name = "process_view_type")
    private ProcessViewType type;

    private ProcessView() {
        super();
    }

    public ProcessView(ProcessViewType processViewType, LinkedHashSet<ProcessDefinitionVariable> selectedVariables) {
        Objects.requireNonNull(processViewType, "type cannot be null");
        Objects.requireNonNull(selectedVariables, "selectedVariables cannot be null");
        if (selectedVariables.isEmpty()) {
            throw new IllegalArgumentException("Must supply at least one processDefinitionVariable");
        }
        this.type = processViewType;
        super.assignVariables(selectedVariables);
    }

    public ProcessView(ProcessViewType processViewType, ViewVarGroup viewVarGroup) {
        Objects.requireNonNull(processViewType, "type cannot be null");
        Objects.requireNonNull(viewVarGroup, "viewVarGroup cannot be null");

        if (!processViewType.supportsGroups()) {
            throw new IllegalStateException("View type does not support variable groups");
        }

        this.type = processViewType;
        super.addViewVarGroup(viewVarGroup);
    }

    @Override
    protected boolean supportsGroups() {
        return type.supportsGroups();
    }

    @Override
    protected boolean equivalentOf(View otherView) {
        if (otherView instanceof ProcessView) {
            ProcessView otherProcessView = (ProcessView)otherView;
            return otherProcessView.getType().equals(this.getType());
        }
        return false;
    }

    public ProcessViewType getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ProcessView)) {
            return false;
        }
        ProcessView that = (ProcessView) o;
        return getType() == that.getType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType());
    }

}