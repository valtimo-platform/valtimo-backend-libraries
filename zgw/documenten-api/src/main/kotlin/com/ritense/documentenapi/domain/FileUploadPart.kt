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

package com.ritense.documentenapi.domain

import com.ritense.documentenapi.client.Bestandsdeel
import com.ritense.documentenapi.client.BestandsdelenRequest
import org.springframework.core.io.ByteArrayResource
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap

data class FileUploadPart(
    val bestandsdeel: Bestandsdeel,
    val bestandsdelenRequest: BestandsdelenRequest,
    val bestandsnaam: String,
) {

    fun createBody(): MultiValueMap<String, Any> {
        val chunk = ByteArray(bestandsdeel.omvang)
        val bytesRead = bestandsdelenRequest.inhoud.read(chunk)

        require(bytesRead == chunk.size) {
            "Failed to read all the bytes to upload. " +
                "Expected ${chunk.size} bytes, but only read $bytesRead bytes. " +
                "Check bestandsdeel: $bestandsdeel."
        }

        return createMultiValueMap(createFileResource(chunk), bestandsdelenRequest.lock)
    }

    private fun createFileResource(chunk: ByteArray): ByteArrayResource {
        return object : ByteArrayResource(chunk) {
            override fun getFilename(): String {
                return bestandsnaam
            }
        }
    }

    private fun createMultiValueMap(fileResource: ByteArrayResource, lock: Any): MultiValueMap<String, Any> {
        return LinkedMultiValueMap<String, Any>().apply {
            add("inhoud", fileResource)
            add("lock", lock)
        }
    }
}
