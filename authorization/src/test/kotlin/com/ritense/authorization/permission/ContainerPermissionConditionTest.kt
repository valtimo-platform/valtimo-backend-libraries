package com.ritense.authorization.permission

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.ritense.authorization.AuthorizationEntityMapper
import com.ritense.authorization.AuthorizationService
import com.ritense.authorization.AuthorizationServiceHolder
import com.ritense.authorization.AuthorizationSpecification
import com.ritense.authorization.testimpl.RelatedTestEntity
import com.ritense.authorization.testimpl.TestChildEntity
import com.ritense.authorization.testimpl.TestEntity
import kotlin.test.assertEquals
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.skyscreamer.jsonassert.JSONAssert
import org.skyscreamer.jsonassert.JSONCompareMode

class ContainerPermissionConditionTest {

    lateinit var mapper: ObjectMapper
    lateinit var entity: TestEntity
    lateinit var conditionTemplate: ContainerPermissionCondition<TestEntity>
    lateinit var authorizationService: AuthorizationService

    @BeforeEach
    fun setup() {
        mapper = jacksonObjectMapper().apply {
            this.registerSubtypes(ContainerPermissionCondition::class.java)
            this.registerSubtypes(FieldPermissionCondition::class.java)
            this.registerSubtypes(ExpressionPermissionCondition::class.java)
        }
        //TODO: The entity or child objects can't be a Map, don't we want to support this?
        entity = TestEntity(
            TestChildEntity("""
            {
                "stringProperty": "myValue"
            }
        """.trimIndent())
        )

        conditionTemplate = ContainerPermissionCondition(
            resourceType = TestEntity::class.java,
            listOf()
        )

        authorizationService = mock()
        AuthorizationServiceHolder(authorizationService)
    }

    @Test
    fun `should fail when no mappers are available`() {
        whenever(authorizationService.getMapper<TestEntity, RelatedTestEntity>(any(), any()))
            .thenThrow(NoSuchElementException("nothing here"))

        assertThrows<NoSuchElementException> {
            conditionTemplate.isValid(entity)
        }
    }

    @Test
    fun `should fail when no related entities are mapped`() {
        val mapper: AuthorizationEntityMapper<TestEntity, RelatedTestEntity> = mock()
        whenever(authorizationService.getMapper<TestEntity, RelatedTestEntity>(any(), any()))
            .thenReturn(mapper)

        whenever(mapper.mapRelated(any())).thenReturn(listOf())
        val authSpec: AuthorizationSpecification<RelatedTestEntity> = mock()
        whenever(authorizationService.getAuthorizationSpecification<RelatedTestEntity>(any(), any())).thenReturn(
            authSpec
        )

        val result = conditionTemplate.isValid(entity)
        assertEquals(false, result)
        verify(authSpec, never()).isAuthorized(any())
    }

    @Test
    fun `should fail when spec for related entity returns false`() {
        val mapper: AuthorizationEntityMapper<TestEntity, RelatedTestEntity> = mock()
        whenever(authorizationService.getMapper<TestEntity, RelatedTestEntity>(any(), any()))
            .thenReturn(mapper)

        val relatedEntity: RelatedTestEntity = mock()
        whenever(mapper.mapRelated(any())).thenReturn(listOf(relatedEntity))
        val authSpec: AuthorizationSpecification<RelatedTestEntity> = mock()
        whenever(authorizationService.getAuthorizationSpecification<RelatedTestEntity>(any(), any())).thenReturn(
            authSpec
        )

        val result = conditionTemplate.isValid(entity)
        assertEquals(false, result)
        verify(authSpec).isAuthorized(relatedEntity)
    }

    @Test
    fun `should fail when spec for related entity returns true`() {
        val mapper: AuthorizationEntityMapper<TestEntity, RelatedTestEntity> = mock()
        whenever(authorizationService.getMapper<TestEntity, RelatedTestEntity>(any(), any()))
            .thenReturn(mapper)

        val relatedEntity: RelatedTestEntity = mock()
        whenever(mapper.mapRelated(any())).thenReturn(listOf(relatedEntity))
        val authSpec: AuthorizationSpecification<RelatedTestEntity> = mock()
        whenever(authorizationService.getAuthorizationSpecification<RelatedTestEntity>(any(), any())).thenReturn(
            authSpec
        )
        whenever(authSpec.isAuthorized(relatedEntity)).thenReturn(true)

        val result = conditionTemplate.isValid(entity)
        assertEquals(true, result)
        verify(authSpec).isAuthorized(relatedEntity)
    }

    @Test
    fun `should serialize to JSON`() {
        val permissionCondition = FieldPermissionCondition("myField", "myValue")
        val condition = conditionTemplate.copy(
            conditions = listOf(
                permissionCondition
            )
        )

        val json = mapper.writeValueAsString(condition)
        JSONAssert.assertEquals(
            """
            {
                  "type": "${condition.type.value}",
                  "resourceType": "${condition.resourceType.name}",
                  "conditions": [
                      {
                          "type": "${permissionCondition.type.value}",
                          "field": "${permissionCondition.field}",
                          "value": "${permissionCondition.value}"
                      }
                  ]
            }
        """.trimIndent(), json, JSONCompareMode.NON_EXTENSIBLE
        )
    }

    @Test
    fun `should deserialize from JSON`() {
        val result: PermissionCondition = mapper.readValue(
            """
                {
                    "type": "container",
                    "resourceType": "com.ritense.authorization.testimpl.TestEntity",
                    "conditions": [
                        {
                            "type": "field",
                            "field": "myField",
                            "value": "myValue"
                        }
                    ]
                }
            """.trimIndent()
        )

        val condition = conditionTemplate.copy(
            conditions = listOf(
                FieldPermissionCondition("myField", "myValue")
            )
        )
        MatcherAssert.assertThat(result, Matchers.equalTo(condition))
    }
}