package com.ritense.formviewmodel.commandhandling

import com.ritense.formviewmodel.commandhandling.Command

data class LambdaCommand(val lambda: Lambda) : Command<Unit> {

    fun interface Lambda {
        fun run()
    }

}