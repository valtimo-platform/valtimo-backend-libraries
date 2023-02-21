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

package com.ritense.valtimo.accessandentitlement.service;

import com.ritense.valtimo.accessandentitlement.domain.Authority;
import com.ritense.valtimo.accessandentitlement.domain.AuthorityRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;

public interface AuthorityService {

    Page<Authority> findAll(Pageable pageable);

    Optional<Authority> findBy(String name);

    Authority createAuthority(AuthorityRequest authorityRequest);

    Authority updateAuthority(AuthorityRequest authorityRequest);

    Authority deleteAuthority(String name) throws IllegalAccessException;

}