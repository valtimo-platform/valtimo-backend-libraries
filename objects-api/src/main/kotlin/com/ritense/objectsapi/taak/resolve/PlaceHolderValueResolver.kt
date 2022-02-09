package com.ritense.objectsapi.taak.resolve

import com.ritense.processdocument.domain.ProcessInstanceId
import org.camunda.bpm.engine.delegate.VariableScope

interface PlaceHolderValueResolver {

    fun resolveValue(placeholder: String, processInstanceId: ProcessInstanceId, variableScope: VariableScope): Any?
}