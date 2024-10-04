/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
 *
 * Licensed under EUPL, Version 1.2 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ritense.case_.widget.displayproperties.codelist

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.jsontype.TypeSerializer
import com.ritense.case_.widget.WidgetView
import com.ritense.case_.widget.displayproperties.CodeListDisplayProperties


class CodeListSerializer(codeListProviders: List<CodeListProvider>) : JsonSerializer<CodeListDisplayProperties>() {

    private val providerMap: Map<String, CodeListProvider> by lazy {
        codeListProviders.associateBy { it.name }
    }

    override fun serializeWithType(value: CodeListDisplayProperties, gen: JsonGenerator, serializers: SerializerProvider, typeSer: TypeSerializer) {
        gen.assignCurrentValue(value)
        val typeIdDef = typeSer.writeTypePrefix(
            gen, typeSer.typeId(value, JsonToken.START_OBJECT)
        )
        serializeContent(value, gen, serializers)
        typeSer.writeTypeSuffix(gen, typeIdDef)
    }

    override fun serialize(value: CodeListDisplayProperties, gen: JsonGenerator, serializers: SerializerProvider) {
        gen.writeStartObject()
        serializeContent(value, gen, serializers)
        gen.writeEndObject()
    }

    private fun serializeContent(value: CodeListDisplayProperties, gen: JsonGenerator, serializers: SerializerProvider) {
        when(serializers.activeView) {
            WidgetView.Management::class.java, null-> {
                gen.writeStringField("providerName", value.providerName)
            }
            WidgetView.User::class.java -> {
                gen.writeObjectField("values", providerMap[value.providerName]!!.getCodeList())
            }
        }
    }
}