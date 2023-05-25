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

package com.ritense.authorization

// TODO: Don't make this an enum, make these values that can be stored in the DB
enum class Action {
    LIST_VIEW, // case, process, task, note
    VIEW, // case, process, task, note
    START, // case-instance, process-instance
    CREATE, // note, s3resource, document-instance
    MODIFY, // document-instance, note, add file to document, assign resource
    DELETE, // note, s3resource
    COMPLETE, // task-instance
    ASSIGN, // document-instance, task-instance
    CLAIM, // Assign to self
    IGNORE,
    DENY // assign document relation
}
