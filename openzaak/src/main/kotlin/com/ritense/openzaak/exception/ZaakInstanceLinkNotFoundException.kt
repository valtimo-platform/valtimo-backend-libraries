package com.ritense.openzaak.exception

import org.zalando.problem.AbstractThrowableProblem
import org.zalando.problem.Exceptional
import org.zalando.problem.Status

class ZaakInstanceLinkNotFoundException(message: String?) : AbstractThrowableProblem
    (
    null,
    message,
    Status.NOT_FOUND
) {
    override fun getCause(): Exceptional? {
        return null
    }
}