/*
 * Copyright 2015-2020 Ritense BV, the Netherlands.
 *
 * Licensed under EUPL, Version 1.2 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ritense.document.export.domain

import com.ritense.valtimo.contract.utils.SecurityUtils
import com.ritense.valtimo.contract.validation.Validatable
import org.hibernate.validator.constraints.Length
import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Embeddable
import javax.validation.constraints.NotBlank

@Embeddable
class UserId(

    @Column(name = "user_id", columnDefinition = "VARCHAR(255)", nullable = false)
    @field:Length(max = 255)
    @field:NotBlank
    private val userId: String

) : Serializable, Validatable {

    init {
        validate()
    }

    companion object {

        fun fromAuthentication(): UserId {
            return UserId((SecurityUtils.getCurrentUserAuthentication().name))
        }

    }

}