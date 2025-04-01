package com.ritense.gzac.valueresolver

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.valueresolver.ValueResolverFactory
import jakarta.servlet.http.HttpServletRequest
import org.camunda.bpm.engine.delegate.VariableScope
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import java.net.URI
import java.util.function.Function

@Component
class IkoValueResolver(
    val restClient: RestClient = RestClient.create(),
    val objectMapper: ObjectMapper,
    val request: HttpServletRequest
) : ValueResolverFactory {
    override fun supportedPrefix() = "iko"


    override fun createResolver(processInstanceId: String, variableScope: VariableScope): Function<String, Any?> {
        return Function { variableScope }
    }

    override fun createResolver(documentId: String): Function<String, Any?> {
        val result = restClient.get().uri(URI.create("http://localhost:9999/hello?${request.queryString}"))
            .exchange { clientRequest, clientResponse ->
                if (clientResponse.statusCode == HttpStatus.OK) {
                    objectMapper.readTree(clientResponse.bodyTo(String::class.java))
                } else {
                    objectMapper.readTree("{}")
                }
            }

        return Function { a: String ->
            result.at(a)
        }
    }

    override fun handleValues(processInstanceId: String, variableScope: VariableScope?, values: Map<String, Any?>) {
        return
    }
}