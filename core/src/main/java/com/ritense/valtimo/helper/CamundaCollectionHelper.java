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

package com.ritense.valtimo.helper;

import org.camunda.bpm.engine.delegate.VariableScope;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CamundaCollectionHelper {

    private static final String LOOP_COUNTER_KEY = "loopCounter";

    /*
     * This can be used to replace a variable back from the local scope of a task instance or sub process created by
     * a multi instance task or multi instance sub process back into the collection in the process variable scope.
     * */
    public void updateListElement(VariableScope variableScope, String collectionKey, String elementKey) {
        Map<String, Object> variables = variableScope.getVariables();

        Object elementToReplace = variables.get(elementKey);
        Objects.requireNonNull(elementToReplace, "element to update was not found in process variables");

        Integer loopCounter = (Integer) variables.get(LOOP_COUNTER_KEY);
        Objects.requireNonNull(loopCounter, "camunda loopcounter was not found in process variables. this is only available in a multi instance element");

        List list = (List) variables.get(collectionKey);
        Objects.requireNonNull(loopCounter, "collection was not found in process variables");

        list.set(loopCounter, elementToReplace);
    }
}
