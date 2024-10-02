/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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

package com.ritense.form.service.impl;

import com.ritense.form.domain.FormDefinition;
import com.ritense.form.domain.FormIoFormDefinition;
import com.ritense.form.domain.request.CreateFormDefinitionRequest;
import com.ritense.form.domain.request.ModifyFormDefinitionRequest;
import com.ritense.form.repository.FormDefinitionRepository;
import com.ritense.form.service.FormDefinitionService;
import com.ritense.form.web.rest.dto.FormOption;
import com.ritense.logging.LoggableResource;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import static com.ritense.logging.LoggingContextKt.withLoggingContext;

public class FormIoFormDefinitionService implements FormDefinitionService {

    private final FormDefinitionRepository formDefinitionRepository;

    public FormIoFormDefinitionService(final FormDefinitionRepository formDefinitionRepository) {
        this.formDefinitionRepository = formDefinitionRepository;
    }

    @Override
    public Page<FormIoFormDefinition> getAll(Pageable pageable) {
        return formDefinitionRepository.findAll(pageable);
    }

    @Override
    public List<FormOption> getAllFormOptions() {
        return formDefinitionRepository.findAllByOrderByNameAsc()
            .stream()
            .map(formIoFormDefinition -> new FormOption(formIoFormDefinition.getId(), formIoFormDefinition.getName()))
            .toList();
    }

    @Override
    public Page<? extends FormDefinition> queryFormDefinitions(String searchTerm, Pageable pageable) {
        return formDefinitionRepository.findAllByNameContainingIgnoreCase(searchTerm, pageable);
    }

    @Override
    public Optional<FormIoFormDefinition> getFormDefinitionById(
        @LoggableResource(resourceType = FormIoFormDefinition.class) UUID formDefinitionId
    ) {
        return formDefinitionRepository.findById(formDefinitionId);
    }

    @Override
    public Optional<FormIoFormDefinition> getFormDefinitionByName(
        @LoggableResource("formDefinitionName") String name
    ) {
        return formDefinitionRepository.findByName(name);
    }

    @Override
    public Optional<FormIoFormDefinition> getFormDefinitionByNameIgnoringCase(
        @LoggableResource("formDefinitionName") String name
    ) {
        return formDefinitionRepository.findByNameIgnoreCase(name);
    }

    @Override
    @Transactional
    public FormIoFormDefinition createFormDefinition(CreateFormDefinitionRequest request) {
        return withLoggingContext("formDefinitionName", request.getName(), () -> {
            if (formDefinitionRepository.findByName(request.getName()).isPresent()) {
                throw new IllegalArgumentException("Duplicate name for new form: " + request.getName());
            }
            return formDefinitionRepository.save(
                new FormIoFormDefinition(
                    UUID.randomUUID(),
                    request.getName(),
                    request.getFormDefinition(),
                    request.isReadOnly()
                )
            );
        });
    }

    @Override
    @Transactional
    public FormIoFormDefinition modifyFormDefinition(ModifyFormDefinitionRequest request) {
        return withLoggingContext("formDefinitionName", request.getName(), () -> {
            if (!formDefinitionRepository.existsById(request.getId())) {
                throw new RuntimeException("Form definition not found with id " + request.getId().toString());
            }
            return formDefinitionRepository
                .findById(request.getId())
                .map(formIoFormDefinition -> {
                    formIoFormDefinition.changeName(request.getName());
                    formIoFormDefinition.changeDefinition(request.getFormDefinition());
                    return formDefinitionRepository.save(formIoFormDefinition);
                }).orElseThrow();
        });
    }

    @Override
    @Transactional
    public FormIoFormDefinition modifyFormDefinition(
        @LoggableResource(resourceType = FormIoFormDefinition.class) UUID id,
        String name,
        String definition,
        Boolean readOnly
    ) {
        return formDefinitionRepository
            .findById(id)
            .map(
                formIoFormDefinition -> {
                    formIoFormDefinition.isWriting();
                    formIoFormDefinition.changeName(name);
                    formIoFormDefinition.changeDefinition(definition);
                    formIoFormDefinition.setReadOnly(readOnly);
                    formIoFormDefinition.doneWriting();
                    return formDefinitionRepository.save(formIoFormDefinition);
                }
            )
            .orElseThrow();
    }

    @Override
    @Transactional
    public void deleteFormDefinition(
        @LoggableResource(resourceType = FormIoFormDefinition.class) UUID formDefinitionId
    ) {
        formDefinitionRepository.deleteById(formDefinitionId);
    }

    @Override
    public boolean formDefinitionExistsById(
        @LoggableResource(resourceType = FormIoFormDefinition.class) UUID id
    ) {
        return formDefinitionRepository.existsById(id);
    }

    @Override
    public Long countAllForms() {
        return formDefinitionRepository.count();

    }

}