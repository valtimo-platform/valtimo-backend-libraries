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

import com.ritense.valtimo.viewconfigurator.domain.type.BooleanVariableType;
import com.ritense.valtimo.viewconfigurator.domain.type.DateVariableType;
import com.ritense.valtimo.viewconfigurator.domain.type.StringVariableType;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static com.ritense.valtimo.contract.viewconfigurator.ProcessVariableGroupsConstants.DEFAULT_GROUP_LABEL;
import static com.ritense.valtimo.contract.viewconfigurator.ProcessVariablesConstants.ACTIVE_LABEL;
import static com.ritense.valtimo.contract.viewconfigurator.ProcessVariablesConstants.ACTIVE_REF;
import static com.ritense.valtimo.contract.viewconfigurator.ProcessVariablesConstants.BUSINESS_KEY_LABEL;
import static com.ritense.valtimo.contract.viewconfigurator.ProcessVariablesConstants.BUSINESS_KEY_REF;
import static com.ritense.valtimo.contract.viewconfigurator.ProcessVariablesConstants.PROCESS_ENDED_LABEL;
import static com.ritense.valtimo.contract.viewconfigurator.ProcessVariablesConstants.PROCESS_ENDED_REF;
import static com.ritense.valtimo.contract.viewconfigurator.ProcessVariablesConstants.PROCESS_STARTED_LABEL;
import static com.ritense.valtimo.contract.viewconfigurator.ProcessVariablesConstants.PROCESS_STARTED_REF;
import static com.ritense.valtimo.contract.viewconfigurator.ProcessVariablesConstants.START_PROCESS_USER_LABEL;
import static com.ritense.valtimo.contract.viewconfigurator.ProcessVariablesConstants.START_PROCESS_USER_REF;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.AllOf.allOf;

public class DefaultProcessVarFactoryTest {

    @Test
    public void getDefaultProcessVariables() {
        final Set<ProcessDefinitionVariable> defaultProcessVariables = DefaultProcessVarFactory.getDefaultProcessVariables();

        assertThat(defaultProcessVariables, isCollectionOfDefaultVariables());
    }

    @Test
    public void getDefaultProcessVariableGroup() {
        LinkedHashSet<ProcessDefinitionVariable> defaultProcessVariables = DefaultProcessVarFactory.getDefaultProcessVariables();
        final ViewVarGroup defaultProcessVariablesGroup = DefaultProcessVarFactory.getDefaultProcessVariablesGroup(defaultProcessVariables);

        Set<ProcessDefinitionVariable> processDefinitionVariables = defaultProcessVariablesGroup.getSelectedProcessDefinitionVariables()
            .stream()
            .map(GroupVar::getVar)
            .collect(Collectors.toSet());

        assertThat(defaultProcessVariablesGroup, allOf(
            hasProperty("label", is(DEFAULT_GROUP_LABEL)),
            hasProperty("sequence", is(new Integer(1)))
        ));

        assertThat(processDefinitionVariables, isCollectionOfDefaultVariables());
    }

    private Matcher isCollectionOfDefaultVariables() {
        return Matchers.<Collection<ProcessDefinitionVariable>>allOf(
            hasSize(5),
            hasItem(allOf(
                instanceOf(StringVariableType.class),
                hasProperty("referenceId", is(BUSINESS_KEY_REF)),
                hasProperty("label", is(BUSINESS_KEY_LABEL))
            )),
            hasItem(allOf(
                instanceOf(DateVariableType.class),
                hasProperty("referenceId", is(PROCESS_STARTED_REF)),
                hasProperty("label", is(PROCESS_STARTED_LABEL))
            )),
            hasItem(allOf(
                instanceOf(DateVariableType.class),
                hasProperty("referenceId", is(PROCESS_ENDED_REF)),
                hasProperty("label", is(PROCESS_ENDED_LABEL))
            )),
            hasItem(allOf(
                instanceOf(BooleanVariableType.class),
                hasProperty("referenceId", is(ACTIVE_REF)),
                hasProperty("label", is(ACTIVE_LABEL))
            )),
            hasItem(allOf(
                instanceOf(StringVariableType.class),
                hasProperty("referenceId", is(START_PROCESS_USER_REF)),
                hasProperty("label", is(START_PROCESS_USER_LABEL))
            ))
        );
    }
}