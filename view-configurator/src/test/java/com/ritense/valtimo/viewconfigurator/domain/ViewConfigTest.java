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

import com.ritense.valtimo.viewconfigurator.domain.type.LongVariableType;
import com.ritense.valtimo.viewconfigurator.domain.type.StringVariableType;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ViewConfigTest extends ViewConfigHelper {

    @Test
    public void initialiseViewConfig() {
        final String processDefinitionId = processDefinitionId();
        final LinkedHashSet<ProcessDefinitionVariable> defaultProcessDefinitionVariables = defaultProcessDefinitionVariables();
        final ViewVarGroup defaultViewVarGroup = defaultProcessDefinitionVariableGroup(defaultProcessDefinitionVariables);
        final Set<View> views = ViewFactory.getDefaultViews(defaultProcessDefinitionVariables, defaultViewVarGroup);

        Set<ProcessDefinitionVariable> processDefinitionVariables = processDefinitionVariables();
        ViewConfig viewConfig = ViewConfig.initialise(processDefinitionId, processDefinitionVariables);

        //Assert
        assertEquals(processDefinitionId, viewConfig.getProcessDefinitionId());
        assertTrue(viewConfig.getAllProcessDefinitionVariables().containsAll(processDefinitionVariables));
        assertEquals(views, viewConfig.getViews());
        assertEquals(3, viewConfig.getViews().size());

        for (View view : viewConfig.getViews()) {
            assertViewVarsHaveCorrectSequence(view);

            Set<ProcessDefinitionVariable> selectedProcessDefinitionVariables;

            if (view instanceof ProcessView && ((ProcessView) view).getType().supportsGroups()) {
                selectedProcessDefinitionVariables = view.getProcessDefinitionVariableGroups().stream()
                    .map(ViewVarGroup::getSelectedProcessDefinitionVariables)
                    .flatMap(Collection::stream)
                    .map(GroupVar::getVar)
                    .collect(Collectors.toSet());
            } else {
                selectedProcessDefinitionVariables = view.getSelectedProcessDefinitionVariables().stream()
                    .map(ViewVar::getVar)
                    .collect(Collectors.toSet());
            }
            assertTrue(selectedProcessDefinitionVariables.containsAll(defaultProcessDefinitionVariables));
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void assignVariableToViewAndKeepOrder() {

        //given
        ViewConfig viewConfig = viewConfigInitialised();
        Long viewId = viewConfig.getViews().stream().findFirst().get().getId();
        LinkedHashSet<Long> viewVarIds = new LinkedHashSet<>(Arrays.asList(P_ID, P_ID_2));

        //when
        viewConfig.assignVariablesToView(viewId, viewVarIds);

        //then
        Set<ViewVar> selectedProcessDefinitionVariables = viewConfig.getViews().stream().findFirst().get().getSelectedProcessDefinitionVariables();

        assertThat(selectedProcessDefinitionVariables, hasVariable1WithSequence(1));
        assertThat(selectedProcessDefinitionVariables, hasVariable2WithSequence(2));

        //reverse
        LinkedHashSet<Long> viewVarIdsReversed = new LinkedHashSet<>(Arrays.asList(P_ID_2, P_ID));
        viewConfig.assignVariablesToView(viewId, viewVarIdsReversed);

        selectedProcessDefinitionVariables = viewConfig.getViews().stream().findFirst().get().getSelectedProcessDefinitionVariables();

        assertThat(selectedProcessDefinitionVariables, hasVariable1WithSequence(2));
        assertThat(selectedProcessDefinitionVariables, hasVariable2WithSequence(1));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void removeVariableToView() {

        //given
        ViewConfig viewConfig = viewConfigInitialised();
        Long viewId = viewConfig.getViews().stream().findFirst().get().getId();
        LinkedHashSet<Long> viewVarIds = new LinkedHashSet<>(Arrays.asList(P_ID, P_ID_2));

        //when
        viewConfig.assignVariablesToView(viewId, viewVarIds);

        //then
        Set<ViewVar> selectedProcessDefinitionVariables = viewConfig.getViews().stream().findFirst().get().getSelectedProcessDefinitionVariables();
        assertEquals(2, selectedProcessDefinitionVariables.size());

        //minimize list same as removing
        LinkedHashSet<Long> viewVarIdsReversed = new LinkedHashSet<>(Arrays.asList(P_ID));
        viewConfig.assignVariablesToView(viewId, viewVarIdsReversed);

        selectedProcessDefinitionVariables = viewConfig.getViews().stream().findFirst().get().getSelectedProcessDefinitionVariables();
        assertEquals(1, selectedProcessDefinitionVariables.size());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void changeLabels() {

        //given
        ViewConfig viewConfig = viewConfigInitialised();
        ProcessDefinitionVariable processDefinitionVariable = viewConfig
            .getAllProcessDefinitionVariables()
            .stream()
            .filter(p -> p.getId() != null && p.getId().equals(P_ID))
            .findFirst()
            .get();

        final String newLabel = "a-changed-label";
        processDefinitionVariable.changeLabel(newLabel);

        //when
        Set<ProcessDefinitionVariable> variables = new LinkedHashSet<>();
        variables.add(processDefinitionVariable);
        viewConfig.changeLabels(variables);

        //then
        assertThat(viewConfig.getAllProcessDefinitionVariables(), hasItem(processDefinitionVariableWithLabel(newLabel)));
    }

    @Test
    public void assignAdditionalProcessVariables() {
        //given
        ViewConfig viewConfig = viewConfigInitialised();
        int initalNumberOfVars = viewConfig.getAllProcessDefinitionVariables().size();
        //when
        Set<ProcessDefinitionVariable> variables = new LinkedHashSet<>();
        variables.add(new StringVariableType(REFERENCE_ID_NEW, LABEL_NEW));
        viewConfig.assignAdditionalProcessVariables(variables);
        StringVariableType stringVariableType = new StringVariableType(REFERENCE_ID_NEW, LABEL_NEW);

        //then
        assertThat(viewConfig.getAllProcessDefinitionVariables(), hasSize(initalNumberOfVars + 1));
        assertThat(viewConfig.getAllProcessDefinitionVariables(), hasItem(stringVariableType));
    }

    @Test
    public void assignAdditionalProcessVariablesShouldNotBeDuplicate() {
        //given
        ViewConfig viewConfig = viewConfigInitialised();
        int initalNumberOfVars = viewConfig.getAllProcessDefinitionVariables().size();

        //when
        Set<ProcessDefinitionVariable> variables = new LinkedHashSet<>();
        ProcessDefinitionVariable duplicateVariable = new StringVariableType(REFERENCE_ID, LABEL);
        variables.add(duplicateVariable);

        viewConfig.assignAdditionalProcessVariables(variables);

        //then
        assertThat(viewConfig.getAllProcessDefinitionVariables(), hasSize(initalNumberOfVars));
    }

    @Test
    public void addView() {
        ViewConfig viewConfig = viewConfigInitialised();

        assertEquals(3, viewConfig.getViews().size());

        View view = new TaskView(UUID.randomUUID().toString(), TaskViewType.LIST);
        viewConfig.addView(view);

        assertEquals(4, viewConfig.getViews().size());
        assertTrue(viewConfig.getViews().contains(view));
    }

    @Test
    public void addSameViewTwiceShouldFail() {
        ViewConfig viewConfig = viewConfigInitialised();
        assertEquals(3, viewConfig.getViews().size());
        View view = new TaskView(UUID.randomUUID().toString(), TaskViewType.LIST);
        viewConfig.addView(view);
        assertEquals(4, viewConfig.getViews().size());

        assertThrows(IllegalStateException.class, () -> viewConfig.addView(view));
    }

    @Test
    public void createViewConfigWithoutProcessDefinitionIdShouldFail() {
        assertThrows(NullPointerException.class, () -> new ViewConfig(null, null, null));
    }

    @Test
    public void createViewConfigWithEmptyProcessDefinitionIdShouldFail() {
        final String processDefinitionId = "";
        assertThrows(IllegalArgumentException.class, () -> new ViewConfig(processDefinitionId, null, null));
    }

    @Test
    public void createViewConfigWithoutViewsShouldFail() {
        final String processDefinitionId = processDefinitionId();
        final Set<ProcessDefinitionVariable> processDefinitionVariables = processDefinitionVariables();

        assertThrows(NullPointerException.class, () -> new ViewConfig(processDefinitionId, processDefinitionVariables, null));
    }

    @Test
    public void createViewConfigWithEmptyViewsShouldFail() {
        final String processDefinitionId = processDefinitionId();
        final Set<ProcessDefinitionVariable> processDefinitionVariables = processDefinitionVariables();
        final Set<View> views = new HashSet<>();

        assertThrows(IllegalArgumentException.class, () -> new ViewConfig(processDefinitionId, processDefinitionVariables, views));
    }

    @Test
    public void createViewConfigWithoutProcessDefinitionVariablesShouldFail() {
        final String processDefinitionId = processDefinitionId();
        final LinkedHashSet<ProcessDefinitionVariable> defaultProcessDefinitionVariables = defaultProcessDefinitionVariables();
        final ViewVarGroup defaultViewVarGroup = defaultProcessDefinitionVariableGroup(defaultProcessDefinitionVariables);
        final Set<View> views = ViewFactory.getDefaultViews(defaultProcessDefinitionVariables, defaultViewVarGroup);

        assertThrows(NullPointerException.class, () -> new ViewConfig(processDefinitionId, null, views));
    }

    @Test
    public void createViewConfigWithEmptyProcessDefinitionVariablesShouldFail() {
        final String processDefinitionId = processDefinitionId();
        final Set<ProcessDefinitionVariable> processDefinitionVariables = new HashSet<>();
        final LinkedHashSet<ProcessDefinitionVariable> defaultProcessDefinitionVariables = defaultProcessDefinitionVariables();
        final ViewVarGroup defaultViewVarGroup = defaultProcessDefinitionVariableGroup(defaultProcessDefinitionVariables);
        final Set<View> views = ViewFactory.getDefaultViews(defaultProcessDefinitionVariables, defaultViewVarGroup);

        assertThrows(IllegalArgumentException.class, () -> new ViewConfig(processDefinitionId, processDefinitionVariables, views));
    }

    @Test
    public void fieldsAreConfiguredAfterConfiguringFromOtherViewConfig() {
        //given
        ViewConfig originalViewConfig = viewConfigInitialised();
        Long viewId = originalViewConfig.getViews().stream().findFirst().get().getId();
        LinkedHashSet<Long> viewVarIds = new LinkedHashSet<>(Arrays.asList(P_ID_2, P_ID, P_ID_3));

        originalViewConfig.assignVariablesToView(viewId, viewVarIds);

        ViewConfig newViewConfig = viewConfigInitialised();

        //when
        newViewConfig.configureFrom(originalViewConfig);

        //then
        Set<ViewVar> selectedProcessDefinitionVariables = newViewConfig.getViews().stream().findFirst().get().getSelectedProcessDefinitionVariables();

        assertThat(selectedProcessDefinitionVariables, hasVariable1WithSequence(2));
        assertThat(selectedProcessDefinitionVariables, hasVariable2WithSequence(1));
        assertThat(selectedProcessDefinitionVariables, hasVariable3WithSequence(3));
    }

    @Test
    public void whenTypeOfFieldIsDifferentItIsNotConfiguredFromOtherViewConfig() {
        //given
        //original view config with string variable
        Set<ProcessDefinitionVariable> variables = new LinkedHashSet<>();

        ProcessDefinitionVariable variable = new StringVariableType(REFERENCE_ID, LABEL);
        variable.setId(P_ID);
        variables.add(variable);

        ViewConfig originalViewConfig = ViewConfig.initialise(processDefinitionId(), variables);

        Long viewId = originalViewConfig.getViews().stream().findFirst().get().getId();
        LinkedHashSet<Long> viewVarIds = new LinkedHashSet<>(Arrays.asList(P_ID));

        originalViewConfig.assignVariablesToView(viewId, viewVarIds);

        //new view config with long variable with same reference_id
        variables = new LinkedHashSet<>();

        variable = new LongVariableType(REFERENCE_ID, LABEL);
        variable.setId(P_ID);
        variables.add(variable);

        ViewConfig newViewConfig = ViewConfig.initialise(processDefinitionId(), variables);

        //when
        newViewConfig.configureFrom(originalViewConfig);

        //then
        Set<ViewVar> selectedProcessDefinitionVariables = newViewConfig.getViews().stream().findFirst().get().getSelectedProcessDefinitionVariables();

        assertThat(selectedProcessDefinitionVariables, not(hasItem(withId(P_ID))));
    }

}