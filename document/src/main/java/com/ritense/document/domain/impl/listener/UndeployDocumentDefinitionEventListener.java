/*
 * Copyright 2015-2020 Ritense BV, the Netherlands.
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

package com.ritense.document.domain.impl.listener;

import com.ritense.document.service.DocumentSnapshotService;
import com.ritense.valtimo.contract.event.UndeployDocumentDefinitionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@RequiredArgsConstructor
public class UndeployDocumentDefinitionEventListener {

    private final DocumentSnapshotService documentSnapshotService;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleEvent(UndeployDocumentDefinitionEvent event) throws Exception {
        final String documentDefinitionName = event.getDocumentDefinitionName();
        logger.debug("Undeployed document definition with name: {}. remove all snapshots", event.getDocumentDefinitionName());
        documentSnapshotService.deleteSnapshotsBy(documentDefinitionName);
    }
}
