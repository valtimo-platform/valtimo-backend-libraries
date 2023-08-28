package com.ritense.exact.client.endpoints

import com.fasterxml.jackson.databind.JsonNode
import org.camunda.bpm.engine.delegate.DelegateExecution

interface ExactPutRequest {

    fun createRequest(execution: DelegateExecution, token: String): PutEndpoint

    fun handleResponse(execution: DelegateExecution, response: JsonNode)

}