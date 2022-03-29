/*
 * Copyright 2015-2022 Ritense BV, the Netherlands.
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

package com.ritense.formflow.domain

import org.hibernate.validator.constraints.Length
import javax.persistence.Column
import javax.persistence.Embeddable
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.JoinColumns
import javax.persistence.ManyToOne

@Embeddable
data class FormFlowStepId(

    @Column(name = "key")
    @field:Length(max = 256)
    val key: String,

    @ManyToOne(targetEntity = FormFlowDefinition::class, fetch = FetchType.LAZY)
    @JoinColumns(
        JoinColumn(name = "form_flow_definition_key", referencedColumnName = "key"),
        JoinColumn(name = "form_flow_definition_version", referencedColumnName = "version")
    )
    val formFlowDefinitionId: FormFlowDefinitionId
) : AbstractId<FormFlowStepId>()