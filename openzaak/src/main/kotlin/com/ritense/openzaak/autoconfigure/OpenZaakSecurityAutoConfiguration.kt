/*
 * Copyright 2020 Dimpact.
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

package com.ritense.openzaak.autoconfigure

import com.ritense.openzaak.security.config.OpenZaakConfigHttpSecurityConfigurer
import com.ritense.openzaak.security.config.ResultaatHttpSecurityConfigurer
import com.ritense.openzaak.security.config.StatusHttpSecurityConfigurer
import com.ritense.openzaak.security.config.ZaakTypeHttpSecurityConfigurer
import com.ritense.openzaak.security.config.ZaakTypeLinkHttpSecurityConfigurer
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order

@Configuration
class OpenZaakSecurityAutoConfiguration {

    @Bean
    @Order(270)
    @ConditionalOnMissingBean(OpenZaakConfigHttpSecurityConfigurer::class)
    fun openZaakConfigHttpSecurityConfigurer(): OpenZaakConfigHttpSecurityConfigurer {
        return OpenZaakConfigHttpSecurityConfigurer()
    }

    @Bean
    @Order(270)
    @ConditionalOnMissingBean(ZaakTypeLinkHttpSecurityConfigurer::class)
    fun zaakTypeLinkHttpSecurityConfigurer(): ZaakTypeLinkHttpSecurityConfigurer {
        return ZaakTypeLinkHttpSecurityConfigurer()
    }

    @Bean
    @Order(270)
    @ConditionalOnMissingBean(ResultaatHttpSecurityConfigurer::class)
    fun resultaatHttpSecurityConfigurer(): ResultaatHttpSecurityConfigurer {
        return ResultaatHttpSecurityConfigurer()
    }

    @Bean
    @Order(270)
    @ConditionalOnMissingBean(StatusHttpSecurityConfigurer::class)
    fun statusHttpSecurityConfigurer(): StatusHttpSecurityConfigurer {
        return StatusHttpSecurityConfigurer()
    }

    @Bean
    @Order(270)
    @ConditionalOnMissingBean(ZaakTypeHttpSecurityConfigurer::class)
    fun zaakTypeHttpSecurityConfigurer(): ZaakTypeHttpSecurityConfigurer {
        return ZaakTypeHttpSecurityConfigurer()
    }

}