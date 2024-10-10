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
