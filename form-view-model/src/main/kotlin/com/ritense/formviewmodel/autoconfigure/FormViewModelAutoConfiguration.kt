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

package com.ritense.formviewmodel.autoconfigure;

import com.ritense.form.service.impl.FormIoFormDefinitionService
import com.ritense.formviewmodel.FormViewModelProcessLinkActivityHandler
import com.ritense.formviewmodel.domain.ViewModelLoader
import com.ritense.formviewmodel.domain.factory.ViewModelLoaderFactory
import com.ritense.formviewmodel.security.config.FormViewModelHttpSecurityConfigurerKotlin
import com.ritense.formviewmodel.web.rest.FormViewModelResource
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.core.annotation.Order

@AutoConfiguration
class FormViewModelAutoConfiguration {

    @Order(390)
    @Bean
    fun formViewModelHttpSecurityConfigurerKotlin() = FormViewModelHttpSecurityConfigurerKotlin();

    @Bean
    fun formViewModelRestResource(
        viewModelLoaderFactory: ViewModelLoaderFactory
    ) = FormViewModelResource(
        viewModelLoaderFactory
    );

    @Bean
    fun formViewModelProcessLinkTaskProvider(
        formDefinitionService: FormIoFormDefinitionService
    ): FormViewModelProcessLinkActivityHandler {
        return FormViewModelProcessLinkActivityHandler(formDefinitionService)
    }

    @Bean
    fun viewModelLoaderFactory(
        loaders: List<ViewModelLoader<*>>
    ) = ViewModelLoaderFactory(
        loaders
    )
}