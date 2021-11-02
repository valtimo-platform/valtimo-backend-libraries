package com.ritense.connector.impl

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.ritense.connector.config.Decryptor
import com.ritense.connector.config.Encryptor

data class NestedObject(
    @get:JsonDeserialize(using = Decryptor::class)
    @set:JsonSerialize(using = Encryptor::class)
    var name: String = ""
)