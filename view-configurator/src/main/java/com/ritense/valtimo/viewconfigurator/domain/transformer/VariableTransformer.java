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

package com.ritense.valtimo.viewconfigurator.domain.transformer;

import com.ritense.valtimo.viewconfigurator.domain.ProcessDefinitionVariable;
import com.ritense.valtimo.viewconfigurator.domain.type.BooleanVariableType;
import com.ritense.valtimo.viewconfigurator.domain.type.DateVariableType;
import com.ritense.valtimo.viewconfigurator.domain.type.LongVariableType;
import com.ritense.valtimo.viewconfigurator.domain.type.StringVariableType;
import java.util.Map;
import java.util.function.Function;

public class VariableTransformer {

    private static final String UNKNOWN_TYPE = "unknown";

    public static final Function<Map.Entry, ProcessDefinitionVariable> transform = mapEntry -> {
        ProcessDefinitionVariable processDefinitionVariable = null;
        String referenceId = mapEntry.getKey().toString();
        String label = mapEntry.getKey().toString();
        String className = mapEntry.getValue() == null ? UNKNOWN_TYPE : mapEntry.getValue().getClass().getSimpleName().toLowerCase();
        switch (className) {
            case "string":
                processDefinitionVariable = new StringVariableType(referenceId, label);
                break;
            case "boolean":
                processDefinitionVariable = new BooleanVariableType(referenceId, label);
                break;
            case "date":
                processDefinitionVariable = new DateVariableType(referenceId, label);
                break;
            case "long":
                processDefinitionVariable = new LongVariableType(referenceId, label);
                break;
            case "integer":
                processDefinitionVariable = new LongVariableType(referenceId, label);
                break;
            default:
                break;
        }
        return processDefinitionVariable;
    };

}