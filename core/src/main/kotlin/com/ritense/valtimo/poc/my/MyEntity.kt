package com.ritense.valtimo.poc.my

import com.ritense.valtimo.poc.Record
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "my_entity")
class MyEntity(
    @Id
    val id: UUID,
    @Column(name = "content")
    val content: String
) : Record{
}