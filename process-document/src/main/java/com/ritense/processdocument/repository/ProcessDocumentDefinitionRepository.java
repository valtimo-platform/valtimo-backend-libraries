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

package com.ritense.processdocument.repository;

import com.ritense.processdocument.domain.ProcessDefinitionKey;
import com.ritense.processdocument.domain.ProcessDocumentDefinitionId;
import com.ritense.processdocument.domain.impl.CamundaProcessJsonSchemaDocumentDefinition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProcessDocumentDefinitionRepository extends
    JpaRepository<CamundaProcessJsonSchemaDocumentDefinition, ProcessDocumentDefinitionId> {

    @Query("" +
        "SELECT  pdd " +
        "FROM    CamundaProcessJsonSchemaDocumentDefinition pdd " +
        "WHERE   pdd.processDocumentDefinitionId.documentDefinitionId.version = ( " +
        "   SELECT  MAX(dd.id.version) " +
        "   FROM    JsonSchemaDocumentDefinition dd " +
        "   WHERE   dd.id.name = pdd.id.documentDefinitionId.name " +
        ")")
    Page<CamundaProcessJsonSchemaDocumentDefinition> findAllByLatestDocumentDefinitionVersion(Pageable pageable);

    @Query("" +
        "SELECT  pdd " +
        "FROM    CamundaProcessJsonSchemaDocumentDefinition pdd " +
        "WHERE   pdd.processDocumentDefinitionId.documentDefinitionId.name = :documentDefinitionName " +
        "AND     pdd.processDocumentDefinitionId.documentDefinitionId.version = ( " +
        "   SELECT  MAX(dd.id.version) " +
        "   FROM    JsonSchemaDocumentDefinition dd " +
        "   WHERE   dd.id.name = pdd.id.documentDefinitionId.name " +
        ")")
    List<CamundaProcessJsonSchemaDocumentDefinition> findAllByDocumentDefinitionNameAndLatestDocumentDefinitionVersion(
        @Param("documentDefinitionName") String documentDefinitionName
    );

    @Query("" +
        "SELECT  pdd " +
        "FROM    CamundaProcessJsonSchemaDocumentDefinition pdd " +
        "WHERE   pdd.processDocumentDefinitionId.documentDefinitionId.name = :documentDefinitionName"
    )
    Optional<CamundaProcessJsonSchemaDocumentDefinition> findByDocumentDefinitionName(
        @Param("documentDefinitionName") String documentDefinitionName
    );

    @Query("" +
        "SELECT  pdd " +
        "FROM    CamundaProcessJsonSchemaDocumentDefinition pdd " +
        "WHERE   pdd.processDocumentDefinitionId.processDefinitionKey = :processDefinitionKey " +
        "AND     pdd.processDocumentDefinitionId.documentDefinitionId.version = ( " +
        "   SELECT  MAX(dd.id.version) " +
        "   FROM    JsonSchemaDocumentDefinition dd " +
        "   WHERE   dd.id.name = pdd.id.documentDefinitionId.name " +
        ")")
    Optional<CamundaProcessJsonSchemaDocumentDefinition> findByProcessDefinitionKeyAndLatestDocumentDefinitionVersion(
        @Param("processDefinitionKey") ProcessDefinitionKey processDefinitionKey
    );

    @Query("" +
        "SELECT  pdd " +
        "FROM    CamundaProcessJsonSchemaDocumentDefinition pdd " +
        "WHERE   pdd.processDocumentDefinitionId.processDefinitionKey = :processDefinitionKey " +
        "AND     pdd.processDocumentDefinitionId.documentDefinitionId.version = ( " +
        "   SELECT MAX(dd.id.version) " +
        "   FROM   JsonSchemaDocumentDefinition dd " +
        "   WHERE  dd.id.name = pdd.id.documentDefinitionId.name " +
        ")")
    List<CamundaProcessJsonSchemaDocumentDefinition> findAllByProcessDefinitionKeyAndLatestDocumentDefinitionVersion(
        @Param("processDefinitionKey") ProcessDefinitionKey processDefinitionKey
    );

    @Query("" +
        "SELECT  pdd " +
        "FROM    CamundaProcessJsonSchemaDocumentDefinition pdd " +
        "WHERE   pdd.processDocumentDefinitionId.processDefinitionKey = :processDefinitionKey " +
        "AND     pdd.processDocumentDefinitionId.documentDefinitionId.version = :documentDefinitionVersion ")
    Optional<CamundaProcessJsonSchemaDocumentDefinition> findByProcessDefinitionKeyAndDocumentDefinitionVersion(
        @Param("processDefinitionKey") ProcessDefinitionKey processDefinitionKey,
        @Param("documentDefinitionVersion") long documentDefinitionVersion
    );

    @Modifying
    @Query("" +
        "   DELETE " +
        "   FROM    CamundaProcessJsonSchemaDocumentDefinition pdd " +
        "   WHERE   pdd.processDocumentDefinitionId.documentDefinitionId.name = :documentDefinitionName")
    void deleteByDocumentDefinition(@Param("documentDefinitionName") String documentDefinitionName);

}