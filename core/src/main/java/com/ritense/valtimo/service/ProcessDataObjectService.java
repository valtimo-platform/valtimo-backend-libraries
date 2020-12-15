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

package com.ritense.valtimo.service;

import com.google.common.collect.Lists;
import com.ritense.valtimo.domain.process.IProcessDataObject;
import com.ritense.valtimo.domain.process.ProcessDataObjectRelation;
import com.ritense.valtimo.exception.ProcessDataObjectBindException;
import com.ritense.valtimo.repository.ProcessDataObjectRelationRepository;
import com.ritense.valtimo.repository.utils.QueryUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.camunda.bpm.engine.impl.Direction;
import org.camunda.bpm.engine.impl.HistoricProcessInstanceQueryProperty;
import org.camunda.bpm.engine.impl.QueryOrderingProperty;
import org.camunda.bpm.engine.impl.db.ListQueryParameterObject;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.orm.jpa.JpaSystemException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This service is used to manage the relation between entities and process instances.
 * It is both used for binding and querying the relations.
 *
 * @author Ivar Koreman
 */
public class ProcessDataObjectService {

    private final ProcessDataObjectRelationRepository processDataObjectRelationRepository;
    private final ApplicationContext applicationContext;
    private final SqlSessionFactory camundaSqlSessionFactory;

    public ProcessDataObjectService(
        final ProcessDataObjectRelationRepository processDataObjectRelationRepository,
        final ApplicationContext applicationContext,
        final SqlSessionFactory camundaSqlSessionFactory
    ) {
        this.processDataObjectRelationRepository = processDataObjectRelationRepository;
        this.applicationContext = applicationContext;
        this.camundaSqlSessionFactory = camundaSqlSessionFactory;
    }

    /**
     * Binds the processDataObject to the processInstance so the relation between the two is registered.
     *
     * @param processDataObject The entity implementing {@link IProcessDataObject}.
     * @param processInstance   The process instance the entity should be related to.
     * @throws IllegalArgumentException       When either parameter (or the id of the processDataObject) is null or empty.
     * @throws ProcessDataObjectBindException When the {@link IProcessDataObject#convertIdentifierToString(Serializable)} returns an empty string.
     */
    public void bind(IProcessDataObject processDataObject, ProcessInstance processInstance) throws IllegalArgumentException {
        bind(processDataObject, processInstance.getProcessInstanceId());
    }

    /**
     * Binds the processDataObject to the processInstance so the relation between the two is registered.
     *
     * @param processDataObject The entity implementing {@link IProcessDataObject}.
     * @param processInstanceId The id of the process instance the entity should be related to.
     * @throws IllegalArgumentException       When either parameter (or the id of the processDataObject) is null or empty.
     * @throws ProcessDataObjectBindException When the {@link IProcessDataObject#convertIdentifierToString(Serializable)} returns an empty string.
     */
    public void bind(IProcessDataObject processDataObject, String processInstanceId) throws IllegalArgumentException {
        if (processDataObject == null
            || processDataObject.getIdentifier() == null
            || processInstanceId == null
            || processInstanceId.isEmpty()) {
            throw new IllegalArgumentException("The (identifier of) processDataObject or processInstanceId is null or empty");
        }

        String objIdAsString = processDataObject.convertIdentifierToString(
            processDataObject.getIdentifier()
        );

        if (objIdAsString.isEmpty()) {
            throw new ProcessDataObjectBindException(
                "The processDataObject.convertIdentifierToString(Serializable) returned an empty string and therefore cannot be bound."
            );
        }

        ProcessDataObjectRelation relation = new ProcessDataObjectRelation();
        relation.setObjType(processDataObject.getClass());
        relation.setObjId(objIdAsString);
        relation.setProcessInstanceId(processInstanceId);

        boolean relationAlreadyExists = processDataObjectRelationRepository
            .existsByObjTypeAndObjIdAndProcessInstanceId(
                relation.getObjType(),
                relation.getObjId(),
                relation.getProcessInstanceId());

        if (!relationAlreadyExists) {
            processDataObjectRelationRepository.save(relation);
        }
    }

    /**
     * Returns all process instances that are related to an {@link IProcessDataObject} implemented entity.
     *
     * @param objType The class of which type you want to query the process instances. (e.g. S3Resource.class)
     * @param objId   The id of the domain.
     * @return A {@link List} of process instances matching the criteria.
     * @throws ReflectiveOperationException if it is not possible to create an instance of the of the {@code objType} class.
     */
    public Page<com.ritense.valtimo.repository.camunda.dto.ProcessInstance> findAllProcessInstancesByObject(
        Class<? extends IProcessDataObject> objType,
        Serializable objId
    ) throws ReflectiveOperationException {
        return findAllProcessInstancesByObject(objType, objId, PageRequest.of(0, Integer.MAX_VALUE));
    }

    /**
     * Returns all process instances that are related to an {@link IProcessDataObject} implemented entity.
     *
     * @param objType The class of which type you want to query the process instances. (e.g. S3Resource.class)
     * @param objId   The id of the domain.
     * @return A {@link List} of process instances matching the criteria.
     * @throws ReflectiveOperationException if it is not possible to create an instance of the of the {@code objType} class.
     */
    @SuppressWarnings("WeakerAccess")
    public Page<com.ritense.valtimo.repository.camunda.dto.ProcessInstance> findAllProcessInstancesByObject(
        Class<? extends IProcessDataObject> objType,
        Serializable objId,
        Pageable pageable
    ) throws ReflectiveOperationException {
        IProcessDataObject objTypeInstance = objType.getDeclaredConstructor().newInstance();
        return findAllProcessInstancesByObjectQuery(objType, objTypeInstance.convertIdentifierToString(objId), pageable);
    }

    /**
     * Returns a list of all domain objects related to a process instance.
     *
     * @return A list of all domain objects related to the process instance.
     */
    public List<IProcessDataObject> findAllObjectsByProcessInstance(String processInstanceId) {
        return findAllObjectsByProcessInstances(Collections.singletonList(processInstanceId));
    }

    /**
     * Returns a list of all domain objects related to a list of process instances.
     *
     * @return A list of all domain objects related to a list of process instances.
     */
    @SuppressWarnings("WeakerAccess")
    public List<IProcessDataObject> findAllObjectsByProcessInstances(List<String> processInstanceIds) {
        List<ProcessDataObjectRelation> relations;
        try {
            relations = processDataObjectRelationRepository.findAllByProcessInstanceIdIn(processInstanceIds);
        } catch (JpaSystemException ex) {
            throw new RuntimeException("A class found in the ProcessDataObjectService table cannot be mapped to an existing class.", ex);
        }

        return findAllObjectsByRelations(relations);
    }

    /**
     * Returns a list of the Entity class that is provided in the first parameter,
     * containing all objects that belong to the provided {@code processInstanceId}.
     *
     * @param objType           The Entity class that should be queried
     * @param processInstanceId A process instance ID that should be queried.
     * @param <T>               The Entity class that should be queried and is returned as a list
     * @return A list of the Entity class that is requested.
     */
    @SuppressWarnings("unused")
    public <T extends IProcessDataObject> List<T> findAllObjectsByObjectTypeAndProcessInstance(
        Class<T> objType,
        String processInstanceId) {
        return findAllObjectsByObjectTypeAndProcessInstances(objType, Collections.singletonList(processInstanceId));
    }

    /**
     * Returns a list of the Entity class that provided in the first parameter,
     * containing all objects that belong to any of the provided processInstanceIds.
     *
     * @param objType            The Entity class that should be queried
     * @param processInstanceIds A list of process instance ids that should be queried.
     * @param <T>                The Entity class that should be queried and is returned as a list
     * @return A list of the Entity class that is requested.
     */
    @SuppressWarnings("WeakerAccess")
    public <T extends IProcessDataObject> List<T> findAllObjectsByObjectTypeAndProcessInstances(
        Class<T> objType,
        List<String> processInstanceIds) {
        List<ProcessDataObjectRelation> relations =
            processDataObjectRelationRepository.findAllByObjTypeAndProcessInstanceIdIn(objType, processInstanceIds);

        return findAllObjectsByRelations(relations);
    }

    @SuppressWarnings("unchecked")
    private <T extends IProcessDataObject> List<T> findAllObjectsByRelations(List<ProcessDataObjectRelation> relations) {
        Map<Class<? extends IProcessDataObject>, List<ProcessDataObjectRelation>> groupedProcessInstances =
            relations.stream().collect(Collectors.groupingBy(ProcessDataObjectRelation::getObjType));

        List<T> returnList = new ArrayList<>();
        for (Map.Entry<
            Class<? extends IProcessDataObject>,
            List<ProcessDataObjectRelation>
            > entity : groupedProcessInstances.entrySet()) {
            try {
                IProcessDataObject pdo = entity.getKey().getDeclaredConstructor().newInstance();
                Class<? extends CrudRepository> repositoryClass = pdo.getRepositoryClass();
                CrudRepository repository = applicationContext.getBean(repositoryClass);

                List<Serializable> objIds = entity.getValue()
                    .stream()
                    .map(processDataObjectRelation ->
                        pdo.convertToIdentifierType(processDataObjectRelation.getObjId()))
                    .collect(Collectors.toList());

                // TODO: Might want to use MyBatis if that is possible.
                List<T> dataObject = (List<T>) repository.findAllById(objIds);

                returnList.addAll(dataObject);
            } catch (ReflectiveOperationException ex) {
                break;
            }
        }

        return returnList;
    }

    private Page<com.ritense.valtimo.repository.camunda.dto.ProcessInstance> findAllProcessInstancesByObjectQuery(
        Class<? extends IProcessDataObject> objType,
        String objId,
        Pageable pageable) {
        try (SqlSession session = camundaSqlSessionFactory.openSession()) {

            Map<String, Object> queryParams = new HashMap<>();
            queryParams.put("objType", objType.getName());
            queryParams.put("objId", objId);

            ListQueryParameterObject queryParameterObject = new ListQueryParameterObject();

            setPagingParams(queryParameterObject, pageable);

            queryParameterObject.setParameter(queryParams);

            List<com.ritense.valtimo.repository.camunda.dto.ProcessInstance> processInstances =
                session.selectList(
                    "com.ritense.valtimo.mapper.getProcessInstancesByProcessDataObjectObject",
                    queryParameterObject);
            Long processInstanceCount =
                session.selectOne(
                    "com.ritense.valtimo.mapper.getProcessInstancesByProcessDataObjectObjectCount",
                    queryParameterObject);

            return new PageImpl<>(processInstances, pageable, processInstanceCount);
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void setPagingParams(ListQueryParameterObject queryParameterObject, Pageable pageable) {
        String orderBy = null;
        if (pageable != null) {
            queryParameterObject.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
            queryParameterObject.setMaxResults(pageable.getPageSize());

            if (pageable.getSort() != null) {
                orderBy = QueryUtils.toOrders(pageable.getSort());
            }
        }

        if (orderBy == null) {
            // Set default to PI.ID_ because otherwise Batis asks Camunda for the default,
            // which is "RES.ID_ asc", which does not exist in the context of the query.
            // See {@link org.camunda.bpm.engine.impl.db.ListQueryParameterObject.DEFAULT_ORDER_BY}

            //TODO: kijk hier ook eens if dit verder gebruikt wordt. Het lijkt er op dat de orderby alleen gezet wordt als deze leeg is.
            queryParameterObject.setOrderingProperties(
                Lists.newArrayList(
                    new QueryOrderingProperty(HistoricProcessInstanceQueryProperty.PROCESS_INSTANCE_ID_, Direction.ASCENDING)
                )
            );
        }
    }
}
