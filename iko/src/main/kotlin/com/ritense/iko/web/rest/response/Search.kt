package com.ritense.iko.web.rest.response

import com.ritense.valtimo.contract.validation.Url

data class Search(
    @Url
    val url: String,
) {
    /*companion object {
        fun fromEntity(entity: ViewEntity) = Search(
            name = entity.name
        )
    }*/
}