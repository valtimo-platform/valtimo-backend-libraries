package com.ritense.valtimo.actuator.health

import org.springframework.boot.actuate.health.Status
import org.springframework.boot.actuate.health.StatusAggregator

class ValtimoHealthAggregator : StatusAggregator {
    override fun getAggregateStatus(statuses: MutableSet<Status>): Status {
        if(statuses.stream().allMatch { s -> s.equals(Status.UP) }) return Status.UP
        if(statuses.stream().anyMatch { s -> s.equals(Status.DOWN) }) return Status.DOWN
        if(statuses.stream().anyMatch { s -> s.equals(Status.OUT_OF_SERVICE) }) return Status.OUT_OF_SERVICE
        return Status.UNKNOWN
    }
}