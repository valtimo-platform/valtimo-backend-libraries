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

package com.ritense.valtimo.domain.formfields;

import org.camunda.bpm.engine.impl.form.type.AbstractFormFieldType;
import org.camunda.bpm.engine.variable.value.TypedValue;

public class TextAreaFormFieldType extends AbstractFormFieldType {

    @Override
    public String getName() {
        return "TextArea";
    }

    @Override
    public TypedValue convertToFormValue(TypedValue propertyValue) {
        return propertyValue;
    }

    @Override
    public TypedValue convertToModelValue(TypedValue propertyValue) {
        return propertyValue;
    }

    @Override
    public Object convertFormValueToModelValue(Object propertyValue) {
        return propertyValue;
    }

    @Override
    public String convertModelValueToFormValue(Object modelValue) {
        try {
            return (String) modelValue;
        } catch (ClassCastException ex) {
            return null;
        }
    }

}