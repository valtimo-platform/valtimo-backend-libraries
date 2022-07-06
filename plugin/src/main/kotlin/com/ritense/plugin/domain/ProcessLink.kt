package com.ritense.plugin.domain

import java.util.UUID

/**
 * TODO: Move this interface to the core?
 */
interface ProcessLink {
    val id: UUID
    val processDefinitionId: String
    val activityId: String
}
