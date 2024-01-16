/*
 * Copyright 2015-2023 Ritense BV, the Netherlands.
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

package com.ritense.dashboard.domain

import com.ritense.valtimo.contract.utils.SecurityUtils
import java.time.ZonedDateTime
import jakarta.persistence.CascadeType.ALL
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType.EAGER
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.OrderBy
import jakarta.persistence.Table

@Entity
@Table(name = "dashboard")
data class Dashboard(

    @Id
    @Column(name = "`key`", updatable = false, nullable = false, unique = true)
    val key: String,

    @Column(name = "title", nullable = false)
    val title: String,

    @Column(name = "description")
    val description: String,

    @OneToMany(mappedBy = "dashboard", fetch = EAGER, cascade = [ALL], orphanRemoval = true)
    @OrderBy("order ASC")
    val widgetConfigurations: List<WidgetConfiguration> = listOf(),

    @Column(name = "sort_order", nullable = false)
    val order: Int,

    @Column(name = "created_by")
    val createdBy: String = SecurityUtils.getCurrentUserLogin(),

    @Column(name = "created_on")
    val createdOn: ZonedDateTime = ZonedDateTime.now()

)
