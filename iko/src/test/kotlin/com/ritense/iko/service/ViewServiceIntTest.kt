package com.ritense.iko.service

import com.ritense.authorization.AuthorizationContext.Companion.runWithoutAuthorization
import com.ritense.iko.BaseIntegrationTest
import com.ritense.iko.web.rest.request.CreateViewRequest
import com.ritense.iko.web.rest.response.Search
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

class ViewServiceIntTest : BaseIntegrationTest() {

    @Autowired
    private lateinit var viewService: ViewService

    @Test
    @Transactional
    fun `should create view`() {
        val request = CreateViewRequest(
            name = "Klantbeeld",
            searches = listOf(
                Search(
                    url = "https://www.iko.org/search",
                )
            )
        )

        val view = runWithoutAuthorization {
            viewService.createView(request)
        }

        assertThat(view).isNotNull
        assertThat(view.id).isNotNull
        assertThat(view.name).isEqualTo(request.name)
    }

    @Test
    @Transactional
    fun `should update view`() {
        val request = createViewRequest()

        val view = runWithoutAuthorization {
            viewService.createView(request)
        }

        assertThat(view).isNotNull
        assertThat(view.id).isNotNull
        assertThat(view.name).isEqualTo(request.name)
    }


}