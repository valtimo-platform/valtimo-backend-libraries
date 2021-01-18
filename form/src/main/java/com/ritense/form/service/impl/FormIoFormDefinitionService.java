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

package com.ritense.form.service.impl;

import com.ritense.form.domain.FormIoFormDefinition;
import com.ritense.form.domain.request.CreateFormDefinitionRequest;
import com.ritense.form.domain.request.ModifyFormDefinitionRequest;
import com.ritense.form.repository.FormDefinitionRepository;
import com.ritense.form.service.FormDefinitionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import javax.transaction.Transactional;
import java.util.Optional;
import java.util.UUID;

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
    public Optional<FormIoFormDefinition> getFormDefinitionById(UUID formDefinitionId) {
        return formDefinitionRepository.findById(formDefinitionId);
    }

    @Override
    public Optional<FormIoFormDefinition> getFormDefinitionByName(String name) {
        return formDefinitionRepository.findByName(name);
    }

    @Override
    @Transactional
    public FormIoFormDefinition createFormDefinition(CreateFormDefinitionRequest request) {
        return formDefinitionRepository.save(
            new FormIoFormDefinition(
                UUID.randomUUID(),
                request.getName(),
                request.getFormDefinition(),
                request.isReadOnly()
            )
        );
    }

    @Override
    @Transactional
    public FormIoFormDefinition modifyFormDefinition(ModifyFormDefinitionRequest request) {
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
    }

    @Override
    @Transactional
    public FormIoFormDefinition modifyFormDefinition(UUID id, String name, String definition, Boolean readOnly) {
       return formDefinitionRepository.findById(id)
        .map(formIoFormDefinition -> {
            formIoFormDefinition.isWriting();
            formIoFormDefinition.changeName(name);
            formIoFormDefinition.changeDefinition(definition);
            formIoFormDefinition.setReadOnly(readOnly);
            formIoFormDefinition.doneWriting();
            return formDefinitionRepository.save(formIoFormDefinition);
        }).orElseThrow();
    }

    @Override
    @Transactional
    public void deleteFormDefinition(UUID formDefinitionId) {
        formDefinitionRepository.deleteById(formDefinitionId);
    }

    @Override
    public boolean formDefinitionExistsById(UUID id) {
        return formDefinitionRepository.existsById(id);
    }

}