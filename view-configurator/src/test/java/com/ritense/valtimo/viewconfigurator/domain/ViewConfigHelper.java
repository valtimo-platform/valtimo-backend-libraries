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

import com.ritense.valtimo.viewconfigurator.domain.type.StringVariableType;
import org.hamcrest.Matcher;
import org.hamcrest.core.IsEqual;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.CombinableMatcher.both;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;

abstract class ViewConfigHelper {

    protected static final Long P_ID = 1L;
    protected static final String REFERENCE_ID = "reference-id";
    protected static final String LABEL = "label";

    protected static final Long P_ID_2 = 2L;
    protected static final String REFERENCE_ID_2 = "reference-id-2";
    protected static final String LABEL_2 = "label-2";

    protected static final Long P_ID_3 = 3L;
    protected static final String REFERENCE_ID_3 = "reference-id-3";
    protected static final String LABEL_3 = "label-3";

    protected static final Long P_ID_4 = 4L;
    protected static final String REFERENCE_ID_4 = "reference-id-4";
    protected static final String LABEL_4 = "label-4";

    protected static final Long P_ID_NEW = 5L;
    protected static final String REFERENCE_ID_NEW = "reference-id-5";
    protected static final String LABEL_NEW = "label-5";


    protected void assertViewVarsHaveCorrectSequence(View view) {
        int i = 1;
        for (ViewVar viewVar : view.getSelectedProcessDefinitionVariables()) {
            assertEquals(i, viewVar.getSequence().intValue());
            i++;
        }
    }

    protected Matcher<Object> withSequence(Integer sequence) {
        return hasProperty("sequence", IsEqual.equalTo(sequence));
    }

    protected Matcher<Object> withReferenceId(String referenceId) {
        return hasProperty("var", hasProperty("referenceId", IsEqual.equalTo(referenceId)));
    }

    protected Matcher<Object> withLabel(String label) {
        return hasProperty("var", hasProperty("label", IsEqual.equalTo(label)));
    }

    protected Matcher withId(Long id) {
        return hasProperty("var", hasProperty("id", IsEqual.equalTo(id)));
    }

    protected Matcher<Object> processDefinitionVariableWithLabel(String label) {
        return hasProperty("label", IsEqual.equalTo(label));
    }

    protected Matcher<Object> processDefinitionVariableWithReferenceId(String referenceId) {
        return hasProperty("referenceId", IsEqual.equalTo(referenceId));
    }

    protected Matcher<Object> hasVariables(Matcher<Object>... matchers) {
        return hasProperty("selectedProcessDefinitionVariables", allOf(matchers));
    }

    protected Matcher<Object> withGroupLabel(String label) {
        return hasProperty("label", IsEqual.equalTo(label));
    }

    protected Matcher<Object> hasVariable1WithSequence(int sequence) {
        return hasItem(
            both(withId(P_ID))
                .and(withLabel(LABEL))
                .and(withReferenceId(REFERENCE_ID))
                .and(withSequence(sequence)));
    }

    protected Matcher<Object> hasVariable2WithSequence(int sequence) {
        return hasItem(
            both(withId(P_ID_2))
                .and(withLabel(LABEL_2))
                .and(withReferenceId(REFERENCE_ID_2))
                .and(withSequence(sequence)));
    }

    protected Matcher<Object> hasVariable3WithSequence(int sequence) {
        return hasItem(
            both(withId(P_ID_3))
                .and(withLabel(LABEL_3))
                .and(withReferenceId(REFERENCE_ID_3))
                .and(withSequence(sequence)));
    }

    protected Matcher<Object> hasVariable4WithSequence(int sequence) {
        return hasItem(
            both(withId(P_ID_4))
                .and(withLabel(LABEL_4))
                .and(withReferenceId(REFERENCE_ID_4))
                .and(withSequence(sequence)));
    }

    protected ViewConfig viewConfigInitialised() {
        final String processDefinitionId = processDefinitionId();
        final Set<ProcessDefinitionVariable> variables = processDefinitionVariables();

        ViewConfig viewConfig = ViewConfig.initialise(processDefinitionId, variables);
        long i = 1L;
        for (View view : viewConfig.getViews()) {
            view.setId(i);
            i++;
        }
        return viewConfig;
    }

    protected Set<ProcessDefinitionVariable> processDefinitionVariables() {
        Set<ProcessDefinitionVariable> variables = new LinkedHashSet<>();

        ProcessDefinitionVariable variable = new StringVariableType(REFERENCE_ID, LABEL);
        variable.setId(P_ID);
        variables.add(variable);

        ProcessDefinitionVariable variable2 = new StringVariableType(REFERENCE_ID_2, LABEL_2);
        variable2.setId(P_ID_2);
        variables.add(variable2);

        ProcessDefinitionVariable variable3 = new StringVariableType(REFERENCE_ID_3, LABEL_3);
        variable3.setId(P_ID_3);
        variables.add(variable3);

        ProcessDefinitionVariable variable4 = new StringVariableType(REFERENCE_ID_4, LABEL_4);
        variable4.setId(P_ID_4);
        variables.add(variable4);


        return variables;
    }

    protected String processDefinitionId() {
        return UUID.randomUUID().toString();
    }

    protected LinkedHashSet<ProcessDefinitionVariable> defaultProcessDefinitionVariables() {
        return DefaultProcessVarFactory.getDefaultProcessVariables();
    }

    protected ViewVarGroup defaultProcessDefinitionVariableGroup(LinkedHashSet<ProcessDefinitionVariable> processVariable) {
        return DefaultProcessVarFactory.getDefaultProcessVariablesGroup(processVariable);
    }

}
