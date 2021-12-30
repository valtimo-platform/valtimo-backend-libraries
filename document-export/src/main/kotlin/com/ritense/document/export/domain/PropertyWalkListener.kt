package com.ritense.document.export.domain

import com.networknt.schema.ValidationMessage
import com.networknt.schema.walk.JsonSchemaWalkListener
import com.networknt.schema.walk.WalkEvent
import com.networknt.schema.walk.WalkFlow

class PropertyWalkListener(val events: MutableList<WalkEvent> = mutableListOf()) : JsonSchemaWalkListener {

    override fun onWalkStart(walkEvent: WalkEvent): WalkFlow {
        events.add(walkEvent)
        return WalkFlow.CONTINUE
    }

    override fun onWalkEnd(walkEvent: WalkEvent?, validationMessages: MutableSet<ValidationMessage>?) {
    }

}