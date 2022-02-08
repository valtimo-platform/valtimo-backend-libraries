package com.ritense.objectsapi.taak.resolve

import com.ritense.processdocument.domain.ProcessInstanceId
import org.camunda.bpm.engine.delegate.VariableScope

class ProcessVariableValueResolver: PlaceHolderValueResolver {
    override fun resolveValue(placeholder: String,
                              processInstanceId: ProcessInstanceId,
                              variableScope: VariableScope): Any? {
        if(!placeholder.startsWith("pv:")) return null

        return variableScope.variables[placeholder.substringAfter(":")]
    }
}