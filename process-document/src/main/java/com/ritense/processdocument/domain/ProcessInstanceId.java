/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
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

package com.ritense.processdocument.domain;

import com.fasterxml.jackson.annotation.JsonValue;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import java.lang.reflect.InvocationTargetException;
import static com.ritense.valtimo.contract.utils.AssertionConcern.assertArgumentNotNull;

public interface ProcessInstanceId {

    @JsonValue
    String toString();

    static <T extends ProcessInstanceId> T fromExecution(DelegateExecution execution, Class<T> targetClass) {
        assertArgumentNotNull(execution, "execution is required");
        assertArgumentNotNull(targetClass, "targetClass is required");
        T target;
        try {
            final String id = execution.getProcessInstanceId();
            target = targetClass.getConstructor(String.class).newInstance(id);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new IllegalArgumentException("Cannot create instance of ProcessInstanceId class" + targetClass);
        }
        return target;
    }

}
