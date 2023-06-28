package com.ritense.valtimo.poc

import com.ritense.valtimo.poc.my.MyEntity
import java.util.UUID
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToOne
import javax.persistence.Table

@Entity
@Table(name = "my_entity_record")
class MyEntityRecord(
    @Id
    val id: UUID,

    @OneToOne
    @JoinColumn(name = "case_record_id")
    val caseRecord: CaseRecord,

    @OneToOne
    @JoinColumn(name = "my_entity_id")
    val myEntity: MyEntity
) {
}