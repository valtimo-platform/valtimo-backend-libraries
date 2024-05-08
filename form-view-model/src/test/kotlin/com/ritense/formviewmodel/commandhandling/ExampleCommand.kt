package com.ritense.formviewmodel.commandhandling

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