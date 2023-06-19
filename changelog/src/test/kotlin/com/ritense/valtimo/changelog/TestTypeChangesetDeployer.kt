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

package com.ritense.valtimo.changelog

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ritense.valtimo.changelog.domain.ChangesetDeployer
import com.ritense.valtimo.changelog.domain.ChangesetDetails

class TestTypeChangesetDeployer : ChangesetDeployer {
    override fun getPath() = "classpath*:**/*.testtype.json"

    override fun getChangelogDetails(filename: String, content: String): List<ChangesetDetails> {
        val jsonNode = jacksonObjectMapper().readTree(content)
        return listOf(
            ChangesetDetails(
                changesetId = jsonNode.get("changesetId").textValue(),
                valueToChecksum = jsonNode.get("testContent"),
                deploy = {}
            )
        )
    }
}