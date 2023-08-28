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

package com.ritense.formlink.autodeployment;

import com.ritense.formlink.domain.impl.formassociation.FormAssociationType;

import java.util.Objects;

@Deprecated(since = "10.6.0", forRemoval = true)
public class FormLinkConfigItem {
    private String formName;
    private String formFlowName;
    private String formLinkElementId;
    private FormAssociationType formAssociationType;

    public FormLinkConfigItem() {
    }

    public String getFormName() {
        return this.formName;
    }

    public String getFormFlowName() {
        return this.formFlowName;
    }

    public String getFormLinkElementId() {
        return this.formLinkElementId;
    }

    public FormAssociationType getFormAssociationType() {
        return this.formAssociationType;
    }

    public void setFormName(String formName) {
        this.formName = formName;
    }

    public void setFormFlowName(String formFlowName) {
        this.formFlowName = formFlowName;
    }

    public void setFormLinkElementId(String formLinkElementId) {
        this.formLinkElementId = formLinkElementId;
    }

    public void setFormAssociationType(FormAssociationType formAssociationType) {
        this.formAssociationType = formAssociationType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FormLinkConfigItem that = (FormLinkConfigItem) o;
        return Objects.equals(getFormName(), that.getFormName()) && Objects.equals(getFormLinkElementId(), that.getFormLinkElementId()) && getFormAssociationType() == that.getFormAssociationType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFormName(), getFormLinkElementId(), getFormAssociationType());
    }

    @Override
    public String toString() {
        return "FormLinkConfigItem{" +
            "formName='" + formName + '\'' +
            ", formLinkElementId='" + formLinkElementId + '\'' +
            ", formAssociationType=" + formAssociationType +
            '}';
    }
}
