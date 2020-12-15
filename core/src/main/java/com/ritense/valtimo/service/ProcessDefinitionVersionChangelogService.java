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

package com.ritense.valtimo.service;

import com.ritense.valtimo.domain.process.ProcessDefinitionVersionChangelog;
import com.ritense.valtimo.repository.ProcessDefinitionVersionChangelogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Deprecated
@Transactional
@Slf4j
@RequiredArgsConstructor
public class ProcessDefinitionVersionChangelogService {

    private final ProcessDefinitionVersionChangelogRepository processDefinitionVersionChangelogRepository;

    public ProcessDefinitionVersionChangelog save(ProcessDefinitionVersionChangelog processDefinitionVersionChangelog) {
        logger.debug("Request to save ProcessDefinitionVersionChangelog : {}", processDefinitionVersionChangelog);
        return processDefinitionVersionChangelogRepository.save(processDefinitionVersionChangelog);
    }

    @Transactional(readOnly = true)
    public ProcessDefinitionVersionChangelog findOneByProcdefId(String procdefId) {
        logger.debug("Request to get ProcessDefinitionVersionChangelog : {}", procdefId);
        return processDefinitionVersionChangelogRepository.findByProcdefId(procdefId);
    }

}
