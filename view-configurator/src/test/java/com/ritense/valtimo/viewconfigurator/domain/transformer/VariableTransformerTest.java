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
import org.junit.jupiter.api.Test;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class VariableTransformerTest {

    private static final String STRING_KEY = "StringExample";
    private static final String STRING_VALUE = "aString";
    private static final String LONG_KEY = "LongExample";
    private static final long LONG_VALUE = 1L;
    private static final String BOOLEAN_KEY = "BooleanExample";
    private static final boolean BOOLEAN_VALUE = true;
    private static final String DATE_KEY = "DateExample";
    private static final Date DATE_VALUE = new Date();
    private static final String INTEGER_KEY = "IntegerExample";
    private static final int INTEGER_VALUE = 1;

    @Test
    public void variableTransform() {
        //given
        Map<String, Object> variables = new HashMap<>();
        variables.put(STRING_KEY, STRING_VALUE);
        variables.put(LONG_KEY, LONG_VALUE);
        variables.put(BOOLEAN_KEY, BOOLEAN_VALUE);
        variables.put(DATE_KEY, DATE_VALUE);
        variables.put(INTEGER_KEY, INTEGER_VALUE);

        //when
        List<ProcessDefinitionVariable> processDefinitionVariableList = variables
            .entrySet()
            .stream()
            .map(VariableTransformer.transform)
            .collect(Collectors.toList());

        //then
        assertEquals(variables.size(), processDefinitionVariableList.size());
        assertThat(processDefinitionVariableList, hasItem(new StringVariableType(STRING_KEY, STRING_KEY)));
        assertThat(processDefinitionVariableList, hasItem(new LongVariableType(LONG_KEY, LONG_KEY)));
        assertThat(processDefinitionVariableList, hasItem(new BooleanVariableType(BOOLEAN_KEY, BOOLEAN_KEY)));
        assertThat(processDefinitionVariableList, hasItem(new DateVariableType(DATE_KEY, DATE_KEY)));
        assertThat(processDefinitionVariableList, hasItem(new LongVariableType(INTEGER_KEY, INTEGER_KEY)));
    }

}