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

package com.ritense.valtimo.accessandentitlement.service.impl;

import com.ritense.valtimo.accessandentitlement.domain.Authority;
import com.ritense.valtimo.accessandentitlement.domain.AuthorityRequest;
import com.ritense.valtimo.accessandentitlement.repository.AuthorityRepository;
import com.ritense.valtimo.accessandentitlement.service.AuthorityService;
import com.ritense.valtimo.contract.authentication.AuthorizedUsersService;
import com.ritense.valtimo.web.rest.error.EntityException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public class AuthorityServiceImpl implements AuthorityService {

    private static final String ENTITY_NAME = "authority";
    private final AuthorityRepository authorityRepository;
    private final AuthorizedUsersService authorizedUsersService;

    public AuthorityServiceImpl(AuthorityRepository authorityRepository, AuthorizedUsersService authorizedUsersService) {
        this.authorityRepository = authorityRepository;
        this.authorizedUsersService = authorizedUsersService;
    }

    @Override
    public Page<Authority> findAll(Pageable pageable) {
        return authorityRepository.findAll(pageable);
    }

    @Override
    public Optional<Authority> findBy(String name) {
        return findByAuthorityName(name);
    }

    @Override
    public Authority createAuthority(AuthorityRequest authorityRequest) {
        Optional<Authority> authorityOptional = findByAuthorityName(authorityRequest.getName());
        if (authorityOptional.isPresent()) {
            throw new EntityException("This authority already exists", "alreadyexists", ENTITY_NAME);
        }
        Authority authority = new Authority(authorityRequest.getName(), false);
        authorityRepository.save(authority);
        return authority;
    }

    @Override
    public Authority updateAuthority(AuthorityRequest authorityRequest) {
        Authority authority = getAuthorityOrThrowEntityException(authorityRequest.getName());
        authority.changeName(authorityRequest.getName());
        return authority;
    }

    @Override
    public Authority deleteAuthority(String name) throws IllegalAccessException {
        final Authority authority = getAuthorityOrThrowEntityException(name);
        if (authority.getSystemAuthority()) {
            throw new EntityException("Authority is a system authority", "issystemauthoritiy", ENTITY_NAME);
        }
        if (authorizedUsersService.isRoleInUse(authority.getName())) {
            throw new EntityException("Authority is in use", "beingused", ENTITY_NAME);
        }
        authorityRepository.delete(authority);
        return authority;
    }

    private Optional<Authority> findByAuthorityName(String name) {
        return authorityRepository.findById(name);
    }

    private Authority getAuthorityOrThrowEntityException(String name) {
        Optional<Authority> foundAuthority = findByAuthorityName(name);
        return foundAuthority.orElseThrow(() -> new EntityException("This authority doesn't exist", "notexists", ENTITY_NAME));
    }

}