package com.ritense.iko.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import java.util.UUID

@Entity
class Search(

    @Id
    val id: UUID = UUID.randomUUID(),

    @JoinColumn(name = "view_id")
    @ManyToOne
    var view: View,

    @Column(name = "url")
    private val url: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Search

        if (id != other.id) return false
        if (view != other.view) return false
        if (url != other.url) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + view.hashCode()
        result = 31 * result + url.hashCode()
        return result
    }
}