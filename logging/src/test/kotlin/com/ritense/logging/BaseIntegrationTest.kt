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

package com.ritense.logging

import com.ritense.logging.repository.LoggingEventExceptionRepository
import com.ritense.logging.repository.LoggingEventPropertyRepository
import com.ritense.logging.repository.LoggingEventRepository
import com.ritense.logging.service.LoggingEventDeletionService
import com.ritense.logging.service.LoggingEventService
import com.ritense.logging.testimpl.LogResourceBean
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension

@SpringBootTest
@ExtendWith(SpringExtension::class)
@Tag("integration")
abstract class BaseIntegrationTest {

    @Autowired
    lateinit var logResourceBean: LogResourceBean

    @Autowired
    lateinit var loggingEventRepository: LoggingEventRepository

    @Autowired
    lateinit var loggingEventPropertyRepository: LoggingEventPropertyRepository

    @Autowired
    lateinit var loggingEventExceptionRepository: LoggingEventExceptionRepository

    @Autowired
    lateinit var loggingEventDeletionService: LoggingEventDeletionService

    @Autowired
    lateinit var loggingEventService: LoggingEventService
}