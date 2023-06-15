/*
 *  Copyright 2015-2023 Ritense BV, the Netherlands.
 *
 *  Licensed under EUPL, Version 1.2 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" basis,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.ritense.valtimo.importchangelog

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ritense.valtimo.contract.importchangelog.ChangesetDeployer
import com.ritense.valtimo.contract.importchangelog.ChangesetDetails
import org.springframework.stereotype.Component

@Component
class TestTypeChangesetDeployer : ChangesetDeployer {
    override fun getPath() = "classpath*:**/*.testtype.json"

    override fun getChangesetDetails(filename: String, content: String): ChangesetDetails {
        val jsonNode = jacksonObjectMapper().readTree(content)
        return ChangesetDetails(
            jsonNode.get("changesetId").textValue(),
            jsonNode.get("testContent")
        )
    }

    override fun deploy(content: String) {
    }
}