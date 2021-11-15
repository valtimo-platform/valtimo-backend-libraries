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

import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ProcessViewTest {

    @Test
    public void createProcessViewDetail() {
        final LinkedHashSet<ProcessDefinitionVariable> defaultProcessVariables = defaultProcessDefinitionVariables();
        ProcessView processViewDetail = new ProcessView(ProcessViewType.DETAIL, defaultProcessVariables);
        assertEquals(ProcessViewType.DETAIL, processViewDetail.getType());

        final Set<ProcessDefinitionVariable> selectedProcessDefinitionVariables =
            processViewDetail.getSelectedProcessDefinitionVariables().stream().map(ViewVar::getVar).collect(Collectors.toSet());
        assertTrue(selectedProcessDefinitionVariables.containsAll(defaultProcessVariables));
    }

    @Test
    public void createProcessViewList() {
        final LinkedHashSet<ProcessDefinitionVariable> defaultProcessVariables = defaultProcessDefinitionVariables();
        ProcessView processViewList = new ProcessView(ProcessViewType.LIST, defaultProcessVariables);
        assertEquals(ProcessViewType.LIST, processViewList.getType());

        final Set<ProcessDefinitionVariable> selectedProcessDefinitionVariables =
            processViewList.getSelectedProcessDefinitionVariables().stream().map(ViewVar::getVar).collect(Collectors.toSet());
        assertTrue(selectedProcessDefinitionVariables.containsAll(defaultProcessVariables));
    }

    @Test
    public void createProcessViewSearch() {
        final LinkedHashSet<ProcessDefinitionVariable> defaultProcessVariables = defaultProcessDefinitionVariables();
        ProcessView processViewSearch = new ProcessView(ProcessViewType.SEARCH, defaultProcessVariables);
        assertEquals(ProcessViewType.SEARCH, processViewSearch.getType());

        final Set<ProcessDefinitionVariable> selectedProcessDefinitionVariables =
            processViewSearch.getSelectedProcessDefinitionVariables().stream().map(ViewVar::getVar).collect(Collectors.toSet());
        assertTrue(selectedProcessDefinitionVariables.containsAll(defaultProcessVariables));
    }

    @Test
    public void createProcessViewWithTypeIsNullShouldFail() {
        assertThrows(NullPointerException.class, () -> {
            new ProcessView(null, (LinkedHashSet) null);
        });
    }

    @Test
    public void createProcessViewWithSelectedVarsIsNullShouldFail() {
        assertThrows(NullPointerException.class, () -> {
            new ProcessView(ProcessViewType.LIST, (LinkedHashSet) null);
        });
    }

    @Test
    public void createProcessViewWithGroupIsNullShouldFail() {
        assertThrows(NullPointerException.class, () -> {
            new ProcessView(ProcessViewType.DETAIL, (ViewVarGroup) null);
        });
    }

    @Test
    public void createProcessViewWithGroupForTypeThatDoesNotSupportGroupsShouldFail() {
        assertThrows(IllegalStateException.class, () -> {
            new ProcessView(ProcessViewType.LIST, new ViewVarGroup());
        });
    }

    private LinkedHashSet<ProcessDefinitionVariable> defaultProcessDefinitionVariables() {
        return DefaultProcessVarFactory.getDefaultProcessVariables();
    }
}