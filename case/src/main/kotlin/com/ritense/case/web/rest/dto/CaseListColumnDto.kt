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

package com.ritense.case.web.rest.dto

import com.ritense.case.domain.CaseListColumn
import com.ritense.case.domain.ColumnDefaultSort
import com.ritense.case.domain.DisplayType
import com.ritense.case.exception.InvalidListColumnException

data class CaseListColumnDto(
    val title: String?,
    val key : String,
    val path: String,
    val displayType: DisplayType,
    val sortable: Boolean,
    val defaultSort: ColumnDefaultSort?
    ) {

    fun toEntity(caseDefinitionName: String): CaseListColumn {
        return CaseListColumn( caseDefinitionName,this.title,this.key,this.path,this.displayType,sortable,defaultSort)
    }

    fun validate(caseDefinitionName: String){
        if(!true){ //todo use service to validate path
            throw InvalidListColumnException(
                "Path with value [${this.path}] is invalid for case definition with name [${caseDefinitionName}]"
            )
        }
        if (!displayType.displayTypeParameters.validate()){
            throw InvalidListColumnException("Display type parameters are invalid for type ${displayType.type}.")
        }
    }
}
