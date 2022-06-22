package com.ritense.objectsapi.listener

import com.ritense.document.exception.NotImplementedException
import com.ritense.valtimo.contract.event.ExternalDataSubmittedEvent
import org.springframework.context.event.EventListener

class ObjectsApiFormDataListener {

    @EventListener(ExternalDataSubmittedEvent::class)
    fun handle(event: ExternalDataSubmittedEvent) {
        throw NotImplementedException()
    }
}
