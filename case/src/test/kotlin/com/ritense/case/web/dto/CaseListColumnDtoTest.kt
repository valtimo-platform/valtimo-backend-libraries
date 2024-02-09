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

package com.ritense.case.web.dto

import com.fasterxml.jackson.databind.jsontype.NamedType
import com.fasterxml.jackson.module.kotlin.readValue
import com.ritense.case.domain.DateFormatDisplayTypeParameter
import com.ritense.case.domain.DisplayType
import com.ritense.case.web.rest.dto.CaseListColumnDto
import com.ritense.valtimo.contract.json.MapperSingleton
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode

class CaseListColumnDtoTest {

    @Test
    fun `should serialize with empty fields`() {
        val dto = createDTOWithEmptyFields()

        val json = MAPPER.writeValueAsString(dto)
        JSONAssert.assertEquals(
            JSON_WITH_EMPTY_FIELDS,
            json,
            JSONCompareMode.NON_EXTENSIBLE
        )
    }

    @Test
    fun `should deserialize with empty fields`() {
        val result:CaseListColumnDto = MAPPER.readValue(JSON_WITH_EMPTY_FIELDS)
        val expected = createDTOWithEmptyFields()

        Assertions.assertThat(result).isEqualTo(expected)
    }

    fun createDTOWithEmptyFields(): CaseListColumnDto {
        return CaseListColumnDto(
            key = "key",
            path = "path",
            displayType = DisplayType(
                type = "date",
                displayTypeParameters = DateFormatDisplayTypeParameter(null)
            ),
            sortable = true,
            defaultSort = null,
            order = null,
            title = null
        )
    }

    companion object {
        private val MAPPER = MapperSingleton.get().copy().apply {
            registerSubtypes(NamedType(DateFormatDisplayTypeParameter::class.java, "date"))
        }
        private const val JSON_WITH_EMPTY_FIELDS = """
            {
              "key" : "key",
              "path" : "path",
              "displayType" : {
                "type" : "date",
                "displayTypeParameters" : {
                  "dateFormat" : null
                }
              },
              "sortable" : true
            }
        """
    }
}