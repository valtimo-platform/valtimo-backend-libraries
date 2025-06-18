package com.ritense.iko.domain

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.util.UUID

@Entity
@Table(
    name = "view",
    uniqueConstraints = [UniqueConstraint(columnNames = ["id", "name"])]
)
class View(
    @Id
    private val id: UUID,

    @Column(name = "name", unique = true)
    var name: String,

    @OneToMany(
        cascade = [(CascadeType.ALL)],
        fetch = FetchType.EAGER,
        orphanRemoval = true,
        mappedBy = "view"
    )
    val searches: MutableSet<Search> = mutableSetOf()
) {
    companion object {
        fun create(
            id: UUID = UUID.randomUUID(),
            name: String,
        ): View {
            return View(
                id = id,
                name = name,
            )
        }
    }
}