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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ViewConfigWithGroupsTest extends ViewConfigHelper {

    @Test
    @SuppressWarnings("unchecked")
    public void assignVariableGroupsToView() {

        final String newGroupLabel1 = "New group 1";
        final String newGroupLabel2 = "New group 2";

        //given
        ViewConfig viewConfig = viewConfigInitialised();
        Long detailViewId = viewConfig.getViews().stream()
            .filter(view -> view instanceof ProcessView && ((ProcessView) view).getType().equals(ProcessViewType.DETAIL))
            .findFirst()
            .get()
            .getId();

        List<ViewConfigurationRequestGroup> viewConfigurationRequestGroups = List.of(
            new ViewConfigurationRequestGroup(
                null,
                newGroupLabel1,
                1,
                List.of(
                    new ViewConfigurationRequestVariable(P_ID, 1),
                    new ViewConfigurationRequestVariable(P_ID_2, 2)
                )
            ),
            new ViewConfigurationRequestGroup(
                null,
                newGroupLabel2,
                2,
                List.of(
                    new ViewConfigurationRequestVariable(P_ID_4, 1),
                    new ViewConfigurationRequestVariable(P_ID_3, 2)
                )
            )
        );

        //when
        viewConfig.assignGroupsToView(detailViewId, viewConfigurationRequestGroups);

        //then
        View detailView = viewConfig.getViews().stream()
            .filter(view -> view instanceof ProcessView && ((ProcessView) view).getType().equals(ProcessViewType.DETAIL))
            .findFirst()
            .get();

        assertEquals(2, detailView.getProcessDefinitionVariableGroups().size());

        assertThat(detailView.getProcessDefinitionVariableGroups(),
            allOf(
                hasItem(allOf(
                    withGroupLabel(newGroupLabel1),
                    withSequence(1),
                    hasVariables(
                        hasVariable1WithSequence(1),
                        hasVariable2WithSequence(2)
                    )
                )),
                hasItem(allOf(
                    withGroupLabel(newGroupLabel2),
                    withSequence(2),
                    hasVariables(
                        hasVariable3WithSequence(2),
                        hasVariable4WithSequence(1)
                    )
                ))
            )
        );

    }

    @Test
    public void fieldsAreConfiguredAfterConfiguringFromOtherViewConfig() {
        //given
        final String newGroupLabel = "New group 1";

        ViewConfig originalViewConfig = viewConfigInitialised();
        Long detailViewId = originalViewConfig.getViews().stream()
            .filter(view -> view instanceof ProcessView && ((ProcessView) view).getType().equals(ProcessViewType.DETAIL))
            .findFirst()
            .get()
            .getId();

        List<ViewConfigurationRequestGroup> viewConfigurationRequestGroups = List.of(
            new ViewConfigurationRequestGroup(
                null,
                newGroupLabel,
                1,
                List.of(
                    new ViewConfigurationRequestVariable(P_ID, 1),
                    new ViewConfigurationRequestVariable(P_ID_2, 2)
                )
            )
        );

        originalViewConfig.assignGroupsToView(detailViewId, viewConfigurationRequestGroups);

        ViewConfig newViewConfig = viewConfigInitialised();

        //when
        newViewConfig.configureFrom(originalViewConfig);

        //then
        View detailView = newViewConfig.getViews().stream()
            .filter(view -> view instanceof ProcessView && ((ProcessView) view).getType().equals(ProcessViewType.DETAIL))
            .findFirst()
            .get();

        assertEquals(1, detailView.getProcessDefinitionVariableGroups().size());

        assertThat(detailView.getProcessDefinitionVariableGroups(),
            hasItem(allOf(
                withGroupLabel(newGroupLabel),
                withSequence(1),
                hasVariables(
                    hasVariable1WithSequence(1),
                    hasVariable2WithSequence(2)
                )
            ))
        );
    }

    @Test
    public void whenTypeOfFieldIsDifferentItIsNotConfiguredFromOtherViewConfig() {
        //given
        //original view config with string variable
        final String newGroupLabel = "New group 1";

        Set<ProcessDefinitionVariable> variables = new LinkedHashSet<>();

        ProcessDefinitionVariable variable = new StringVariableType(REFERENCE_ID, LABEL);
        variable.setId(P_ID);
        variables.add(variable);

        ViewConfig originalViewConfig = ViewConfig.initialise(processDefinitionId(), variables);
        long i = 1L;
        for (View view : originalViewConfig.getViews()) {
            view.setId(i);
            i++;
        }

        Long detailViewId = originalViewConfig.getViews().stream()
            .filter(view -> view instanceof ProcessView && ((ProcessView) view).getType().equals(ProcessViewType.DETAIL))
            .findFirst()
            .get()
            .getId();

        List<ViewConfigurationRequestGroup> viewConfigurationRequestGroups = List.of(
            new ViewConfigurationRequestGroup(
                null,
                newGroupLabel,
                1,
                List.of(
                    new ViewConfigurationRequestVariable(P_ID, 1)
                )
            )
        );

        originalViewConfig.assignGroupsToView(detailViewId, viewConfigurationRequestGroups);

        //new view config with long variable with same reference_id
        variables = new LinkedHashSet<>();

        variable = new LongVariableType(REFERENCE_ID, LABEL);
        variable.setId(P_ID);
        variables.add(variable);

        ViewConfig newViewConfig = ViewConfig.initialise(processDefinitionId(), variables);

        //when
        newViewConfig.configureFrom(originalViewConfig);

        //then
        View detailView = newViewConfig.getViews().stream()
            .filter(view -> view instanceof ProcessView && ((ProcessView) view).getType().equals(ProcessViewType.DETAIL))
            .findFirst()
            .get();

        assertEquals(1, detailView.getProcessDefinitionVariableGroups().size());

        assertThat(detailView.getProcessDefinitionVariableGroups(),
            hasItem(allOf(
                withGroupLabel(newGroupLabel),
                withSequence(1),
                hasProperty("selectedProcessDefinitionVariables", hasSize(0))
            ))
        );
    }

}