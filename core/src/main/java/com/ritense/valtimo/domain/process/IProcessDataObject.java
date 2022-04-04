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

package com.ritense.valtimo.domain.process;

import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.springframework.data.repository.CrudRepository;
import java.io.Serializable;

/**
 * Implement this interface to an domain/entity you want to link to a specific process instance.
 * When implemented, you can use {@link com.ritense.valtimo.service.ProcessDataObjectService#bind(IProcessDataObject, ProcessInstance)} to
 * bind an entity to a process.
 * To query the relations between entities that implemented this
 * class use {@link com.ritense.valtimo.service.ProcessDataObjectService#findAllObjectsByProcessInstance(String)}
 * and {@link com.ritense.valtimo.service.ProcessDataObjectService#findAllProcessInstancesByObject(Class, Serializable)}.
 *
 * @author Ivar Koreman
 */
public interface IProcessDataObject {

    /**
     * This method should return the identifier of the entity.
     * (Most of the times this is just a copy of the getId() function.)
     *
     * @return The ID of the object that it implements.
     */
    Serializable getIdentifier();

    /**
     * The implementation of this method should return a String representation of the ID of the object that it implements.
     * This String should be reversible back to the original type of the identifier with use of the {@link #convertToIdentifierType(String)}.
     *
     * @return A String representation of the ID of the object that it implements.
     */
    String convertIdentifierToString(Serializable identifier);

    /**
     * The implementation of this method should convert a String
     * (created with the {@link #convertIdentifierToString(Serializable)})
     * back to the original type of the identifier.
     *
     * @param identifier A string representation of the ID of the object that it implements.
     * @return The id with the original type of the identifier of the entity
     */
    Serializable convertToIdentifierType(String identifier);

    /**
     * The implementation of this method should return the {@link CrudRepository} class that is used to query this entity.
     *
     * @return The {@link CrudRepository} class that is used to query this entity.
     */
    Class<? extends CrudRepository> getRepositoryClass();
}
