package com.ritense.formviewmodel.error

// TODO does not yet work
class FormException(
    msg: String,
    val component: String? = null,
    cause: Throwable? = null
) : Exception(msg, cause)