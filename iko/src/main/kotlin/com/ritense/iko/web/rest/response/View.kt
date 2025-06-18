package com.ritense.iko.web.rest.response

import com.ritense.iko.domain.View as ViewEntity

data class View(
    val name: String,
) {
    companion object {
        fun fromEntity(entity: ViewEntity) = View(
            name = entity.name
        )
    }
}