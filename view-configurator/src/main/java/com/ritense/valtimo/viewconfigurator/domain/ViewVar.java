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

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonView;
import com.ritense.valtimo.viewconfigurator.domain.type.EnumVariableType;
import com.ritense.valtimo.viewconfigurator.web.rest.view.Views;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Entity
@Table(name = "view_var")
public class ViewVar implements Comparable<ViewVar> {

    @EmbeddedId
    private ViewVarId id;

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("viewId")
    private View view;

    @JsonAnySetter
    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("varId")
    private ProcessDefinitionVariable var;

    @JsonView(Views.ViewConfig.class)
    @Column(name = "sequence", columnDefinition = "INT")
    private Integer sequence;

    private ViewVar() {
    }

    public ViewVar(View view, ProcessDefinitionVariable var, Integer sequence) {
        this.view = view;
        this.var = var;
        this.id = new ViewVarId(view.getId(), var.getId());
        this.sequence = sequence;
    }

    /**
     * We can't use @JsonUnwrap in combination with @JsonTypeInfo.
     * So this creates a custom JSON response so the '@type' is being
     * added to the response.
     *
     * @return The map which eventually will be serialized by Jackson
     */
    @JsonAnyGetter
    public Map<String, Object> getViewVar() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("id", this.var.getId());
        map.put("label", this.var.getLabel());
        map.put("referenceId", this.var.getReferenceId());
        map.put("sequence", this.sequence);
        map.put("@type", this.var.getTypeName());
        if (this.var instanceof EnumVariableType) {
            map.put("items", ((EnumVariableType) this.var).getItems());
        }
        return map;
    }

    public ViewVarId getId() {
        return id;
    }

    public View getView() {
        return view;
    }

    public ProcessDefinitionVariable getVar() {
        return var;
    }

    public Integer getSequence() {
        return sequence;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ViewVar)) {
            return false;
        }
        ViewVar viewVar = (ViewVar) o;
        return
            Objects.equals(getId(), viewVar.getId())
            &&
            Objects.equals(getView(), viewVar.getView())
            &&
            Objects.equals(getVar(), viewVar.getVar())
            &&
            Objects.equals(getSequence(), viewVar.getSequence());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getView(), getVar(), getSequence());
    }


    @Override
    public int compareTo(ViewVar otherViewVar) {
        final int before = -1;
        final int equal = 0;
        final int after = 1;

        if (this == otherViewVar) {
            return equal;
        }

        if (this.getSequence() < otherViewVar.getSequence()) {
            return before;
        }
        if (this.getSequence() > otherViewVar.getSequence()) {
            return after;
        }

        return equal;
    }
}
