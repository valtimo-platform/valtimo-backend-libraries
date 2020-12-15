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

package com.ritense.audit.service.impl;

import com.ritense.audit.AbstractTestHelper;
import com.ritense.audit.domain.AuditRecord;
import com.ritense.audit.domain.AuditRecordId;
import com.ritense.audit.domain.MetaData;
import com.ritense.audit.domain.event.TestEvent;
import com.ritense.audit.exception.AuditRecordNotFoundException;
import com.ritense.audit.repository.impl.AuditRecordImplRepository;
import com.ritense.audit.service.AuditService;
import com.ritense.valtimo.contract.audit.AuditEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AuditServiceImplTest extends AbstractTestHelper {

    private AuditService auditService;
    private AuditRecordImplRepository auditRecordRepository;

    @BeforeEach
    public void setUp() {
        auditRecordRepository = mock(AuditRecordImplRepository.class);
        auditService = new AuditServiceImpl(auditRecordRepository);
    }

    @Test
    public void shouldReturnCheckedExceptionWhenNotFound() {
        final AuditEvent event = testEvent(LocalDateTime.now());
        final AuditRecordId auditRecordId = AuditRecordId.existingId(event.getId());
        when(auditRecordRepository.findById(auditRecordId)).thenReturn(Optional.empty());

        assertThrows(AuditRecordNotFoundException.class, () -> {
            auditService.findById(auditRecordId);
        });
    }

    @Test
    public void shouldFindById() {
        final AuditEvent event = testEvent(LocalDateTime.now());

        final MetaData metaData = metaData(event);
        final AuditRecord auditRecord = auditRecord(event, metaData);

        final AuditRecordId auditRecordId = AuditRecordId.existingId(event.getId());
        when(auditRecordRepository.findById(auditRecordId)).thenReturn(Optional.of(auditRecord));
        final AuditRecord foundAuditRecord = auditService.findById(auditRecordId);

        assertNotNull(foundAuditRecord);
        assertEquals(foundAuditRecord, auditRecord);
    }

    @Test
    public void shouldFindByEventAndOccurredBetween() {
        final LocalDateTime now = LocalDateTime.now();
        final TestEvent testEvent = testEvent(now, "John Doe");
        final MetaData metaData = metaData(testEvent);
        final AuditRecord auditRecord = auditRecord(testEvent, metaData);

        final Class event = testEvent.getClass();
        final LocalDateTime from = now.minusDays(2);
        final LocalDateTime until = now.plusDays(2);
        final Pageable unpaged = Pageable.unpaged();
        when(auditRecordRepository.findByEventAndOccurredBetween(event.getName(), from, until, unpaged)).thenReturn(List.of(auditRecord));

        final List<AuditRecord> auditRecords = auditService.findByEventAndOccurredBetween(TestEvent.class, from, until, unpaged);
        assertNotNull(auditRecords);
        assertThat(auditRecords, hasSize(1));
        assertThat(auditRecords, hasItem(auditRecord));
    }

    @Test
    public void shouldFindByPropertyWithSingleResult() {
        final String nameValue = "John Doe";
        final AuditRecord auditRecord = generateTestAuditRecord(testEvent(LocalDateTime.now(), nameValue));
        final Pageable pageable = Pageable.unpaged();

        when(auditRecordRepository.findAuditRecordsByProperty("name", nameValue, pageable)).thenReturn(new PageImpl<>(List.of(auditRecord), pageable, 1));

        final Page<AuditRecord> foundAuditRecords = auditService.findByProperty("name", nameValue, pageable);

        assertNotNull(foundAuditRecords);
        assertEquals(1, foundAuditRecords.getContent().size());
        assertEquals(auditRecord, foundAuditRecords.getContent().get(0));
    }

    @Test
    public void shouldFindByPropertyWithMultipleResults() {
        final String nameValue = "John Doe";
        final AuditRecord auditRecord1 = generateTestAuditRecord(testEvent(LocalDateTime.now(), nameValue));
        final AuditRecord auditRecord2 = generateTestAuditRecord(testEvent(LocalDateTime.now(), nameValue));
        final Pageable pageable = Pageable.unpaged();

        when(auditRecordRepository.findAuditRecordsByProperty("name", nameValue, pageable))
            .thenReturn(new PageImpl<>(List.of(auditRecord1, auditRecord2), pageable, 2));

        final Page<AuditRecord> foundAuditRecords = auditService.findByProperty("name", nameValue, pageable);

        assertNotNull(foundAuditRecords);
        assertEquals(2, foundAuditRecords.getContent().size());
        assertEquals(auditRecord1, foundAuditRecords.getContent().get(0));
        assertEquals(auditRecord2, foundAuditRecords.getContent().get(1));
    }

    @Test
    public void shouldFindByPropertyWithNoResults() {
        final String nameValue = "John Doe";
        final Pageable pageable = Pageable.unpaged();

        when(auditRecordRepository.findAuditRecordsByProperty("name", nameValue, pageable))
            .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        final Page<AuditRecord> auditRecords = auditService.findByProperty("name", nameValue, pageable);

        assertNotNull(auditRecords);
        assertEquals(0, auditRecords.getContent().size());
    }

    @Test
    public void shouldFindByEventAndPropertyWithResults() {
        final String nameValue = "John Doe";
        final AuditRecord auditRecord = generateTestAuditRecord(testEvent(LocalDateTime.now(), nameValue));
        when(
            auditRecordRepository.findAuditRecordsByEventAndProperty(
                TestEvent.class.getName(),
                "name",
                nameValue
            )
        ).thenReturn(List.of(auditRecord));

        final List<AuditRecord> auditRecords = auditService.findByEventTypeAndProperty(TestEvent.class, "name", nameValue);

        assertNotNull(auditRecords);
        assertEquals(1, auditRecords.size());
    }

    private AuditRecord generateTestAuditRecord(AuditEvent auditEvent) {
        MetaData metaData = metaData(auditEvent);
        return auditRecord(auditEvent, metaData);
    }

}