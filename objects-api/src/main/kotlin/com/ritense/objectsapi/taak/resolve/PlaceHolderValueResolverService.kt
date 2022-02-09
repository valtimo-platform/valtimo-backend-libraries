package com.ritense.objectsapi.taak.resolve

import com.ritense.processdocument.domain.ProcessInstanceId
import org.camunda.bpm.engine.delegate.VariableScope

class PlaceHolderValueResolverService(
    private val placeHolderValueResolvers: List<PlaceHolderValueResolver>
) : PlaceHolderValueResolver {

    override fun resolveValue(
        placeholder: String,
        processInstanceId: ProcessInstanceId,
        variableScope: VariableScope
    ): Any? {
        return placeHolderValueResolvers.firstNotNullOfOrNull {
            it.resolveValue(placeholder, processInstanceId, variableScope)
        }
    }
}