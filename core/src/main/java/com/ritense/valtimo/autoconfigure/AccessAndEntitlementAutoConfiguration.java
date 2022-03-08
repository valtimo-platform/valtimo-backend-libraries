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

package com.ritense.valtimo.autoconfigure;

import com.ritense.valtimo.accessandentitlement.domain.listener.AuthorityDeletedEventListener;
import com.ritense.valtimo.accessandentitlement.repository.AuthorityRepository;
import com.ritense.valtimo.accessandentitlement.service.AuthorityService;
import com.ritense.valtimo.accessandentitlement.service.impl.AuthorityServiceImpl;
import com.ritense.valtimo.accessandentitlement.web.rest.AuthorityResource;
import com.ritense.valtimo.service.AuthorizedUsersService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackageClasses = AuthorityRepository.class)
@EntityScan("com.ritense.valtimo.accessandentitlement.domain")
public class AccessAndEntitlementAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(AuthorityDeletedEventListener.class)
    public AuthorityDeletedEventListener authorityDeletedEventListener(
        final ApplicationEventPublisher applicationEventPublisher
    ) {
        return new AuthorityDeletedEventListener(applicationEventPublisher);
    }

    @Bean
    @ConditionalOnMissingBean(AuthorityService.class)
    public AuthorityService authorityService(
        final AuthorityRepository authorityRepository,
        final AuthorizedUsersService authorizedUsersService
    ) {
        return new AuthorityServiceImpl(authorityRepository, authorizedUsersService);
    }

    @Bean
    @ConditionalOnMissingBean(AuthorityResource.class)
    public AuthorityResource authorityResource(
        final AuthorityService authorityService
    ) {
        return new AuthorityResource(authorityService);
    }

}
