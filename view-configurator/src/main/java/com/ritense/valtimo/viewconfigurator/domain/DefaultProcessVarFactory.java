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
import java.util.LinkedHashSet;
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

public class DefaultProcessVarFactory {

    public static LinkedHashSet<ProcessDefinitionVariable> getDefaultProcessVariables() {
        LinkedHashSet<ProcessDefinitionVariable> variables = new LinkedHashSet<>();
        variables.add(new StringVariableType(BUSINESS_KEY_REF, BUSINESS_KEY_LABEL));
        variables.add(new DateVariableType(PROCESS_STARTED_REF, PROCESS_STARTED_LABEL));
        variables.add(new DateVariableType(PROCESS_ENDED_REF, PROCESS_ENDED_LABEL));
        variables.add(new BooleanVariableType(ACTIVE_REF, ACTIVE_LABEL));
        variables.add(new StringVariableType(START_PROCESS_USER_REF, START_PROCESS_USER_LABEL));
        return variables;
    }

    public static ViewVarGroup getDefaultProcessVariablesGroup(LinkedHashSet<ProcessDefinitionVariable> processVariables) {
        return new ViewVarGroup(DEFAULT_GROUP_LABEL, 1, processVariables);
    }

}
