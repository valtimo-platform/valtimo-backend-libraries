package com.ritense.plugin.domain

import com.ritense.valtimo.contract.domain.AbstractId

/**
 * TODO: Move this interface to the core?
 */
interface ProcessLink {
    val id: AbstractId<out Any>
    val processDefinitionId: String
    val activityId: String
}
