package com.ritense.formviewmodel.util.commandhandling

class NoHandlerForCommandException(command: Command<*>) :
    RuntimeException("No matching handler available to handle command [$command]")