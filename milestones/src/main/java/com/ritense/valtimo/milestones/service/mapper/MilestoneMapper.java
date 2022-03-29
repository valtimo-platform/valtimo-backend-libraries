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

package com.ritense.valtimo.milestones.service.mapper;

import com.ritense.valtimo.milestones.domain.Milestone;
import com.ritense.valtimo.milestones.web.rest.dto.MilestoneDTO;
import org.mapstruct.Mapper;
import java.util.List;

/**
 * Mapper for the entity Milestone and its DTO MilestoneDTO.
 */
@Mapper(componentModel = "spring")
public interface MilestoneMapper {

    MilestoneDTO milestoneToMilestoneDTO(Milestone milestone);

    Milestone milestoneDTOToMilestone(MilestoneDTO milestoneDTO);

    List<MilestoneDTO> milestonesToMilestoneDtos(List<Milestone> milestones);

    /**
     * generating the fromId for all mappers if the databaseType is sql, as the class has relationship to it might need it,
     * instead of creating a new attribute to know if the entity has any relationship from some other entity.
     *
     * @param id id of the entity
     * @return the entity instance
     */
    default Milestone milestoneFromId(Long id) {
        if (id == null) {
            return null;
        }
        Milestone milestone = new Milestone();
        milestone.setId(id);
        return milestone;
    }

}
