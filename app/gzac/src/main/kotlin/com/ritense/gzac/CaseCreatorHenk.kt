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

package com.ritense.gzac

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.document.domain.impl.request.NewDocumentRequest
import com.ritense.processdocument.domain.impl.request.NewDocumentAndStartProcessRequest
import com.ritense.processdocument.service.impl.CamundaProcessJsonSchemaDocumentService
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class CaseCreatorHenk(
    val camundaProcessJsonSchemaDocumentService: CamundaProcessJsonSchemaDocumentService,
    val objectMapper: ObjectMapper
) {

    @EventListener(ApplicationReadyEvent::class)
    fun onApplicationReadyEvent() {
//        runWithoutAuthorization<Any?> {
//            for (i in 1..10000) {
//                val startRequest = NewDocumentAndStartProcessRequest(
//                    "lening-aanvragen",
//                    NewDocumentRequest(
//                        "leningen",
//                        objectMapper.readTree("{}")
//                    )
//                )
//                camundaProcessJsonSchemaDocumentService.newDocumentAndStartProcess(startRequest)
//            }
//        }
    }
}