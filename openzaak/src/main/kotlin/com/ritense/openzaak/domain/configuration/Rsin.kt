package com.ritense.openzaak.domain.configuration

import com.fasterxml.jackson.annotation.JsonValue
import com.ritense.valtimo.contract.validation.Validatable
import org.hibernate.validator.constraints.Length
import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Embeddable
import javax.validation.constraints.NotBlank

@Embeddable
class Rsin(
    @Column(name = "rsin", columnDefinition = "CHAR(9)", nullable = false)
    @field:Length(min = 9, max = 9)
    @field:NotBlank
    @JsonValue
    val value: String
) : Serializable, Validatable {

    init {
        validate()
        require(isValid(value)) { "Invalid RSIN" }
    }

    private fun isValid(rsin: String): Boolean {
        var result = 0
        for (x in 0..8) {
            val number = Character.getNumericValue(rsin[x])
            val multiplier = if (x > 0) 9 - x else -1
            result += number * multiplier
        }
        return result % 11 == 0
    }
}