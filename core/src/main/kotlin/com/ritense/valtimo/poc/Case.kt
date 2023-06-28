package com.ritense.valtimo.poc

import java.util.UUID
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.OneToMany
import javax.persistence.Table

@Entity
@Table(name = "case")
class Case(
    @Id
    val id: UUID,

    @OneToMany(mappedBy = "case")
    val caseRecords: Set<CaseRecord>
) {
}