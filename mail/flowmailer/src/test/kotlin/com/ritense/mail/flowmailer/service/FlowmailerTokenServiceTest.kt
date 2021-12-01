package com.ritense.mail.flowmailer.service

import com.ritense.mail.flowmailer.config.FlowmailerProperties
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.web.client.RestTemplate

@ExtendWith(MockitoExtension::class)
class FlowmailerTokenServiceTest {

    @Mock
    lateinit var flowmailerProperties: FlowmailerProperties

    @InjectMocks
    lateinit var flowmailerTokenService: FlowmailerTokenService

    @Test
    fun `should return a token`() {

    }

    @Test
    fun `should throw exception when no token is returned`() {

    }
}