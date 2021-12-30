/*
 * Copyright 2015-2021 Ritense BV, the Netherlands.
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

package com.ritense.document.export.domain

import com.fasterxml.jackson.databind.JsonNode
import com.networknt.schema.JsonSchemaFactory
import com.networknt.schema.SchemaValidatorsConfig
import org.hibernate.annotations.Type
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Embeddable

@Embeddable
data class Tree(
    @Type(type = "com.vladmihalcea.hibernate.type.json.JsonStringType")
    @Column(name = "nodes", columnDefinition = "json")
    val nodes: List<Node>
) {

    companion object {

        fun init(schema: JsonNode): Tree {
            val config = SchemaValidatorsConfig()
            val propertyWalkListener = PropertyWalkListener()
            config.addPropertyWalkListener(propertyWalkListener)
            val jsonSchema2 = JsonSchemaFactory
                .getInstance()//SpecVersion.VersionFlag.V7
                .getSchema(schema, config)

            val result = jsonSchema2.walk(null, false)
            val nodes = mutableListOf<Node>()
            propertyWalkListener.events.forEach { walkEvent ->
                //val node = nodes.find { walkEvent.at.startsWith(it.path) }
                val node = nodes.find { walkEvent.at.startsWith(it.path) }

                if (node == null) {
                    nodes.add(
                        Node(
                            id = UUID.randomUUID(),
                            name = walkEvent.at.substringAfterLast("."),
                            path = walkEvent.at,
                            parent = null
                        )
                    )
                } else {
                    node.childs.add(
                        Node(
                            id = UUID.randomUUID(),
                            name = walkEvent.at.substringAfterLast("."),
                            path = walkEvent.at,
                            parent = node
                        )
                    )
                }
            }
            return Tree(nodes)
        }

    }

}