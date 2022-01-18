package com.ritense.document.export.domain

import com.jayway.jsonpath.Configuration
import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.Option
import com.networknt.schema.ValidationMessage
import com.networknt.schema.walk.JsonSchemaWalkListener
import com.networknt.schema.walk.WalkEvent
import com.networknt.schema.walk.WalkFlow
import org.json.JSONArray
import org.json.JSONObject

class PropertyWalkListener(var json: String = "{}") : JsonSchemaWalkListener {

    companion object {
        val CONF: Configuration = Configuration.defaultConfiguration()
            .addOptions(Option.DEFAULT_PATH_LEAF_TO_NULL)
            .addOptions(Option.SUPPRESS_EXCEPTIONS)
    }

    override fun onWalkStart(walkEvent: WalkEvent): WalkFlow {
        val context = JsonPath.using(CONF).parse(this.json)
        if (walkEvent.schemaNode.has("type")) {
            if (walkEvent.schemaNode.get("type").textValue() == "array") {
                context.set(walkEvent.at, JSONArray(1).put(JSONObject()))
            } else if (walkEvent.schemaNode.get("type").textValue() == "string") {
                context.set(walkEvent.at, "")
            } else {
                context.set(walkEvent.at, JSONObject())
            }
        } else {
            context.set(walkEvent.at, JSONObject())
        }
        json = context.jsonString()
        return WalkFlow.CONTINUE
    }

    override fun onWalkEnd(walkEvent: WalkEvent?, validationMessages: MutableSet<ValidationMessage>?) {
    }

}