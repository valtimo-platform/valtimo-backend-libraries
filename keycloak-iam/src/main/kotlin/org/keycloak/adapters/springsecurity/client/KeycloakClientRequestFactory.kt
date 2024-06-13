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

package org.keycloak.adapters.springsecurity.client

/**
 * This is a workaround for the fact that the Keycloak adapters are not supported for Spring Boot 3, which we will have to fix.
 * More info here: https://stackoverflow.com/questions/74571191/use-keycloak-spring-adapter-with-spring-boot-3
 *
 * This class override only exists because there is no sure way to prevent Spring Boot from creating a bean via component scanning.
 * Preventing this scan would only be possible if we have control over the @SpringBootApplication or other configurations enabling @ComponentScan.
 * However, we cannot enforce that from the libraries.
 *
 * TLDR; Component scanning is bad.
 */
class KeycloakClientRequestFactory