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
import javax.persistence.Embeddable;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Table(name = "context_processes")
public class ContextProcess implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "process", length = 250, nullable = false)
    private String processDefinitionKey;

    @Column(name = "visible_in_menu", nullable = false)
    private boolean visibleInMenu = true;

    private ContextProcess() {
}

    public ContextProcess(String processDefinitionKey, boolean visibleInMenu) {
        if (processDefinitionKey == null || processDefinitionKey.length() == 0) {
            throw new NullPointerException("ProcessDefinitionKey not defined!");
        }
        this.processDefinitionKey = processDefinitionKey;
        this.visibleInMenu = visibleInMenu;
    }

    public String getProcessDefinitionKey() {
        return processDefinitionKey;
    }

    public boolean isVisibleInMenu() {
        return visibleInMenu;
    }

    public boolean getVisibleInMenu() {
        return visibleInMenu;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ContextProcess that = (ContextProcess) o;
        return
            visibleInMenu == that.visibleInMenu
            &&
            Objects.equals(processDefinitionKey, that.processDefinitionKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(processDefinitionKey, visibleInMenu);
    }
}
