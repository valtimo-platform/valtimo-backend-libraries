package com.ritense.connector.service

import com.ritense.connector.BaseTest
import com.ritense.connector.domain.Connector
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito

internal class ConnectorFluentBuilderTest : BaseTest() {
    lateinit var connectorService: ConnectorService

    @BeforeEach
    fun setUp() {
        super.baseSetUp()
        connectorService = Mockito.mock(ConnectorService::class.java)
    }

    @Test
    fun `should create 2 unique builders`() {
        val builder1 = ConnectorFluentBuilder(connectorService).builder()
        val builder2 = ConnectorFluentBuilder(connectorService).builder()

        assertThat(builder1).isNotSameAs(builder2)
    }

    @Test
    fun `should build with connector`() {
        val connector = Mockito.mock(Connector::class.java)

        Mockito.`when`(connectorService.load(anyString())).thenReturn(connector)

        val conenctor = ConnectorFluentBuilder(connectorService)
            .builder()
            .withConnector("aName")

        assertThat(conenctor).isInstanceOf(Connector::class.java)
    }
}