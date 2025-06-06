package com.ritense.iko.service

import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.valueresolver.ValueResolverFactory
import org.camunda.bpm.engine.delegate.VariableScope
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import java.util.function.Function

class IkoValueResolver(
    private val restClientBuilder: RestClient.Builder,
) : ValueResolverFactory, QueryParamValueResolverFactory {

    override fun supportedPrefix(): String = "iko"

    override fun createResolver(queryParam: QueryString): Function<String, Any?> {
        TODO("Not yet implemented")
        // Create tje resolver
    }

    override fun resolveValues(
        queryParam: QueryString,
        requestedValues: Collection<String>
    ): Map<String, Any?> {
        // Add api call here

        val resolvedValues = buildRestClient()
            .get()
            .uri("TODO URL TO PROFILE")
            .retrieve()
            .body<ObjectNode>()!!
        // TODO return value as map
        return emptyMap()
    }

    override fun createResolver(processInstanceId: String, variableScope: VariableScope): Function<String, Any?> {
        throw NotImplementedError()
    }

    override fun createResolver(documentId: String): Function<String, Any?> {
        throw NotImplementedError()
    }

    override fun handleValues(processInstanceId: String, variableScope: VariableScope?, values: Map<String, Any?>) {
        throw NotImplementedError()
    }


    private fun buildRestClient(): RestClient {
        return restClientBuilder
            .clone()
            .defaultHeaders { headers ->
                headers.setBearerAuth("TODO SAME AS CURRENT USER LOGGED IN")
            }.build()
    }

}