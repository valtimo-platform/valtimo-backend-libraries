package com.ritense.outbox.domain

import liquibase.pro.packaged.T
import java.time.LocalDateTime
import java.util.*


data class CloudEvent (
    val id: UUID = UUID.randomUUID(),
    val source: String,
    val specversion: String,
    val type: String,
    val time: LocalDateTime,
    val data: T
)