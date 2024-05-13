package com.ritense.formviewmodel.commandhandling

import com.ritense.formviewmodel.commandhandling.Command

data class ExampleCommand(
    val age: Int
) : Command<Boolean> {

    init {
        if (age > 18) {
            println("You are an adult")
        } else {
            throw IllegalArgumentException("You are not an adult")
        }
    }
}