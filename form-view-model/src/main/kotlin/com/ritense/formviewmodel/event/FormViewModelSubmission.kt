package com.ritense.formviewmodel.event

import com.ritense.formviewmodel.viewmodel.Submission

data class FormViewModelSubmission<T : Submission>(
    val formName: String,
    val submission: T, // Reuse of ViewModel is allowed.
    val taskInstanceId: String
)