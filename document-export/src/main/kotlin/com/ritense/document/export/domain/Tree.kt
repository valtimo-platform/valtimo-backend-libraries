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
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.networknt.schema.JsonSchemaFactory
import com.networknt.schema.SchemaValidatorsConfig
import com.ritense.valtimo.contract.json.Mapper
import com.ritense.valtimo.contract.validation.Validatable
import org.hibernate.annotations.Type
import javax.persistence.Column
import javax.persistence.Embeddable

@Embeddable
class Tree(
    @Type(type = "com.vladmihalcea.hibernate.type.json.JsonStringType")
    @Column(name = "nodes", columnDefinition = "json")
    //TODO not empty
    val root: MutableList<Node>
) : Validatable {

    init {
        validate()
    }

    companion object {

        fun init(schema: JsonNode): Tree {
            val config = SchemaValidatorsConfig()
            val propertyWalkListener = PropertyWalkListener()
            config.addPropertyWalkListener(propertyWalkListener)
            val jsonSchema2 = JsonSchemaFactory
                .getInstance()//SpecVersion.VersionFlag.V7
                .getSchema(schema, config)

            jsonSchema2.walk(null, false)
            val tree = Mapper.INSTANCE.get().readTree(propertyWalkListener.json)
            val nodes = mutableListOf<Node>()
            traverse(tree, nodes)

            /*propertyWalkListener.events.forEach { walkEvent ->
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
            }*/
            return Tree(nodes)
        }

        //Not perfect
        // Should parse:
        /* {
            "myAdress": {
                "number": "",
                "country": {
                    "iso": ""
                },
                "streetName": "",
                "province": "",
                "city": ""
            },
            "otherAddresses": [{
                "number": "",
                "country": {
                    "iso": ""
                },
                "streetName": "",
                "province": "",
                "city": ""
            }],
            "voornaam": ""
        }}*/
        fun traverse(rootNode: JsonNode, result: MutableList<Node>) {
            val iterator = rootNode.fields()
            while (iterator.hasNext()) {
                val jsonNode = iterator.next()
                if (jsonNode.value.isTextual) {
                    result.add(Node(name = jsonNode.key))
                } else if (jsonNode.value.isObject) {
                    val objectNode = jsonNode.value as ObjectNode
                    val containerNode = Node(name = "Container ${jsonNode.key.toString()}")
                    objectNode.fieldNames().forEach { fieldName ->
                        containerNode.children.add(
                            Node(name = fieldName, parent = containerNode)
                        )
                    }
                    result.add(containerNode)
                } else if (jsonNode.value.isArray) {
                    val arrayNode = jsonNode.value as ArrayNode
                    val containerNode = Node(name = "Container ${jsonNode.key.toString()}")
                    arrayNode.get(0).fieldNames().forEach { fieldName ->
                        containerNode.children.add(
                            Node(name = fieldName, parent = containerNode)
                        )
                    }
                    result.add(containerNode)
                }
            }

            /* rootNode.forEach { jsonNode ->
                 if (jsonNode.isTextual) {
                     *//*  val containerNode = Node(name = "Container")
                      containerNode.children.add(Node(name = field, parent = containerNode))
                      result.add(containerNode)*//*
                    result.add(Node(name = jsonNode.fieldNames().next()))
                } else if (jsonNode.isObject) {
                    jsonNode as ObjectNode
                    val containerNode = Node(name = "Container ")
                    jsonNode.fieldNames().forEach { fieldName ->
                        containerNode.children.add(
                            Node(name = fieldName, parent = containerNode)
                        )
                    }
                    result.add(containerNode)
                } else if (jsonNode.isArray) {
                    traverse(jsonNode, result)
                }
            }*/
        }

    }

}