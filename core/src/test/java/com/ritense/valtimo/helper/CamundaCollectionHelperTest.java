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

import org.assertj.core.util.Lists;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CamundaCollectionHelperTest {

    private static final String COLLECTION_NAME = "collection";
    private static final String ELEMENT_NAME = "element";
    private static final String LOOP_COUNTER_NAME = "loopCounter";

    private static final String COLLECTION_ELEMENT_STRING = "a";
    private static final Integer COLLECTION_ELEMENT_INTEGER = 2;
    private static final Boolean COLLECTION_ELEMENT_BOOLEAN = true;
    private static final String COLLECTION_ELEMENT_NEW_VALUE = "new value";

    private static final Integer LOOP_COUNTER_VALUE = 2;
    private DelegateTask delegateTask;
    private Map variableMap;
    private List collection;
    private CamundaCollectionHelper camundaCollectionHelper = new CamundaCollectionHelper();

    @BeforeEach
    public void setUp() {
        delegateTask = mock(DelegateTask.class);
        collection = Lists.newArrayList(COLLECTION_ELEMENT_STRING, COLLECTION_ELEMENT_INTEGER, COLLECTION_ELEMENT_BOOLEAN);
        variableMap = new HashMap();
        variableMap.put(COLLECTION_NAME, collection);
        variableMap.put(ELEMENT_NAME, COLLECTION_ELEMENT_NEW_VALUE);
        variableMap.put(LOOP_COUNTER_NAME, LOOP_COUNTER_VALUE);
        when(delegateTask.getVariables()).thenReturn(variableMap);
    }

    @Test
    public void shouldUpdateList() {
        camundaCollectionHelper.updateListElement(delegateTask, COLLECTION_NAME, ELEMENT_NAME);

        assertThat(collection.get(0)).isEqualTo(COLLECTION_ELEMENT_STRING);
        assertThat(collection.get(1)).isEqualTo(COLLECTION_ELEMENT_INTEGER);
        assertThat(collection.get(2)).isEqualTo(COLLECTION_ELEMENT_NEW_VALUE);
    }

    @Test
    public void shouldThrowExceptionWhenCollectionDoesntExist() {
        assertThrows(NullPointerException.class, () -> camundaCollectionHelper.updateListElement(delegateTask, "fake_collection_key", ELEMENT_NAME));
    }

    @Test
    public void shouldThrowExceptionWhenElementDoesntExist() {
        assertThrows(NullPointerException.class, () -> camundaCollectionHelper.updateListElement(delegateTask, COLLECTION_NAME, "fake_element_key"));
    }

    @Test
    public void shouldthrowExceptionWhenLoopcounterDoesntExist() {
        variableMap.remove(LOOP_COUNTER_NAME);
        assertThrows(NullPointerException.class, () -> camundaCollectionHelper.updateListElement(delegateTask, COLLECTION_NAME, ELEMENT_NAME));
    }
}