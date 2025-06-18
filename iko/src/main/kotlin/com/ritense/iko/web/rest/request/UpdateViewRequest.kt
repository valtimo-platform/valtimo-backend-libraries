package com.ritense.iko.web.rest.request

import com.ritense.iko.web.rest.response.Search
import jakarta.validation.constraints.NotBlank
import java.util.UUID

data class UpdateViewRequest(
    val id: UUID,
    @NotBlank
    val name: String,
    val searches: List<Search>
)