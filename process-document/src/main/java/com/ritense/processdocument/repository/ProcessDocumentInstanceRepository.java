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

package com.ritense.processdocument.repository;

import com.ritense.document.domain.Document;
import com.ritense.processdocument.domain.ProcessDocumentInstanceId;
import com.ritense.processdocument.domain.ProcessInstanceId;
import com.ritense.processdocument.domain.impl.CamundaProcessJsonSchemaDocumentInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProcessDocumentInstanceRepository extends JpaRepository<CamundaProcessJsonSchemaDocumentInstance, ProcessDocumentInstanceId> {

    @Query(" SELECT  pdi " +
        "    FROM    CamundaProcessJsonSchemaDocumentInstance pdi " +
        "    WHERE   pdi.processDocumentInstanceId.documentId = :documentId ")
    List<CamundaProcessJsonSchemaDocumentInstance> findAllByDocumentId(@Param("documentId") Document.Id documentId);

    @Modifying
    @Query(" DELETE " +
        "    FROM    CamundaProcessJsonSchemaDocumentInstance pdi " +
        "    WHERE   pdi.processName = :processName ")
    void deleteAllByProcessName(@Param("processName") String processName);


    @Query(" SELECT  pdi " +
        "    FROM    CamundaProcessJsonSchemaDocumentInstance pdi " +
        "    WHERE   pdi.processDocumentInstanceId.processInstanceId = :processInstanceId ")
    Optional<CamundaProcessJsonSchemaDocumentInstance> findByProcessInstanceId(@Param("processInstanceId") ProcessInstanceId processInstanceId);

}