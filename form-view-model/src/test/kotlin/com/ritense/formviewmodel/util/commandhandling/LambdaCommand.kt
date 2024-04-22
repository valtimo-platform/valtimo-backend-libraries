package com.ritense.valtimo.implementation.util.commandhandling


data class LambdaCommand(val lambda: Lambda) : Command<Unit> {

    fun interface Lambda {
        fun run()
    }

}