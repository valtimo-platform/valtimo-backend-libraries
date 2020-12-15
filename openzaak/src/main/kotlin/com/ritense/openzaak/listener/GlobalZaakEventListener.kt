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
import com.ritense.openzaak.domain.event.ZaakCreatedEvent
import com.ritense.openzaak.service.impl.ZaakService
import org.springframework.context.event.EventListener
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import java.time.LocalDateTime

@Transactional
class GlobalZaakEventListener(
    val zaakService: ZaakService
) {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun handleCreateZaak(event: ZaakCreatedEvent) {
        zaakService.createZaakWithLink(event.delegateExecution)
    }

    @EventListener(StatusSetEvent::class)
    fun handleSetStatus(event: StatusSetEvent) {
        zaakService.setZaakStatus(
            event.zaak,
            event.statusType,
            LocalDateTime.now()
        )
    }

    @EventListener(ResultaatSetEvent::class)
    fun handleSetResultaat(event: ResultaatSetEvent) {
        zaakService.setZaakResultaat(
            event.zaak,
            event.resultaatType
        )

    }

    @EventListener(EigenschappenSetEvent::class)
    fun handleSetEigenschappen(event: EigenschappenSetEvent) {
        zaakService.modifyEigenschap(
            event.zaakUrl,
            event.zaakId,
            event.eigenschappen
        )
    }

}