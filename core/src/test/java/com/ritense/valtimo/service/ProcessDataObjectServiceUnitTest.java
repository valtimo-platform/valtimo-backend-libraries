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

import com.ritense.valtimo.domain.process.IProcessDataObject;
import com.ritense.valtimo.repository.ProcessDataObjectRelationRepository;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.JpaRepository;

import java.io.Serializable;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProcessDataObjectServiceUnitTest {

    private ProcessDataObjectService processDataObjectService;

    @BeforeEach
    public void setUp() {
        SqlSessionFactory sqlSessionFactory = mock(SqlSessionFactory.class);
        SqlSession sqlSession = mock(SqlSession.class);

        when(sqlSessionFactory.openSession()).thenReturn(sqlSession);
        when(sqlSession.selectList(
            eq("com.ritense.valtimo.mapper.getProcessInstancesByProcessDataObjectObject"),
            any()))
            .thenReturn(
                Collections.emptyList());

        when(sqlSession.selectOne(
            eq("com.ritense.valtimo.mapper.getProcessInstancesByProcessDataObjectObjectCount"),
            any()))
            .thenReturn(0L);

        processDataObjectService = new ProcessDataObjectService(
            mock(ProcessDataObjectRelationRepository.class),
            mock(ApplicationContext.class),
            sqlSessionFactory
        );
    }

    @Test
    void shouldNotThrowIllegalArgumentExceptionWhenFindAllProcessInstancesByObjectIsCalled() throws ReflectiveOperationException {
        try {
            processDataObjectService.findAllProcessInstancesByObject(ExampleProcessDataObject.class, 10);
        } catch (IllegalArgumentException e) {
            Assertions.fail("processDataObjectService.findAllProcessInstancesByObject() threw an IllegalArgumentException");
        }
    }

    @Test
    void shouldNotThrowIllegalArgumentExceptionWhenFindAllObjectsByProcessInstancesIsCalled() {
        try {
            processDataObjectService.findAllObjectsByProcessInstances(Collections.singletonList("1234"));
        } catch (IllegalArgumentException e) {
            Assertions.fail("processDataObjectService.findAllObjectsByProcessInstances() threw an IllegalArgumentException");
        }
    }

    @Test
    void shouldNotThrowIllegalArgumentExceptionWhenFindAllObjectsByObjectTypeAndProcessInstanceIsCalled() {
        try {
            processDataObjectService.findAllObjectsByObjectTypeAndProcessInstance(ExampleProcessDataObject.class, "1234");
        } catch (IllegalArgumentException e) {
            Assertions.fail("processDataObjectService.findAllObjectsByObjectTypeAndProcessInstance() threw an IllegalArgumentException");
        }
    }

    @Test
    void shouldNotThrowIllegalArgumentExceptionWhenFindAllObjectsByObjectTypeAndProcessInstancesIsCalled() {
        try {
            processDataObjectService.findAllObjectsByObjectTypeAndProcessInstances(ExampleProcessDataObject.class, Collections.singletonList("1234"));
        } catch (IllegalArgumentException e) {
            Assertions.fail("processDataObjectService.findAllObjectsByObjectTypeAndProcessInstances() threw an IllegalArgumentException");
        }
    }

    public static class ExampleProcessDataObject implements IProcessDataObject {

        @Override
        public Serializable getIdentifier() {
            return null;
        }

        @Override
        public String convertIdentifierToString(Serializable identifier) {
            return null;
        }

        @Override
        public Serializable convertToIdentifierType(String identifier) {
            return null;
        }

        @Override
        public Class<? extends JpaRepository> getRepositoryClass() {
            return null;
        }
    }
}
