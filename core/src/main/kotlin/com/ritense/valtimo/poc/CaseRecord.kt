package com.ritense.valtimo.poc

import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = "case_record")
class CaseRecord(
   @Id
   val id: UUID,

   @ManyToOne
   @JoinColumn(name = "case_id")
   val case: Case,

   @Column(name = "record_type")
   val recordType: String

)