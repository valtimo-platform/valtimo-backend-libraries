package com.ritense.documentenapi.domain

import org.springframework.core.io.ByteArrayResource
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap

data class FileUploadPart(
    val chunk: ByteArray,
    val bestandsnaam: String,
    val lock: String
) {

    fun createBody(): MultiValueMap<String, Any> {
        val fileResource = object : ByteArrayResource(chunk) {
            override fun getFilename(): String {
                return bestandsnaam
            }
        }

        return LinkedMultiValueMap<String, Any>().apply {
            add("inhoud", fileResource)
            add("lock", lock)
        }
    }
}
