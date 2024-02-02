package com.ritense.zakenapi.provider

import com.ritense.plugin.service.PluginService
import com.ritense.processdocument.service.ProcessDocumentService
import com.ritense.zakenapi.link.ZaakInstanceLinkService
import org.assertj.core.api.Assertions
import org.camunda.community.mockito.delegate.DelegateTaskFake
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

class ZaakBsnProviderTest {

    lateinit var processDocumentService: ProcessDocumentService
    lateinit var zaakInstanceLinkService: ZaakInstanceLinkService
    lateinit var pluginService: PluginService
    lateinit var zaakBsnProvider: ZaakBsnProvider

    @BeforeEach
    fun setUp() {
        processDocumentService = mock()
        zaakInstanceLinkService = mock()
        pluginService = mock()
        zaakBsnProvider = ZaakBsnProvider(
            processDocumentService,
            zaakInstanceLinkService,
            pluginService
        )
    }

    @Test
    fun `should get bsn via zaak rollen`() {
        val task = DelegateTaskFake()
        val bsn = zaakBsnProvider.getBurgerServiceNummer(task)

        Assertions.assertThat(bsn).isEqualTo("12345")
    }
}