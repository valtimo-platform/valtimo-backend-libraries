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

package com.ritense.valtimo.web.rest.dto;

import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.form.FormField;
import java.util.List;

public class StartFormDto {

    private String formLocation;

    private List<FormField> formFields;

    private boolean genericForm;

    public StartFormDto(String formLocation, List<FormField> formFields) {
        this.formLocation = formLocation;
        this.formFields = formFields;
        this.genericForm = StringUtils.isBlank(formLocation);
    }

    public String getFormLocation() {
        return formLocation;
    }

    public void setFormLocation(String formLocation) {
        this.formLocation = formLocation;
    }

    public List<FormField> getFormFields() {
        return formFields;
    }

    public void setFormFields(List<FormField> formFields) {
        this.formFields = formFields;
    }

    public boolean isGenericForm() {
        return genericForm;
    }

    public void setGenericForm(boolean genericForm) {
        this.genericForm = genericForm;
    }
}
