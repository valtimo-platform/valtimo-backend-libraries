package com.ritense.objectsapi.resolver

import com.ritense.document.exception.NotImplementedException
import com.ritense.valtimo.contract.form.ExternalFormFieldType
import com.ritense.valtimo.contract.form.FormFieldDataResolver
import java.util.*

class ObjectsApiFormDataResolver: FormFieldDataResolver {

    override fun supports(externalFormFieldType: ExternalFormFieldType): Boolean {
        return externalFormFieldType == ExternalFormFieldType.OA
    }

    override fun get(documentDefinitionName: String, documentId: UUID, vararg varNames: String): Map<String, Any> {
        throw NotImplementedException()
    }
}
