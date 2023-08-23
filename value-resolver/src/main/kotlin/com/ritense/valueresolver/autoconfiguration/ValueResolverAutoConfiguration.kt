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

package com.ritense.valueresolver.autoconfiguration

import com.ritense.valueresolver.FixedValueResolverFactory
import com.ritense.valueresolver.ProcessVariableValueResolverFactory
import com.ritense.valueresolver.ValueResolverFactory
import com.ritense.valueresolver.ValueResolverService
import com.ritense.valueresolver.ValueResolverServiceImpl
import org.camunda.bpm.engine.RuntimeService
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy

@Configuration
class ValueResolverAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(ValueResolverService::class)
    fun valueResolverService(
        @Lazy valueResolverFactories: List<ValueResolverFactory>
    ): ValueResolverService {
        return ValueResolverServiceImpl(valueResolverFactories)
    }

    @Bean
    @ConditionalOnMissingBean(name = ["fixedValueResolver"])
    fun fixedValueResolver(): ValueResolverFactory {
        return FixedValueResolverFactory()
    }

    @Bean
    @ConditionalOnMissingBean(name = ["httpValueResolver"])
    fun httpValueResolver(): ValueResolverFactory {
        return FixedValueResolverFactory("http")
    }

    @Bean
    @ConditionalOnMissingBean(name = ["httpsValueResolver"])
    fun httpsValueResolver(): ValueResolverFactory {
        return FixedValueResolverFactory("https")
    }

    @Bean
    @ConditionalOnBean(RuntimeService::class)
    @ConditionalOnMissingBean(ProcessVariableValueResolverFactory::class)
    fun processVariableValueResolver(
        runtimeService: RuntimeService,
    ): ValueResolverFactory {
        return ProcessVariableValueResolverFactory(runtimeService)
    }
}
