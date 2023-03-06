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

package com.ritense.processdocument.service

import org.camunda.bpm.engine.runtime.MessageCorrelationResult

interface CorrelationService {

    fun sendStartMessage(message: String,businessKey: String): MessageCorrelationResult
    fun sendStartMessage(message: String,businessKey: String, variables: Map<String, Any>?): MessageCorrelationResult
    fun sendStartMessageWithProcessDefinitionKey(message: String,targetProcessDefinitionKey: String,businessKey: String, variables: Map<String, Any>?)
    fun sendCatchEventMessage(message: String, businessKey: String): MessageCorrelationResult
    fun sendCatchEventMessage(message: String, businessKey: String, variables: Map<String, Any>?): MessageCorrelationResult
    fun sendCatchEventMessageToAll(message: String, businessKey: String): List<MessageCorrelationResult>
    fun sendCatchEventMessageToAll(message: String, businessKey: String, variables: Map<String,Any>?): List<MessageCorrelationResult>

}