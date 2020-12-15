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

package com.ritense.valtimo.repository.queryparameter;

import com.ritense.valtimo.repository.queryparameter.type.BooleanProcessVariableQueryParameterV2;
import com.ritense.valtimo.repository.queryparameter.type.DateProcessVariableQueryParameterV2;
import com.ritense.valtimo.repository.queryparameter.type.EnumProcessVariableQueryParameterV2;
import com.ritense.valtimo.repository.queryparameter.type.FileUploadProcessVariableQueryParameterV2;
import com.ritense.valtimo.repository.queryparameter.type.LongProcessVariableQueryParameterV2;
import com.ritense.valtimo.repository.queryparameter.type.StringProcessVariableQueryParameterV2;

import java.util.Objects;

import static com.ritense.valtimo.contract.viewconfigurator.ProcessVariablesConstants.ACTIVE_REF;
import static com.ritense.valtimo.contract.viewconfigurator.ProcessVariablesConstants.BUSINESS_KEY_REF;
import static com.ritense.valtimo.contract.viewconfigurator.ProcessVariablesConstants.PROCESS_ENDED_REF;
import static com.ritense.valtimo.contract.viewconfigurator.ProcessVariablesConstants.PROCESS_STARTED_REF;
import static com.ritense.valtimo.contract.viewconfigurator.ProcessVariablesConstants.START_PROCESS_USER_REF;

public abstract class ProcessVariableQueryParameterV2 implements IQueryParameter {
    public String name;

    public ProcessVariableQueryParameterV2(String name) {
        Objects.requireNonNull(name, "name cannot be null");
        this.name = name;
    }

    public boolean isVariableString() {
        return isStringType() && !isBusinessKey() && !isStartUser();
    }

    public boolean isVariableBoolean() {
        return isBooleanType() && !isActive();
    }

    public boolean isVariableLong() {
        return isLongType();
    }

    public boolean isVariableEnum() {
        return isEnumType();
    }

    public boolean isVariableDate() {
        return isDateType();
    }

    public boolean isVariableFileUpload() {
        return isFileUploadType();
    }

    private boolean isStringType() {
        return this.getClass().equals(StringProcessVariableQueryParameterV2.class);
    }

    private boolean isBooleanType() {
        return this.getClass().equals(BooleanProcessVariableQueryParameterV2.class);
    }

    private boolean isLongType() {
        return this.getClass().equals(LongProcessVariableQueryParameterV2.class);
    }

    private boolean isEnumType() {
        return this.getClass().equals(EnumProcessVariableQueryParameterV2.class);
    }

    private boolean isDateType() {
        return this.getClass().equals(DateProcessVariableQueryParameterV2.class);
    }

    private boolean isFileUploadType() {
        return this.getClass().equals(FileUploadProcessVariableQueryParameterV2.class);
    }

    public boolean isBusinessKey() {
        return this.name.equals(BUSINESS_KEY_REF);
    }

    public boolean isProcessStarted() {
        return this.name.equals(PROCESS_STARTED_REF);
    }

    public boolean isProcessEnded() {
        return this.name.equals(PROCESS_ENDED_REF);
    }

    public boolean isActive() {
        return this.name.equals(ACTIVE_REF);
    }

    public boolean isStartUser() {
        return this.name.equals(START_PROCESS_USER_REF);
    }

}