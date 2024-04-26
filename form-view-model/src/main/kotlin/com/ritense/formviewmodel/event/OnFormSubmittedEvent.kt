package com.ritense.formviewmodel.event

import com.ritense.formviewmodel.domain.ViewModel

data class OnFormSubmittedEvent(
    val submission: ViewModel,
    val formName: String,
    val taskInstanceId: String
)