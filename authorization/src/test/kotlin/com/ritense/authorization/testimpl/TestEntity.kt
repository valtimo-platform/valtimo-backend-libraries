/*
 * Copyright 2015-2024 Ritense BV, the Netherlands.
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

package com.ritense.authorization.testimpl

import io.hypersistence.utils.hibernate.type.json.JsonType
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.Table
import java.util.UUID
import org.hibernate.annotations.Type

@Entity
@Table(name = "test_entity")
data class TestEntity(

    @Type(value = JsonType::class)
    @Column(name = "child", columnDefinition = "json")
    val child: TestChildEntity? = null,

    @Column(name = "name", columnDefinition = "varchar(100)")
    val name: String = "test",

    @ElementCollection
    @CollectionTable(
        name = "fruit",
        joinColumns = [JoinColumn(name = "test_entity_id", referencedColumnName = "id")]
    )
    @Column(name = "name")
    val fruits: MutableList<String?> = mutableListOf(),

    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(name = "some_number", columnDefinition = "int")
    val someNumber: Int? = null
)