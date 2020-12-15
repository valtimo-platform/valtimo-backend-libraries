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

import com.ritense.valtimo.domain.process.ProcessDefinitionVersionChangelog;
import com.ritense.valtimo.service.ProcessDefinitionVersionChangelogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

@Deprecated(since = "4.0.5-RELEASE", forRemoval = true)
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
public class ProcessDefinitionVersionChangelogResource {

    private final ProcessDefinitionVersionChangelogService processDefinitionVersionChangelogService;

    @PostMapping(value = "/process-definition-version-changelog")
    public ResponseEntity<ProcessDefinitionVersionChangelog> createProcessDefinitionVersionChangelog(
        @Valid @RequestBody ProcessDefinitionVersionChangelog processDefinitionVersionChangelog
    ) throws URISyntaxException {
        logger.debug("REST request to save ProcessDefinitionVersionChangelog : {}", processDefinitionVersionChangelog);
        ProcessDefinitionVersionChangelog result = processDefinitionVersionChangelogService.save(processDefinitionVersionChangelog);
        return ResponseEntity.created(new URI("/api/process-definition-version-changelog/" + result.getId()))
            .body(result);
    }

    @GetMapping(value = "/process-definition-version-changelog/procdefId/{procdefId}")
    public ResponseEntity<ProcessDefinitionVersionChangelog> getProcessDefinitionVersionChangelogByProcdefId(
        @PathVariable String procdefId
    ) {
        logger.debug("REST request to get ProcessDefinitionVersionChangelog : {}", procdefId);
        ProcessDefinitionVersionChangelog processDefinitionVersionChangelog = processDefinitionVersionChangelogService.findOneByProcdefId(procdefId);
        return Optional.ofNullable(processDefinitionVersionChangelog)
            .map(result -> new ResponseEntity<>(result, HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

}