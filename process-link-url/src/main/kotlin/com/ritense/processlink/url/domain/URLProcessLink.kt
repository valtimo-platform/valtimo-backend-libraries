package com.ritense.processlink.url.domain

import com.ritense.processlink.domain.ActivityTypeWithEventName
import com.ritense.processlink.domain.ProcessLink
import com.ritense.processlink.url.mapper.URLProcessLinkMapper
import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import java.util.UUID

@Entity
@DiscriminatorValue(URLProcessLinkMapper.PROCESS_LINK_TYPE_URL)
class URLProcessLink(
    id: UUID,
    processDefinitionId: String,
    activityId: String,
    activityType: ActivityTypeWithEventName,
    @Column(name = "url")
    val url: String
) : ProcessLink(
    id,
    processDefinitionId,
    activityId,
    activityType,
    URLProcessLinkMapper.PROCESS_LINK_TYPE_URL
) {

    override fun copy(id: UUID, processDefinitionId: String) =
        copy(
            id = id,
            processDefinitionId = processDefinitionId,
            activityId = activityId
        )

    fun copy(
        id: UUID = this.id,
        processDefinitionId: String = this.processDefinitionId,
        activityId: String = this.activityId,
        activityType: ActivityTypeWithEventName = this.activityType,
        url: String = this.url
    ) = URLProcessLink(
        id = id,
        processDefinitionId = processDefinitionId,
        activityId = activityId,
        activityType = activityType,
        url = url,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as URLProcessLink

        return url == other.url
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + url.hashCode()
        return result
    }
}