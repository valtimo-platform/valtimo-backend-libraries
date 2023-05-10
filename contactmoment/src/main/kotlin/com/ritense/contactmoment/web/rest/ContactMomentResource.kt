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

package com.ritense.contactmoment.web.rest

import com.ritense.contactmoment.domain.ContactMoment
import com.ritense.contactmoment.domain.Kanaal
import com.ritense.contactmoment.web.rest.request.CreateContactMomentRequest
import com.ritense.valtimo.contract.domain.ValtimoMediaType.APPLICATION_JSON_UTF8_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
@RequestMapping("/api", produces = [APPLICATION_JSON_UTF8_VALUE])
interface ContactMomentResource {

    @GetMapping("/v1/contactmoment")
    fun getContactMomenten(): ResponseEntity<List<ContactMoment>>

    @PostMapping("/v1/contactmoment")
    fun createContactMomenten(@Valid @RequestBody request: CreateContactMomentRequest): ResponseEntity<ContactMoment>

    @GetMapping("/v1/contactmoment/kanaal")
    fun getKanalen(): ResponseEntity<Array<Kanaal>>

}
