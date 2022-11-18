package com.ritense.exact.web.rest

import com.ritense.exact.service.ExactService
import com.ritense.exact.service.request.ExactExchangeRequest
import com.ritense.exact.service.response.ExactExchangeResponse
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping(value = ["/api"], produces = [MediaType.APPLICATION_JSON_VALUE])
@RestController
class ExactResource(
    val exactService: ExactService
) {

    @PostMapping("/plugin/exact/exchange")
    fun exchange(@RequestBody request: ExactExchangeRequest): ExactExchangeResponse {
        return exactService.exchange(request)
    }

}