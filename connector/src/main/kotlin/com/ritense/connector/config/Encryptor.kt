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

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider

class Encryptor(private var aesEncryption: AesEncryption) : JsonSerializer<String>() {

    override fun serialize(value: String, gen: JsonGenerator, serializers: SerializerProvider) {
        return with(gen) {
            if (value.isNotEmpty() && !isEncrypted(value)) {
                writeString("ENCRYPTED:" + aesEncryption.encrypt(value))
            } else {
                writeString(value)
            }
        }
    }

    private fun isEncrypted(value: String): Boolean {
        return value.startsWith("ENCRYPTED:")
    }
}