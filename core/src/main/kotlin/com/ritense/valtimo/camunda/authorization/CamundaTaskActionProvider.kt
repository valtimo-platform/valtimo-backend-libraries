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

package com.ritense.valtimo.camunda.authorization

import com.ritense.authorization.Action
import com.ritense.authorization.ResourceActionProvider
import com.ritense.valtimo.camunda.domain.CamundaTask

class CamundaTaskActionProvider : ResourceActionProvider<CamundaTask> {
    override fun getAvailableActions(): List<Action<CamundaTask>> {
        return listOf(ASSIGN, ASSIGNABLE, CLAIM, COMPLETE, VIEW_LIST, VIEW)
    }

    companion object {
        @JvmField
        val ASSIGN = Action<CamundaTask>(Action.ASSIGN)

        @JvmField
        val ASSIGNABLE = Action<CamundaTask>(Action.ASSIGNABLE)

        @JvmField
        val CLAIM = Action<CamundaTask>(Action.CLAIM)

        @JvmField
        val COMPLETE = Action<CamundaTask>(Action.COMPLETE)

        @JvmField
        val VIEW_LIST = Action<CamundaTask>(Action.VIEW_LIST)

        @JvmField
        val VIEW = Action<CamundaTask>(Action.VIEW)
    }
}
