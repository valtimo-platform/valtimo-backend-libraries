package com.ritense.processdocument.dto

import java.io.Serializable

data class ProcessInstanceSimpleDto (
    val processName: String,
    val processInstanceId: String
) : Serializable