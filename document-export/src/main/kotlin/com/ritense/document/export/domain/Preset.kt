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

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.ritense.valtimo.contract.validation.Validatable
import org.springframework.data.domain.Persistable
import java.util.UUID
import javax.persistence.Embedded
import javax.persistence.EmbeddedId
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "preset")
data class Preset(

    @EmbeddedId
    @JsonProperty("id")
    val presetId: PresetId,

    @Embedded
    var status: Status,

    @Embedded
    var tree: Tree

) : Persistable<PresetId>, Validatable {

    init {
        validate()
    }

    /*Persistable related*/
    @JsonIgnore
    override fun getId(): PresetId {
        return presetId
    }

    @JsonIgnore
    override fun isNew(): Boolean {
        return presetId.isNew
    }

}