package com.ritense.resource.service.request

import org.springframework.web.multipart.MultipartFile

data class CreateEnkelvoudigInformatieObjectRequest (
    val file: MultipartFile,
    val documentDefinitionName: String,
    )