package com.ritense.outbox.domain
import java.util.UUID
import com.fasterxml.jackson.databind.node.ObjectNode
import com.ritense.valtimo.contract.utils.SecurityUtils
import java.time.LocalDateTime

abstract class BaseEvent (
    val id: UUID  = UUID.randomUUID(),
    var source: String?,
    val specversion: String,
    val type: String,
    val data: LocalDateTime = LocalDateTime.now(),
    val userId: String = SecurityUtils.getCurrentUserLogin() ?: "System",
    val roles: String = SecurityUtils.getCurrentUserRoles().joinToString(),
    val resultType: String?,
    val resultId: String?,
    val result: ObjectNode,
)