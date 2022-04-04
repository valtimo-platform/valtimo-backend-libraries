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

package com.ritense.valtimo.web.rest;

import com.ritense.valtimo.service.CamundaProcessService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
public class ProcessInstanceResource {

    private final CamundaProcessService camundaProcessService;

    public ProcessInstanceResource(CamundaProcessService camundaProcessService) {
        this.camundaProcessService = camundaProcessService;
    }

    @PostMapping(value = "/process-instance/{id}/variables")
    public ResponseEntity<Map<String, Object>> getProcessInstanceVariables(
        @PathVariable String id,
        @RequestBody List<String> variableNames
    ) {
        final Map<String, Object> processVariables = camundaProcessService.getProcessInstanceVariables(id, variableNames);
        return ResponseEntity.ok(processVariables);
    }

}