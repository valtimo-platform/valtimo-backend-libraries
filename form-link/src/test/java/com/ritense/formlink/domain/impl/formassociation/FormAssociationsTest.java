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

package com.ritense.formlink.domain.impl.formassociation;

import com.ritense.formlink.domain.impl.formassociation.formlink.BpmnElementFormIdLink;
import org.junit.jupiter.api.Test;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FormAssociationsTest {
    @Test
    public void testDuplicates() {
        FormAssociations camundaFormAssociations = new FormAssociations();

        BpmnElementFormIdLink bpmnElementFormIdLink = new BpmnElementFormIdLink("form-id", UUID.randomUUID());
        UserTaskFormAssociation userTaskFormAssociation = new UserTaskFormAssociation(UUID.randomUUID(), bpmnElementFormIdLink);
        boolean add = camundaFormAssociations.add(userTaskFormAssociation);
        assertThat(add).isTrue();

        boolean secondAdd = camundaFormAssociations.add(userTaskFormAssociation);
        assertThat(secondAdd).isFalse();

        assertThat(camundaFormAssociations).hasSize(1);
    }
}