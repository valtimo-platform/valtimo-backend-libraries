package com.ritense.formviewmodel.event

import com.ritense.formviewmodel.domain.ViewModel
import com.ritense.valtimo.implementation.util.commandhandling.Command
import com.ritense.valtimo.implementation.util.commandhandling.dispatchCommand

interface OnFormSubmittedCommandDispatcher<T : ViewModel> : OnFormSubmittedEventHandler<T> {

    override fun handle(submission: T, taskInstanceId: String) {
        initializeCommands(submission, taskInstanceId).forEach { dispatchCommand(it) }
    }

    fun initializeCommands(submission: T, taskInstanceId: String): List<Command<*>>

}