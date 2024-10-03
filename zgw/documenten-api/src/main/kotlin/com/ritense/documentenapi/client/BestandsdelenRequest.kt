package com.ritense.documentenapi.client

import java.io.InputStream

class BestandsdelenRequest(
    val inhoud: InputStream,
    val lock:String
)