package com.ritense.resource.service.request

import org.springframework.http.MediaType
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class ByteArrayMultipartFile(
    val content: ByteArray,
    val filename: String,
    val contentType: MediaType
) : MultipartFile {

    override fun getInputStream(): InputStream {
        return ByteArrayInputStream(content)
    }

    override fun getName(): String {
        return filename
    }

    override fun getOriginalFilename(): String? {
        return filename
    }

    override fun getContentType(): String? {
        return contentType.toString()
    }

    override fun isEmpty(): Boolean {
        return content.isNotEmpty()
    }

    override fun getSize(): Long {
        return content.size.toLong()
    }

    override fun getBytes(): ByteArray {
        return content
    }

    override fun transferTo(dest: File) {
        FileOutputStream(dest).write(content)
    }
}