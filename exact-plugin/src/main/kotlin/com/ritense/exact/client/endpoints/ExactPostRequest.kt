package com.ritense.exact.client.endpoints

import com.fasterxml.jackson.databind.JsonNode
import org.camunda.bpm.engine.delegate.DelegateExecution

interface ExactPostRequest {

    fun createRequest(execution: DelegateExecution, token: String): PostEndpoint

    fun handleResponse(execution: DelegateExecution, response: JsonNode)

}