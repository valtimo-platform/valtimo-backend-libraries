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

package com.ritense.valtimo.service.util;

import org.camunda.bpm.engine.variable.VariableMap;
import org.hamcrest.collection.IsMapContaining;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.CombinableMatcher.both;

class FormUtilsTest {

    public static final long LONG_VALUE = 10L;
    public static final Date DATE_VALUE = new Date();

    @Test
    void createTypedVariableMap() {
        Map<String, Object> variables = new HashMap<>();
        variables.put("leeftijd", LONG_VALUE);
        variables.put("huidige_tijd", DATE_VALUE);

        VariableMap typedVariableMap = FormUtils.createTypedVariableMap(variables);

        assertThat(typedVariableMap, both(IsMapContaining.hasKey("leeftijd")).and(IsMapContaining.hasValue(LONG_VALUE)));
        assertThat(typedVariableMap, both(IsMapContaining.hasKey("huidige_tijd")).and(IsMapContaining.hasValue(DATE_VALUE)));
    }

}