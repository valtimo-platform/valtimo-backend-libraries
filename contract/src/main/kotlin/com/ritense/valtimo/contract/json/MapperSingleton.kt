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

package com.ritense.valtimo.contract.json

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import com.ritense.valtimo.contract.json.serializer.PageSerializer
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.data.domain.Page
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import java.time.format.DateTimeFormatter

object MapperSingleton {

    const val DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"

    val customizer = Jackson2ObjectMapperBuilderCustomizer { builder ->
        builder.simpleDateFormat(DATE_TIME_FORMAT)
        builder.serializers(LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT)))
        builder.defaultViewInclusion(false)
        builder.serializerByType(Page::class.java, PageSerializer())
    }

    private var mapper: ObjectMapper

    fun set(mapper: ObjectMapper) {
        MapperSingleton.mapper = mapper
    }

    @JvmStatic
    fun get(): ObjectMapper = mapper

    init {
        val builder = Jackson2ObjectMapperBuilder()
        customizer.customize(builder)
        builder.findModulesViaServiceLoader(true)
        mapper = builder.build()
    }

}
