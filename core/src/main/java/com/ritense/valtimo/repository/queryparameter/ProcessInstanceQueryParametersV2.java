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
import com.ritense.valtimo.service.util.DateUtils;
import com.ritense.valtimo.web.rest.dto.processvariable.ProcessVariableDTOV2;
import com.ritense.valtimo.web.rest.dto.processvariable.type.BooleanProcessVariableDTOV2;
import com.ritense.valtimo.web.rest.dto.processvariable.type.DateProcessVariableDTOV2;
import com.ritense.valtimo.web.rest.dto.processvariable.type.EnumProcessVariableDTOV2;
import com.ritense.valtimo.web.rest.dto.processvariable.type.FileUploadProcessVariableDTOV2;
import com.ritense.valtimo.web.rest.dto.processvariable.type.LongProcessVariableDTOV2;
import com.ritense.valtimo.web.rest.dto.processvariable.type.StringProcessVariableDTOV2;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ProcessInstanceQueryParametersV2 {
    private String processDefinitionName;
    private String processDefinitionId;
    private List<ProcessVariableDTOV2> processVariables;

    public ProcessInstanceQueryParametersV2 processDefinitionName(String processDefinitionName) {
        this.processDefinitionName = processDefinitionName;
        return this;
    }

    public ProcessInstanceQueryParametersV2 processDefinitionId(String processDefinitionId) {
        this.processDefinitionId = processDefinitionId;
        return this;
    }

    public ProcessInstanceQueryParametersV2 processVariables(List<ProcessVariableDTOV2> processVariables) {
        this.processVariables = processVariables;
        return this;
    }

    public Map<String, Object> createParameters() {
        Map<String, Object> parameters = new HashMap<>();
        if (Optional.ofNullable(processDefinitionName).isPresent()) {
            parameters.put("processDefinitionName", processDefinitionName);
        }
        if (Optional.ofNullable(processDefinitionId).isPresent()) {
            parameters.put("processDefinitionId", processDefinitionId);
        }
        if (processVariables != null && processVariables.size() > 0) {
            List<ProcessVariableQueryParameterV2> processVariableQueryParameterV2s = new ArrayList<>();
            processVariables.forEach(processVariable -> createProcessVariableQueryParameter(processVariableQueryParameterV2s, processVariable));
            parameters.put("variables", processVariableQueryParameterV2s);
        }
        return parameters;
    }

    private void createProcessVariableQueryParameter(
            List<ProcessVariableQueryParameterV2> processVariableQueryParameterV2s,
            ProcessVariableDTOV2 processVariable
    ) {
        if (processVariable.getClass().equals(StringProcessVariableDTOV2.class)) {
            StringProcessVariableDTOV2 stringProcessVariable = (StringProcessVariableDTOV2) processVariable;
            processVariableQueryParameterV2s.add(
                    new StringProcessVariableQueryParameterV2(
                        processVariable.getName(),
                        stringProcessVariable.getValue().toUpperCase()
                    )
            );
        } else if (processVariable.getClass().equals(LongProcessVariableDTOV2.class)) {
            LongProcessVariableDTOV2 longProcessVariable = (LongProcessVariableDTOV2) processVariable;
            processVariableQueryParameterV2s.add(
                    new LongProcessVariableQueryParameterV2(
                        processVariable.getName(),
                        longProcessVariable.getValue()
                    )
            );
        } else if (processVariable.getClass().equals(BooleanProcessVariableDTOV2.class)) {
            BooleanProcessVariableDTOV2 booleanProcessVariable = (BooleanProcessVariableDTOV2) processVariable;
            processVariableQueryParameterV2s.add(
                    new BooleanProcessVariableQueryParameterV2(
                        processVariable.getName(),
                        booleanProcessVariable.getValue()
                    )
            );
        } else if (processVariable.getClass().equals(EnumProcessVariableDTOV2.class)) {
            EnumProcessVariableDTOV2 enumProcessVariable = (EnumProcessVariableDTOV2) processVariable;
            processVariableQueryParameterV2s.add(
                    new EnumProcessVariableQueryParameterV2(
                        processVariable.getName(),
                        enumProcessVariable.getValues()
                    )
            );
        } else if (processVariable.getClass().equals(DateProcessVariableDTOV2.class)) {
            DateProcessVariableDTOV2 dateProcessVariable = (DateProcessVariableDTOV2) processVariable;
            Long to = null;
            Long from = null;
            if (dateProcessVariable.getDateRange().getFrom() != null) {
                from = DateUtils.toEpochMilliSeconds(dateProcessVariable.getDateRange().getFrom());
            }
            if (dateProcessVariable.getDateRange().getTo() != null) {
                to = DateUtils.toEpochMilliSeconds(dateProcessVariable.getDateRange().getTo());
            }

            processVariableQueryParameterV2s.add(
                    new DateProcessVariableQueryParameterV2(
                        processVariable.getName(),
                        from,
                        to
                    )
            );
        } else if (processVariable.getClass().equals(FileUploadProcessVariableDTOV2.class)) {
            FileUploadProcessVariableDTOV2 fileUploadProcessVariable = (FileUploadProcessVariableDTOV2) processVariable;
            processVariableQueryParameterV2s.add(
                    new FileUploadProcessVariableQueryParameterV2(
                        processVariable.getName(),
                        fileUploadProcessVariable.getValue()
                    )
            );
        }
    }
}