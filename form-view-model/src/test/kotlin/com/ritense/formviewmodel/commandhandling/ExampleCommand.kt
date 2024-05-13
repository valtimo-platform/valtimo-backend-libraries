package com.ritense.formviewmodel.commandhandling

import com.ritense.formviewmodel.error.FormException

data class ExampleCommand(
    val age: Int
) : Command<Boolean> {

    init {
        if (age > 18) {
            println("You are an adult")
        } else {
            throw FormException("You are not an adult", component = "age")
        }
    }
}