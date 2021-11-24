package com.ritense.mail.domain.blacklist

import com.ritense.valtimo.contract.validation.Validatable
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "blacklisted_email")
data class BlacklistedEmail(
    @Id
    @Column(name = "email", columnDefinition = "VARCHAR(500)")
    private var emailAddress: String,

    @Column(name = "date_created", columnDefinition = "DATETIME", nullable = false)
    private val dateCreated: LocalDateTime,

    @Column(name = "cause", columnDefinition = "VARCHAR(500)")
    private val cause: String? = null
) : Validatable {

    init {
        validate()
    }

}