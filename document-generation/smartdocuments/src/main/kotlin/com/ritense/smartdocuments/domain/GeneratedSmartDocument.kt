package com.ritense.smartdocuments.domain

import com.ritense.documentgeneration.domain.GeneratedDocument
import java.util.Objects

data class GeneratedSmartDocument(
    private val name: String,
    private val extension: String,
    private val contentType: String,
    private val bytes: ByteArray,
) : GeneratedDocument {

    override fun getName(): String {
        return name
    }

    override fun getExtension(): String {
        return extension
    }

    override fun getSize(): Long {
        return bytes.size.toLong()
    }

    override fun getContentType(): String {
        return contentType
    }

    override fun getAsByteArray(): ByteArray {
        return bytes
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GeneratedSmartDocument

        if (name != other.name) return false
        if (extension != other.extension) return false
        if (contentType != other.contentType) return false

        return true
    }

    override fun hashCode(): Int {
        return Objects.hash(name, extension, contentType)
    }
}
