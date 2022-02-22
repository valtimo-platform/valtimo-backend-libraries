package com.ritense.smartdocuments.domain

import org.springframework.http.MediaType

enum class DocumentFormatOption(val mediaType: MediaType) {
    DOCX(MediaType.valueOf("application/vnd.openxmlformats-officedocument.wordprocessingml.document")),
    PDF(MediaType.valueOf("application/pdf")),
    XML(MediaType.valueOf("application/xml")),
    HTML(MediaType.valueOf("text/html")),
}