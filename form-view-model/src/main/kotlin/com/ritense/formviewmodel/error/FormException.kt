package com.ritense.formviewmodel.error

class FormException(
    private val msg: String,
    val component: String? = null,
    cause: Throwable? = null
) : Exception(msg, cause)