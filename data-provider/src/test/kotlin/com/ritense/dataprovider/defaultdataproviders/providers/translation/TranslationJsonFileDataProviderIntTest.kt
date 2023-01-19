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

package com.ritense.dataprovider.defaultdataproviders.providers.translation

import com.ritense.dataprovider.BaseIntegrationTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.context.WebApplicationContext

@Transactional
internal class TranslationJsonFileDataProviderIntTest : BaseIntegrationTest() {

    @Autowired
    lateinit var webApplicationContext: WebApplicationContext

    lateinit var mockMvc: MockMvc

    @BeforeEach
    fun beforeEach() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(webApplicationContext)
            .build()
    }

    @Test
    fun `should get all translations`() {
        mockMvc.perform(get("/api/v1/data/translation/single?key=nl"))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(content().string("""{"menu":{"title":"Titel","back":"Terug","save":"Opslaan"}}"""))
    }

    @Test
    fun `should get translations by property list`() {
        mockMvc.perform(get("/api/v1/data/translation/single?key=nl&properties=menu.back,menu.save"))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(content().string("""{"menu.back":"Terug","menu.save":"Opslaan"}"""))
    }

    @Test
    fun `should get translation by one property`() {
        mockMvc.perform(get("/api/v1/data/translation/single?key=nl&properties=menu.back"))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(content().string("""{"menu.back":"Terug"}"""))
    }

}
