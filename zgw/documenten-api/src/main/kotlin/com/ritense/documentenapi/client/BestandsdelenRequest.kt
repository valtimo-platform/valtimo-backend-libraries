package com.ritense.documentenapi.client

import org.springframework.core.io.ByteArrayResource

class BestandsdelenRequest(
    val inhoud: ByteArrayResource,
    val lock:String
)