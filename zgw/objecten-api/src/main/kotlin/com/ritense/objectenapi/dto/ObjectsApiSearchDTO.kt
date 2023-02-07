package com.ritense.objectenapi.dto

import com.ritense.search.domain.DataType
import com.ritense.search.domain.FieldType

data class ObjectsApiSearchDTO(
    val key: String,
    val type: DataType,
    val value: Any,
    val wayToSearch: FieldType
)
