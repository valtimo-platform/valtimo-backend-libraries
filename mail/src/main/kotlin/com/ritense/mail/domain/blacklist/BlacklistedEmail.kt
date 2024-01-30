package com.ritense.mail.domain.blacklist

import com.ritense.valtimo.contract.validation.Validatable
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.validation.constraints.NotBlank
import org.hibernate.validator.constraints.Length
import java.time.LocalDateTime

@Entity
@Table(name = "blacklisted_email")
data class BlacklistedEmail(
    @Id
    @Column(name = "email", columnDefinition = "VARCHAR(500)")
    @field:Length(max = 500)
    @field:NotBlank
    val emailAddress: String,

    @Column(name = "date_created", columnDefinition = "DATETIME", nullable = false)
    val dateCreated: LocalDateTime,

    @Column(name = "cause", columnDefinition = "VARCHAR(500)")
    val cause: String? = null
) : Validatable {

    init {
        validate()
    }

    constructor(emailAddress: String, cause: String?) : this(emailAddress, LocalDateTime.now(), cause)
}