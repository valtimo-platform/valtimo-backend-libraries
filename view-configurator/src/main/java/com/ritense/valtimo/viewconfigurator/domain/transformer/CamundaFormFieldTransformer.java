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

import com.ritense.valtimo.viewconfigurator.domain.EnumVariableTypeItem;
import com.ritense.valtimo.viewconfigurator.domain.ProcessDefinitionVariable;
import com.ritense.valtimo.viewconfigurator.domain.type.BooleanVariableType;
import com.ritense.valtimo.viewconfigurator.domain.type.DateVariableType;
import com.ritense.valtimo.viewconfigurator.domain.type.EnumVariableType;
import com.ritense.valtimo.viewconfigurator.domain.type.FileUploadVariableType;
import com.ritense.valtimo.viewconfigurator.domain.type.LongVariableType;
import com.ritense.valtimo.viewconfigurator.domain.type.StringVariableType;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaFormField;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CamundaFormFieldTransformer {

    public static final Function<CamundaFormField, Optional<ProcessDefinitionVariable>> transform = camundaFormField -> {
        final String type = camundaFormField.getCamundaType();
        final String referenceId = camundaFormField.getCamundaId();
        final String label = camundaFormField.getCamundaLabel() != null ? camundaFormField.getCamundaLabel() : camundaFormField.getCamundaId();

        switch (type) {
            case "string":
                return Optional.of(new StringVariableType(referenceId, label));
            case "boolean":
                return Optional.of(new BooleanVariableType(referenceId, label));
            case "date":
                return Optional.of(new DateVariableType(referenceId, label));
            case "enum":
                Set<EnumVariableTypeItem> enumVariableTypeItems = camundaFormField
                    .getCamundaValues()
                    .stream()
                    .map(camundaValue -> new EnumVariableTypeItem(camundaValue.getCamundaId(), camundaValue.getCamundaName()))
                    .collect(Collectors.toSet());
                return Optional.of(new EnumVariableType(referenceId, label, enumVariableTypeItems));
            case "long":
                return Optional.of(new LongVariableType(referenceId, label));
            case "FileUpload":
                return Optional.of(new FileUploadVariableType(referenceId, label));
            case "TextArea":
                return Optional.empty();
            case "ChoiceField":
                return Optional.empty();
            default:
                throw new IllegalStateException("Cannot map camundaFormField type of " + camundaFormField.getCamundaType());
        }
    };

}
