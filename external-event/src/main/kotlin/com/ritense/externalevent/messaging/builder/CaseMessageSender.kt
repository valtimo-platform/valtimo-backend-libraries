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

package com.ritense.externalevent.messaging.builder

import com.fasterxml.jackson.core.JsonPointer
import com.fasterxml.jackson.databind.JsonNode
import com.ritense.document.service.impl.JsonSchemaDocumentService
import com.ritense.externalevent.service.ExternalCaseService
import org.camunda.bpm.engine.delegate.DelegateExecution

data class CaseMessageSender(
    val payload: MutableMap<JsonPointer, JsonNode> = mutableMapOf(),
    val externalCaseService: ExternalCaseService,
    val documentService: JsonSchemaDocumentService,
    var execution: DelegateExecution?
) {

    fun execution(execution: DelegateExecution): CaseMessageSender {
        this.execution = execution
        return this
    }

    fun put(key: String, pathToValue: String): CaseMessageSender {
        this.payload[JsonPointer.valueOf(key)] = externalCaseService.getCaseValue(
            execution!!,
            JsonPointer.valueOf(pathToValue)
        )
        return this
    }

    fun send() {
        externalCaseService.publishCaseUpdate(payload, this.execution!!)
    }

}