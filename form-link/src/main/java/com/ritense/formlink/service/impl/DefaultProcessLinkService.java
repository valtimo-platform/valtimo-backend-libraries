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

package com.ritense.formlink.service.impl;

import com.ritense.formlink.domain.FormAssociation;
import com.ritense.formlink.domain.FormLink;
import com.ritense.formlink.domain.FormLinkTaskProvider;
import com.ritense.formlink.domain.TaskOpenResult;
import com.ritense.formlink.service.FormAssociationService;
import com.ritense.formlink.service.ProcessLinkService;
import com.ritense.valtimo.camunda.domain.CamundaTask;
import com.ritense.valtimo.camunda.service.CamundaRepositoryService;
import com.ritense.valtimo.service.CamundaTaskService;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static com.ritense.valtimo.camunda.repository.CamundaTaskSpecificationHelper.byActive;
import static com.ritense.valtimo.camunda.repository.CamundaTaskSpecificationHelper.byId;
import static org.springframework.transaction.annotation.Isolation.SERIALIZABLE;

@Deprecated(since = "10.6.0", forRemoval = true)
public class DefaultProcessLinkService implements ProcessLinkService {
    private final CamundaRepositoryService repositoryService;
    private final CamundaTaskService taskService;
    private final FormAssociationService formAssociationService;
    private final List<FormLinkTaskProvider> formLinkTaskProviders;

    public DefaultProcessLinkService(
        CamundaRepositoryService repositoryService,
        CamundaTaskService taskService,
        FormAssociationService formAssociationService,
        List<FormLinkTaskProvider> formLinkTaskProviders
    ) {
        this.repositoryService = repositoryService;
        this.taskService = taskService;
        this.formAssociationService = formAssociationService;
        this.formLinkTaskProviders = formLinkTaskProviders;
    }

    @Override
    @Transactional(isolation = SERIALIZABLE)
    public TaskOpenResult openTask(UUID taskId) {
        CamundaTask task = taskService.findTask(
            byId(taskId.toString())
                .and(byActive())
        );

        final var processDefinition = repositoryService.findProcessDefinitionById(task.getProcessDefinitionId());

        Optional<? extends FormAssociation> formAssociationOptional = formAssociationService.getFormAssociationByFormLinkId(
            processDefinition.getKey(),
            task.getTaskDefinitionKey()
        );

        FormAssociation formAssociation = formAssociationOptional
            .orElseThrow(() -> new NoSuchElementException("Could not find FormAssociation for task " + taskId));

        FormLink formLink = formAssociation.getFormLink();
        return formLinkTaskProviders.stream()
            .filter(formLinkTaskProvider -> formLinkTaskProvider.supports(formLink))
            .map(formLinkTaskProvider -> formLinkTaskProvider.getTaskResult(task, formLink))
            .findFirst()
            .orElseThrow(() -> new NoSuchElementException("Could not find ProcessLinkTaskProvider for FormLink related to task " + taskId));
    }
}
