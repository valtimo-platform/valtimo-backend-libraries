package com.ritense.valtimo.implementation.util.commandhandling

import org.springframework.transaction.annotation.Transactional
import kotlin.reflect.KClass

@Transactional
interface CommandHandler<C : Command<T>, out T> {

    fun execute(command: C) : T

    @Suppress("UNCHECKED_CAST")
    fun getCommandType() = this::class.supertypes.first().arguments.first().type!!.classifier as KClass<C>

}