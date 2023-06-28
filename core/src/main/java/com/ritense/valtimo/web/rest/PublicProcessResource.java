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

package com.ritense.valtimo.web.rest;

import com.ritense.valtimo.camunda.domain.CamundaProcessDefinition;
import com.ritense.valtimo.camunda.domain.ProcessInstanceWithDefinition;
import com.ritense.valtimo.camunda.service.CamundaRepositoryService;
import com.ritense.valtimo.service.CamundaProcessService;
import com.ritense.valtimo.service.util.FormUtils;
import com.ritense.valtimo.web.rest.dto.StartFormDto;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.FormService;
import org.camunda.bpm.engine.form.FormField;
import org.camunda.bpm.engine.rest.dto.runtime.ProcessInstanceDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.ritense.valtimo.camunda.repository.CamundaProcessDefinitionSpecificationHelper.byKey;
import static com.ritense.valtimo.camunda.repository.CamundaProcessDefinitionSpecificationHelper.byLatestVersion;
import static com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE;

@RestController
@RequestMapping(value = "/api", produces = APPLICATION_JSON_UTF8_VALUE)
public class PublicProcessResource {

    private final FormService formService;
    private final CamundaRepositoryService repositoryService;
    private final CamundaProcessService camundaProcessService;

    public PublicProcessResource(
        final FormService formService,
        final CamundaRepositoryService repositoryService,
        final CamundaProcessService camundaProcessService
    ) {
        this.formService = formService;
        this.repositoryService = repositoryService;
        this.camundaProcessService = camundaProcessService;
    }

    @GetMapping("/v1/public/process/definition/{processDefinitionKey}/start-form")
    @ResponseBody
    public ResponseEntity<StartFormDto> getStartForm(
        HttpServletRequest request,
        @PathVariable String processDefinitionKey) {

        CamundaProcessDefinition processDefinition = repositoryService.find(
            byKey(processDefinitionKey)
                .and(byLatestVersion())
        );
        String startFormKey = formService.getStartFormKey(processDefinition.getId());

        List<FormField> startFormData = new ArrayList<>();
        String formLocation = "";

        if (StringUtils.isBlank(startFormKey)) {
            startFormData = formService.getStartFormData(processDefinition.getId()).getFormFields();
        } else {
            formLocation = FormUtils.getFormLocation(startFormKey, request);
        }
        StartFormDto startFormDto = new StartFormDto(formLocation, startFormData);
        return new ResponseEntity<>(startFormDto, HttpStatus.OK);
    }

    @PostMapping(value = "/v1/public/process/definition/{processDefinitionKey}/{businessKey}/start",
        consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<ProcessInstanceDto> startProcessInstance(
        @PathVariable String processDefinitionKey,
        @PathVariable String businessKey,
        @RequestBody Map<String, Object> variables
    ) {
        final ProcessInstanceWithDefinition processInstanceWithDefinition = camundaProcessService.startProcess(processDefinitionKey, businessKey, variables);
        return new ResponseEntity<>(processInstanceWithDefinition.getProcessInstanceDto(), HttpStatus.OK);
    }

}
