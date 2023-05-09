package com.ritense.authorization

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "role")
data class Role(
    @Id
    @Column(name = "key", nullable = false, updatable = false)
    val key: String
)