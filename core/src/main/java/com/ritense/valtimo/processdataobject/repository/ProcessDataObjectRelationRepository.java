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

package com.ritense.valtimo.processdataobject.repository;

import com.ritense.valtimo.domain.process.IProcessDataObject;
import com.ritense.valtimo.domain.process.ProcessDataObjectRelation;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;

/**
 * Spring Data JPA repository for the ProcessDataObjectRelation entity.
 * Missing some? There is a MyBatis query also querying this table.
 */
@Repository
public interface ProcessDataObjectRelationRepository extends CrudRepository<ProcessDataObjectRelation, Long> {

    List<ProcessDataObjectRelation> findAllByProcessInstanceIdIn(
            List<String> processInstanceIds);

    List<ProcessDataObjectRelation> findAllByObjTypeAndProcessInstanceIdIn(
            @NotNull Class<? extends IProcessDataObject> objType,
            Collection<@NotNull String> processInstanceId);

    boolean existsByObjTypeAndObjIdAndProcessInstanceId(
            @NotNull Class<? extends IProcessDataObject> objType,
            @NotNull String objId,
            @NotNull String processInstanceId);
}
