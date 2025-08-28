/*
 * Copyright 2015-2025 Ritense BV, the Netherlands.
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

package com.ritense.case_.widget.divider

import com.ritense.case_.domain.tab.CaseWidgetTabWidget
import com.ritense.case_.domain.tab.CaseWidgetTabWidgetId
import com.ritense.valtimo.contract.annotation.AllOpen
import com.ritense.valtimo.contract.conditions.Condition
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity

@AllOpen
@Entity
@DiscriminatorValue("divider")
class DividerCaseWidget(
    id: CaseWidgetTabWidgetId,
    title: String,
    order: Int,
    width: Int,
    highContrast: Boolean,
    useConditionsToDisplay: Boolean,
    displayConditions: List<Condition<*>>,
) : CaseWidgetTabWidget(
    id, title, order, width, highContrast, emptyList(), useConditionsToDisplay, displayConditions
) {
    override fun copy(id: CaseWidgetTabWidgetId) = DividerCaseWidget(
        id = id,
        title = title,
        order = order,
        width = width,
        highContrast = highContrast,
        useConditionsToDisplay = useConditionsToDisplay,
        displayConditions = displayConditions,
    )
}