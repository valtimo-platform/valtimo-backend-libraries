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

package com.valtimo.keycloak.security.config

import org.springframework.security.test.context.support.WithSecurityContext
import java.lang.annotation.Inherited

@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.CLASS
)
@Retention(
    AnnotationRetention.RUNTIME
)
@Inherited
@MustBeDocumented
@WithSecurityContext(factory = WithMockKeycloakUserSecurityContextFactory::class)
annotation class WithMockKeycloakUser(

    val value: String = "",

    val email: String = "",

    val roles: Array<String> = ["ROLE_USER"],

    val clientId: String = "",

    val clientRoles: Array<String> = [],
)