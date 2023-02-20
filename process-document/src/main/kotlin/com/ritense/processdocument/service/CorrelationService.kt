package com.ritense.processdocument.service

import org.camunda.bpm.engine.runtime.MessageCorrelationResult

interface CorrelationService {

    fun sendStartMessage(message: String)
    fun sendStartMessage(message: String,businessKey: String?)
    fun sendStartMessage(message: String,businessKey: String?, variables: Map<String, Any>?)
    fun sendStartMessage(message: String,businessKey: String, variables: Map<String, Any>?, targetProcessDefinitionKey: String)
    fun sendMessageToAll(message: String): List<MessageCorrelationResult>
    fun sendMessageToAll(message: String,businessKey: String?): List<MessageCorrelationResult>
    fun sendMessageToAll(message: String,businessKey: String?,variables: Map<String,Any>?): List<MessageCorrelationResult>

}