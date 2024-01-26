package com.ritense.valtimo.camunda.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "valtimo_script")
class ValtimoScript(
    @Id
    @Column(name = "key")
    val key: String,

    @Column(name = "content")
    var content: String = ""
)