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

package com.ritense.valtimo.viewconfigurator.service;

import com.ritense.valtimo.viewconfigurator.domain.ProcessDefinitionVariable;
import com.ritense.valtimo.viewconfigurator.domain.View;
import com.ritense.valtimo.viewconfigurator.domain.ViewConfig;
import com.ritense.valtimo.viewconfigurator.domain.type.StringVariableType;
import com.ritense.valtimo.viewconfigurator.repository.ViewConfigRepository;
import com.ritense.valtimo.viewconfigurator.service.impl.ProcessDefinitionVariableServiceImpl;
import com.ritense.valtimo.viewconfigurator.service.impl.ViewConfigServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ViewConfigServiceImplTest {

    private static final String REFERENCE_ID = "reference-id";
    private static final String LABEL = "label";

    private ViewConfigServiceImpl viewConfigServiceImpl;
    private ProcessDefinitionVariableService processDefinitionVariableService;
    private ViewConfigRepository viewConfigRepository;
    private ProcessDefinitionService processDefinitionService;

    @BeforeEach
    public void setUp() {
        viewConfigRepository = mock(ViewConfigRepository.class);
        processDefinitionVariableService = mock(ProcessDefinitionVariableServiceImpl.class);
        processDefinitionService = mock(ProcessDefinitionService.class);
        viewConfigServiceImpl = new ViewConfigServiceImpl(processDefinitionVariableService, viewConfigRepository, processDefinitionService);
    }

    @Test
    public void getViewConfig_ViewConfigDoesNotExist_ThenShouldInitialse() throws Exception {
        //given
        when(viewConfigRepository.findByProcessDefinitionId(any())).thenReturn(Optional.empty());

        Set<ProcessDefinitionVariable> allVariables = processDefinitionVariables();
        when(processDefinitionVariableService.extractVariables(any())).thenReturn(allVariables);

        final String processDefinitionId = UUID.randomUUID().toString();

        //when
        Optional<ViewConfig> viewConfigOptional = viewConfigServiceImpl.createViewConfiguration(processDefinitionId);

        //then
        assertThat(viewConfigOptional).isPresent();

        verify(viewConfigRepository).findByProcessDefinitionId(any());
        verify(processDefinitionVariableService).extractVariables(any());
        verify(viewConfigRepository).saveAndFlush(any(ViewConfig.class));
    }

    @Test
    public void getViewConfig_ViewConfigExists_thenReturn() throws Exception {
        //given
        ViewConfig viewConfig = viewConfig();
        when(viewConfigRepository.findByProcessDefinitionId(any())).thenReturn(Optional.of(viewConfig()));

        //when
        final Optional<ViewConfig> viewConfigOptional = viewConfigServiceImpl.createViewConfiguration(viewConfig.getProcessDefinitionId());

        //then
        assertThat(viewConfigOptional).isPresent();
        verify(processDefinitionVariableService, never()).extractVariables(any());
        verify(viewConfigRepository, never()).save(any(ViewConfig.class));
    }

    @Test
    public void assignVariableToView_whenViewConfigIsNotFound_thenReturnEmpty() {
        //given
        final Long viewConfigId = viewConfigId();

        //when
        when(viewConfigRepository.findById(viewConfigId)).thenReturn(Optional.empty());
        final Optional<ViewConfig> viewConfigOptional = viewConfigServiceImpl.assignVariablesToView(viewConfigId, null, null);

        //then
        assertThat(viewConfigOptional).isEmpty();
    }

    @Test
    public void changeLabels_whenViewConfigIsNotFound_thenReturnEmpty() {
        //given
        final Long viewConfigId = viewConfigId();

        //when
        when(viewConfigRepository.findById(viewConfigId)).thenReturn(Optional.empty());
        final Optional<ViewConfig> viewConfigOptional = viewConfigServiceImpl.changeLabels(viewConfigId, null);

        //then
        assertThat(viewConfigOptional).isEmpty();
    }

    @Test
    public void assignAdditionalProcessVariables_whenViewConfigIsNotFound_thenReturnEmpty() {
        //when
        when(viewConfigRepository.findByProcessDefinitionId(any())).thenReturn(Optional.empty());
        final Optional<ViewConfig> viewConfigOptional = viewConfigServiceImpl.assignAdditionalProcessVariables(any(), null);

        //then
        assertThat(viewConfigOptional).isEmpty();
    }

    private String processDefinitionId() {
        return UUID.randomUUID().toString();
    }

    private Long viewConfigId() {
        return 1L;
    }

    private Set<ProcessDefinitionVariable> processDefinitionVariables() {
        Set<ProcessDefinitionVariable> allVariables = new HashSet<>();
        allVariables.add(new StringVariableType(REFERENCE_ID, LABEL));
        allVariables.add(new StringVariableType(REFERENCE_ID, LABEL));
        return allVariables;
    }

    private ViewConfig viewConfig() {
        Set<ProcessDefinitionVariable> variables = new HashSet<>();
        StringVariableType stringVariableType = new StringVariableType(UUID.randomUUID().toString(), "label");
        stringVariableType.setId(1L);
        variables.add(stringVariableType);

        ViewConfig viewConfig = ViewConfig.initialise(processDefinitionId(), variables);
        viewConfig.setId(1L);

        long i = 1L;
        for (View view : viewConfig.getViews()) {
            view.setId(i);
            i++;
        }
        return viewConfig;
    }

}