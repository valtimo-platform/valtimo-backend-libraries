package com.ritense.exact.client.endpoints

import com.fasterxml.jackson.databind.JsonNode
import org.operaton.bpm.engine.delegate.DelegateExecution

interface ExactGetRequest {

    fun createRequest(execution: DelegateExecution, token: String): GetEndpoint

    fun handleResponse(execution: DelegateExecution, response: JsonNode)

}