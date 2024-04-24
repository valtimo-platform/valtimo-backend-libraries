package com.ritense.valtimo.implementation.util.commandhandling

class NoHandlerForCommandException(command: Command<*>) :
    RuntimeException("No matching handler available to handle command [$command]")