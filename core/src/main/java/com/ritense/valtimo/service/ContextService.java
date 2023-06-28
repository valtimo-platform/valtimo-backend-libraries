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

package com.ritense.valtimo.service;

import com.ritense.valtimo.camunda.domain.CamundaProcessDefinition;
import com.ritense.valtimo.camunda.dto.CamundaProcessDefinitionDto;
import com.ritense.valtimo.camunda.service.CamundaRepositoryService;
import com.ritense.valtimo.context.repository.ContextRepository;
import com.ritense.valtimo.context.repository.UserContextRepository;
import com.ritense.valtimo.contract.authentication.AuthoritiesConstants;
import com.ritense.valtimo.contract.authentication.model.ValtimoUser;
import com.ritense.valtimo.contract.exception.ValtimoRuntimeException;
import com.ritense.valtimo.domain.contexts.Context;
import com.ritense.valtimo.domain.contexts.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.ritense.valtimo.camunda.repository.CamundaProcessDefinitionSpecificationHelper.byActive;
import static com.ritense.valtimo.camunda.repository.CamundaProcessDefinitionSpecificationHelper.byLatestVersion;

public class ContextService {

    private static final Logger logger = LoggerFactory.getLogger(ContextService.class);
    private final CurrentUserService currentUserService;
    private final ContextRepository contextRepository;
    private final UserContextRepository userContextRepository;
    private final CamundaRepositoryService repositoryService;

    public ContextService(
        CurrentUserService currentUserService, ContextRepository contextRepository,
        UserContextRepository userContextRepository, CamundaRepositoryService repositoryService
    ) {
        this.currentUserService = currentUserService;
        this.contextRepository = contextRepository;
        this.userContextRepository = userContextRepository;
        this.repositoryService = repositoryService;
    }

    public void setContextOfCurrentUser(Long contextId) throws IllegalAccessException {
        ValtimoUser valtimoUser = currentUserService.getCurrentUser();
        Context context = contextRepository.findFirstByRolesInAndId(valtimoUser.getRoles(), contextId);
        if (context == null) {
            throw new ValtimoRuntimeException("Cannot set context", null, "apiError", "Cannot set context");
        }

        UserContext userContext = userContextRepository.findByUserId(valtimoUser.getId());
        if (userContext != null) {
            userContextRepository.delete(userContext);
        }
        userContext = new UserContext(context.getId(), valtimoUser.getId());
        userContextRepository.save(userContext);
    }

    public Context getContextOfCurrentUser() throws IllegalAccessException, ValtimoRuntimeException {
        ValtimoUser valtimoUser = currentUserService.getCurrentUser();
        UserContext userContext = userContextRepository.findByUserId(valtimoUser.getId());
        if (userContext == null) {
            return resetUserContext(valtimoUser);
        }
        Context context = contextRepository.findFirstByRolesInAndId(valtimoUser.getRoles(), userContext.getContextId());
        if (context == null) {
            return resetUserContext(valtimoUser);
        }
        return context;
    }

    private Context resetUserContext(ValtimoUser valtimoUser) {
        //Step 1 try to find context with non-default roles ROLE_USER/ROLE_ADMIN
        List<String> nonDefaultRoles = getNonDefaultUserRoles(valtimoUser);
        Context context = null;
        if (!nonDefaultRoles.isEmpty()) {
            context = contextRepository.findFirstByRolesIn(nonDefaultRoles);
        }
        // Step 2: When context is null after non-default query use all roles found.
        if (context == null) {
            context = contextRepository.findFirstByRolesIn(valtimoUser.getRoles());
        }
        if (context == null) {
            throw new ValtimoRuntimeException(
                "Cannot set default context, no context found", null, "apiError",
                "Cannot set default context, no context found"
            );
        }
        UserContext userContext = new UserContext(context.getId(), valtimoUser.getId());
        try {
            userContextRepository.save(userContext);
        } catch (DataIntegrityViolationException exception) {
            // We know there is a race condition here, but it is not worth it to lock.
            logger.debug("User context for context {} and user {} already exists", context.getId(), valtimoUser.getId());
        }
        return context;
    }

    private List<String> getNonDefaultUserRoles(ValtimoUser valtimoUser) {
        List<String> nonDefaultRoles = new ArrayList<>();
        for (String role : valtimoUser.getRoles()) {
            if (!role.equals(AuthoritiesConstants.ADMIN) && !role.equals(AuthoritiesConstants.USER)) {
                nonDefaultRoles.add(role);
            }
        }
        return nonDefaultRoles;
    }

    @Transactional(readOnly = true)
    public List<Context> findContextsMatchingRoles() throws IllegalAccessException {
        ValtimoUser valtimoUser = currentUserService.getCurrentUser();
        return contextRepository.findDistinctByRolesIn(valtimoUser.getRoles());
    }

    @Transactional(readOnly = true)
    public List<CamundaProcessDefinitionDto> findVisibleContextProcesses() throws IllegalAccessException {
        List<CamundaProcessDefinition> deployedDefinitions = repositoryService.findAll(byActive().and(byLatestVersion()));
        Context context = getContextOfCurrentUser();
        List<CamundaProcessDefinition> contextFilteredDeployedDefinitions = new ArrayList<>();
        deployedDefinitions.forEach(p -> {
            //NOTE: this also considers the menu visibility
            if (context.containsProcessAndVisibleInMenu(p.getKey())) {
                contextFilteredDeployedDefinitions.add(p);
            }
        });
        return contextFilteredDeployedDefinitions.stream()
            .map(CamundaProcessDefinitionDto::of)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<Context> findAll(Pageable pageable) {
        return contextRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Context> findOneById(Long id) {
        return contextRepository.findById(id);
    }

    public void delete(Long id) {
        contextRepository.deleteById(id);
    }

    public Context save(Context context) {
        return contextRepository.save(context);
    }

}