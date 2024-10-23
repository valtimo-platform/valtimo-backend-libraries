package com.ritense.case.domain.casedefinition

import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "case_definition_2")
data class CaseDefinition(
    @Id
    val id: UUID,
    @Column(name = "case_definition_name")
    val name: String,
    @Embedded
    val version: SemVer
) {
    init {
        require(name.isNotBlank()) { "CaseDefinition name must not be blank" }
    }
}