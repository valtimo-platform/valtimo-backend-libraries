/*
 * Copyright 2015-2020 Ritense BV, the Netherlands.
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

package com.ritense.connector.config

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import mu.KotlinLogging
import javax.crypto.IllegalBlockSizeException

class Decryptor(private var aesEncryption: AesEncryption) : JsonDeserializer<String>() {

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext?): String {
        val node = p.readValueAsTree<JsonNode>()
        val value = node.textValue()
        if (value.isNotEmpty() && isEncrypted(value)) {
            try {
                return aesEncryption.decrypt(node.textValue().substringAfter("ENCRYPTED:"))
            } catch (e: IllegalBlockSizeException) {
                logger.info { "Cannot decrypt skip" }
            }
        }
        return value
    }

    private fun isEncrypted(value: String): Boolean {
        return value.startsWith("ENCRYPTED:")
    }

    companion object {
        val logger = KotlinLogging.logger {}
    }
}