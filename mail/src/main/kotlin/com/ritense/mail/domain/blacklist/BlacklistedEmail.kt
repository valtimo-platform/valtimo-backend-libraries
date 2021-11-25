package com.ritense.mail.domain.blacklist

import com.ritense.valtimo.contract.validation.Validatable
import org.hibernate.validator.constraints.Length
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table
import javax.validation.constraints.NotBlank

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

}