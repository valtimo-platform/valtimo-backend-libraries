package com.ritense.outbox.service

interface OutboxService<T> {

    fun send(message: T)
}