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

package com.ritense.haalcentraal.brp.autoconfigure

import com.ritense.haalcentraal.brp.security.config.HaalCentraalBrpSecurityConfigurer
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.core.annotation.Order

@AutoConfiguration
class HaalCentraalSecurityAutoConfiguration {

    @Bean
    @Order(270)
    @ConditionalOnMissingBean(HaalCentraalBrpSecurityConfigurer::class)
    fun haalCentraalSecurityConfigurer(): HaalCentraalBrpSecurityConfigurer {
        return HaalCentraalBrpSecurityConfigurer()
    }
}