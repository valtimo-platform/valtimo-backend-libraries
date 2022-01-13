/*
 * Copyright 2020 Dimpact.
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

package com.ritense.openzaak.listener

import com.ritense.openzaak.domain.event.EigenschappenSetEvent
import com.ritense.openzaak.domain.event.ResultaatSetEvent
import com.ritense.openzaak.domain.event.StatusSetEvent
import com.ritense.openzaak.service.impl.ZaakService
import org.springframework.context.event.EventListener
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import org.springframework.core.annotation.Order

@Transactional
class GlobalZaakEventListener(
    val zaakService: ZaakService
) {
    @Order(0)
    @EventListener(StatusSetEvent::class)
    fun handleSetStatus(event: StatusSetEvent) {
        zaakService.setZaakStatus(
            event.zaak,
            event.statusType,
            LocalDateTime.now()
        )
    }

    @Order(0)
    @EventListener(ResultaatSetEvent::class)
    fun handleSetResultaat(event: ResultaatSetEvent) {
        zaakService.setZaakResultaat(
            event.zaak,
            event.resultaatType
        )
    }

    @Order(0)
    @EventListener(EigenschappenSetEvent::class)
    fun handleSetEigenschappen(event: EigenschappenSetEvent) {
        zaakService.modifyEigenschap(
            event.zaakUrl,
            event.zaakId,
            event.eigenschappen
        )
    }

}