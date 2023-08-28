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

import com.ritense.valtimo.contract.utils.SecurityUtils;
import com.ritense.valtimo.domain.contexts.Context;
import com.ritense.valtimo.domain.contexts.ContextProcess;
import com.ritense.valtimo.service.CamundaProcessService;
import com.ritense.valtimo.service.ContextService;
import com.ritense.valtimo.web.rest.dto.UserContextDTO;
import com.ritense.valtimo.web.rest.util.HeaderUtil;
import com.ritense.valtimo.web.rest.util.PaginationUtil;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.rest.dto.repository.ProcessDefinitionDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE;

@RestController
@RequestMapping(value = "/api", produces = APPLICATION_JSON_UTF8_VALUE)
public class ContextResource {

    private final ContextService contextService;
    private final CamundaProcessService camundaProcessService;

    public ContextResource(
        final ContextService contextService,
        final CamundaProcessService camundaProcessService
    ) {
        this.contextService = contextService;
        this.camundaProcessService = camundaProcessService;
    }

    @GetMapping("/v1/user/context/processes")
    public ResponseEntity<List<ProcessDefinitionDto>> getContextProcess() throws IllegalAccessException {
        return ResponseEntity.ok(contextService.findVisibleContextProcesses());
    }

    @GetMapping("/v1/user/contexts")
    public ResponseEntity<List<Context>> getContextsMatchingRoles() throws IllegalAccessException {
        final List<Context> contexts = contextService.findContextsMatchingRoles();
        return ResponseEntity.ok(contexts);
    }

    @GetMapping("/v1/contexts")
    public ResponseEntity<List<Context>> getContexts(Pageable pageable) {
        final Page<Context> page = contextService.findAll(pageable);
        final HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/v1/contexts");
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/v1/user/context")
    public ResponseEntity<Context> getUserContext() throws IllegalAccessException {
        return ResponseEntity.ok(contextService.getContextOfCurrentUser());
    }

    @PostMapping("/v1/user/context")
    public ResponseEntity<Void> setUserContext(@Valid @RequestBody UserContextDTO userContextDTO) throws IllegalAccessException {
        contextService.setContextOfCurrentUser(userContextDTO.getContextId());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/v1/contexts")
    public ResponseEntity<Context> updateContext(@Valid @RequestBody Context context) throws URISyntaxException {
        if (context.getId() == null) {
            return createContext(context);
        }
        Context result = contextService.save(context);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert("context", context.getName()))
            .body(result);
    }

    @PostMapping("/v1/contexts")
    public ResponseEntity<Context> createContext(@Valid @RequestBody Context context) throws URISyntaxException {
        if (context.getId() != null) {
            return ResponseEntity.badRequest()
                .headers(HeaderUtil.createFailureAlert("context", "idexists", "A new context cannot already have an ID"))
                .body(null);
        }
        Context result = contextService.save(context);
        return ResponseEntity.created(new URI("/api/v1/contexts/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert("context", result.getName()))
            .body(result);
    }

    @DeleteMapping("/v1/contexts/{id}")
    public ResponseEntity<Void> deleteChoiceField(@PathVariable Long id) {
        contextService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert("context", id.toString())).build();
    }

    @GetMapping("/v1/contexts/{id}")
    public ResponseEntity<Context> getContext(@PathVariable Long id) {
        Optional<Context> context = contextService.findOneById(id);
        return context
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/v1/context/process/user/active")
    public ResponseEntity<List<HistoricProcessInstance>> getAllActiveContextProcessesStartedByCurrentUser() throws IllegalAccessException {
        final Set<String> processes =
            contextService
                .getContextOfCurrentUser()
                .getProcesses()
                .stream()
                .map(ContextProcess::getProcessDefinitionKey)
                .collect(Collectors.toSet());

        final List<HistoricProcessInstance> processInstances =
            camundaProcessService.getAllActiveContextProcessesStartedByCurrentUser(processes, SecurityUtils.getCurrentUserLogin());
        return ResponseEntity.ok(processInstances);
    }

}
