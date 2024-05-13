package com.ritense.formviewmodel.commandhandling

class NoHandlerForCommandException(command: Command<*>) :
    RuntimeException("No matching handler available to handle command [$command]")