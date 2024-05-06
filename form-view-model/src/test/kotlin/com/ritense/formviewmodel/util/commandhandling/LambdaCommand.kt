package com.ritense.valtimo.implementation.util.commandhandling

import com.ritense.formviewmodel.util.commandhandling.Command


data class LambdaCommand(val lambda: Lambda) : Command<Unit> {

    fun interface Lambda {
        fun run()
    }

}