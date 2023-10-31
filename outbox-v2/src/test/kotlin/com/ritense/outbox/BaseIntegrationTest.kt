package com.ritense.outbox

import com.fasterxml.jackson.databind.ObjectMapper
import com.ritense.outbox.service.OutboxService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension

@SpringBootTest
@ExtendWith(value = [SpringExtension::class])
@Tag("integration")
abstract class BaseIntegrationTest {

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Autowired
    lateinit var outboxService: OutboxService

    @BeforeEach
    fun cleanup() {
        /*   outboxService.getMessages().forEach {
               outboxService.deleteMessage(it.id)
           }*/
    }
}
