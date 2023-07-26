package com.ritense.valtimo.actuator.health

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.actuate.health.Status

class ValtimoHealthAggregatorTest {
    lateinit var aggregator: ValtimoHealthAggregator

    @BeforeEach
    fun beforeEach() {
        aggregator = ValtimoHealthAggregator()
    }

    @Test
    fun `should return up when all components are up`(){
        val statusSet = HashSet<Status>()
        statusSet.add(Status.UP)
        statusSet.add(Status.UP)
        statusSet.add(Status.UP)

        Assertions.assertEquals(Status.UP, aggregator.getAggregateStatus(statusSet))
    }

    @Test
    fun `should return unknown when one of the components is unknown`(){
        val statusSet = HashSet<Status>()
        statusSet.add(Status.UP)
        statusSet.add(Status.UP)
        statusSet.add(Status.UNKNOWN)

        Assertions.assertEquals(Status.UNKNOWN, aggregator.getAggregateStatus(statusSet))
    }

    @Test
    fun `should return down when one of the components is down`(){
        val statusSet = HashSet<Status>()
        statusSet.add(Status.UP)
        statusSet.add(Status.UNKNOWN)
        statusSet.add(Status.DOWN)

        Assertions.assertEquals(Status.DOWN, aggregator.getAggregateStatus(statusSet))
    }

    @Test
    fun `should return out of service when one of the components is out of service`(){
        val statusSet = HashSet<Status>()
        statusSet.add(Status.UP)
        statusSet.add(Status.UNKNOWN)
        statusSet.add(Status.OUT_OF_SERVICE)

        Assertions.assertEquals(Status.OUT_OF_SERVICE, aggregator.getAggregateStatus(statusSet))
    }
}