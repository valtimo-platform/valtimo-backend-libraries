package com.ritense.objectsapi.taak.resolve

import com.ritense.processdocument.domain.ProcessInstanceId
import com.ritense.valtimo.contract.audit.VariableScope

interface PlaceHolderValueResolver {

    fun resolveValue(placeholder:String, processInstanceId: ProcessInstanceId, variableScope: VariableScope): Any?
}