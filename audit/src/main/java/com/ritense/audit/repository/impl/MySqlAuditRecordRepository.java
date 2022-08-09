/*
 * Copyright 2015-2021 Ritense BV, the Netherlands.
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

package com.ritense.audit.repository.impl;

import com.ritense.audit.domain.AuditRecord;
import com.ritense.audit.domain.AuditRecordId;
import com.ritense.audit.repository.AuditRecordRepository;
import com.ritense.valtimo.contract.audit.AuditEvent;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

@NoRepositoryBean
public interface MySqlAuditRecordRepository extends AuditRecordRepository<AuditRecord, AuditRecordId> {

    @Query(" SELECT  ar " +
        "    FROM    AuditRecord ar " +
        "    WHERE   className = :className ")
    List<AuditRecord> findAuditRecordsByEvent(@Param("className") String className);

    @Query(" SELECT  ar " +
        "    FROM    AuditRecord ar " +
        "    WHERE   className = :className " +
        "    AND     JSON_EXTRACT(ar.auditEvent, CONCAT('$.',:key)) = :value ")
    List<AuditRecord> findAuditRecordsByEventAndProperty(
        @Param("className") String className,
        @Param("key") String key,
        @Param("value") Object value
    );

    @Query(" SELECT  ar " +
        "    FROM    AuditRecord ar " +
        "    WHERE   className IN (:eventTypes) " +
        "    AND     documentId = :documentId " +
        "    ORDER BY ar.metaData.occurredOn DESC")
    Page<AuditRecord> findByEventAndDocumentId(
        List<Class<? extends AuditEvent>> eventTypes,
        UUID documentId,
        Pageable pageable
    );

    @Query(" SELECT  ar " +
        "    FROM    AuditRecord ar " +
        "    WHERE   className = :className " +
        "    AND     ar.metaData.occurredOn BETWEEN :from AND :until")
    List<AuditRecord> findByEventAndOccurredBetween(
        @Param("className") String className,
        @Param("from") LocalDateTime from,
        @Param("until") LocalDateTime until,
        Pageable pageable
    );

    @Query(" SELECT      ar " +
        "    FROM        AuditRecord ar " +
        "    WHERE       JSON_EXTRACT(ar.auditEvent, CONCAT('$.',?1)) = ?2 ")
    Page<AuditRecord> findAuditRecordsByProperty(String key, Object value, Pageable pageable);

    @Modifying
    @Transactional
    @Query(" DELETE " +
        "    FROM    AuditRecord ar " +
        "    WHERE   ar.createdOn < :date")
    void deleteAllBefore(@Param("date") LocalDateTime date);

}