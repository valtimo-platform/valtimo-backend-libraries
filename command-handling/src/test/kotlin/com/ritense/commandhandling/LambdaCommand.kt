package com.ritense.commandhandling

data class LambdaCommand(val lambda: Lambda) : Command<Unit> {

    fun interface Lambda {
        fun run()
    }

}