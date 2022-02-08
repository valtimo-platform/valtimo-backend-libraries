package com.ritense.objectsapi.taak.resolve

import com.fasterxml.jackson.core.JsonPointer
import com.ritense.objectsapi.taak.ProcessDocumentService
import com.ritense.objectsapi.taak.orNull
import com.ritense.processdocument.domain.ProcessInstanceId
import com.ritense.valtimo.contract.json.Mapper
import org.camunda.bpm.engine.delegate.VariableScope

class DocumentValueResolver(
    private val processDocumentService: ProcessDocumentService
) : PlaceHolderValueResolver {

    override fun resolveValue(
        placeholder: String,
        processInstanceId: ProcessInstanceId,
        variableScope: VariableScope
    ): Any? {
        if (!placeholder.startsWith("doc:")) return null

        val document = processDocumentService.getDocument(processInstanceId, variableScope)

        val value = document.content().getValueBy(JsonPointer.valueOf(placeholder.substringAfter(":"))).orNull()
        if(value?.isValueNode == true) {
            return Mapper.INSTANCE.get().readValue(value.asText(), Object::class.java)
        }

        return null
    }
}