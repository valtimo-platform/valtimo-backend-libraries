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

package com.ritense.smartdocuments.domain

import com.fasterxml.jackson.annotation.JsonProperty

data class DocumentsStructure(
    @JsonProperty("TemplatesStructure")
    val templatesStructure: TemplatesStructure,
    @JsonProperty("UsersStructure")
    val usersStructure: UsersStructure
)

data class TemplatesStructure(
    @JsonProperty("@IsAccessible")
    val isAccessible: String,
    @JsonProperty("TemplateGroups")
    val templateGroups: List<TemplateGroup>
)

data class TemplateGroup(
    @JsonProperty("@IsAccessible")
    val isAccessible: String,
    @JsonProperty("@ID")
    val id: String,
    @JsonProperty("@Name")
    val name: String,
    @JsonProperty("TemplateGroups")
    val templateGroups: List<TemplateGroup>,
    @JsonProperty("Templates")
    val templates: List<Template>
)

data class Template(
    @JsonProperty("@ID")
    val id: String,
    @JsonProperty("@Name")
    val name: String
)

data class UsersStructure(
    @JsonProperty("@IsAccessible")
    val isAccessible: String,
    @JsonProperty("GroupsAccess")
    val groupsAccess: GroupsAccess,
    @JsonProperty("UserGroups")
    val userGroups: UserGroups
)

data class GroupsAccess(
    @JsonProperty("TemplateGroups")
    val templateGroups: List<TemplateGroup>,
    @JsonProperty("HeaderGroups")
    val headerGroups: List<Any>
)

data class UserGroups(
    @JsonProperty("UserGroup")
    val userGroup: UserGroup
)

data class UserGroup(
    @JsonProperty("@IsAccessible")
    val isAccessible: String,
    @JsonProperty("@ID")
    val id: String,
    @JsonProperty("@Name")
    val name: String,
    @JsonProperty("GroupsAccess")
    val groupsAccess: GroupsAccess,
    @JsonProperty("UserGroups")
    val userGroups: List<UserGroup>,
    @JsonProperty("Users")
    val users: Users
)

data class Users(
    @JsonProperty("User")
    val user: User
)

data class User(
    @JsonProperty("@ID")
    val id: String,
    @JsonProperty("@Name")
    val name: String
)
