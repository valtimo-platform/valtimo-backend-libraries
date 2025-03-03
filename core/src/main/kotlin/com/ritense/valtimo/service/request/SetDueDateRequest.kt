package com.ritense.valtimo.service.request

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime

data class SetDueDateRequest @JsonCreator constructor(
    @JsonProperty(value = "dueDate", required = true)
    @NotNull val dueDate: LocalDateTime,
)