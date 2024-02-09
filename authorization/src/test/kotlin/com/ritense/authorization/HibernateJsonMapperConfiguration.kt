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
package com.ritense.authorization

import com.fasterxml.jackson.databind.ObjectMapper
import org.hibernate.cfg.AvailableSettings
import org.hibernate.type.format.jackson.JacksonJsonFormatMapper
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer
import org.springframework.context.annotation.Bean


@AutoConfiguration
class HibernateJsonMapperConfiguration {

    @Bean
    fun jsonFormatMapperCustomizer(objectMapper: ObjectMapper): HibernatePropertiesCustomizer {
        return HibernatePropertiesCustomizer { properties: MutableMap<String?, Any?> ->
            properties[AvailableSettings.JSON_FORMAT_MAPPER] = JacksonJsonFormatMapper(objectMapper)
        }
    }
}
