/*
 * Copyright 2015-2025 Ritense BV, the Netherlands.
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

package com.ritense.form.domain.submission.formfield

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.readValue
import com.ritense.form.BaseTest
import com.ritense.form.domain.FormIoFormDefinition.NOT_IGNORED
import com.ritense.valtimo.contract.json.MapperSingleton
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class FormFieldTest : BaseTest() {

    @Test
    fun `should get FormFields of datagrid with nested UploadFields`() {
        val formDefinition = formDefinitionOf("datagrid-upload-field")
        val formData = MapperSingleton.get().readValue<JsonNode>("""
            {
              "textField" : "1234",
              "dataGrid" : [ {
                "documentenApiFile" : [ {
                  "id" : "18182169417376128053-11653144863331328211",
                  "filename" : "img1.jpg",
                  "sizeInBytes" : 2073331
                } ],
                "documentenApiFile2" : [ {
                  "id" : "12262880345062617667-5555523454283988899",
                  "filename" : "img2.jpg",
                  "sizeInBytes" : 2073331
                } ]
              }, {
                "documentenApiFile" : [ {
                  "id" : "18182169417376128053-11653144863331328211",
                  "filename" : "img3.jpg",
                  "sizeInBytes" : 2073331
                } ],
                "documentenApiFile2" : [ {
                  "id" : "12262880345062617667-5555523454283988899",
                  "filename" : "img4.jpg",
                  "sizeInBytes" : 2073331
                } ]
              } ],
              "submit" : true
            }
        """)

        val formFields = formDefinition.getDocumentMappedFieldsFiltered(NOT_IGNORED)
            .mapNotNull { objectNode -> FormField.getFormField(formData, objectNode, mock()) }

        assertEquals(2, formFields.size)
        assertEquals("DataField", formFields[0].javaClass.simpleName)
        assertEquals("1234", formFields[0].value.textValue())
        assertEquals("/textField", formFields[0].pointer.toString())
        assertEquals("ComponentsField", formFields[1].javaClass.simpleName)
        assertEquals("/dataGrid", formFields[1].pointer.toString())
        val dataGrid = formFields[1] as ComponentsField
        assertEquals("UploadField", dataGrid.childFormFields[0].javaClass.simpleName)
        assertTrue(dataGrid.childFormFields[0].value.toString().contains(""""filename":"img1.jpg""""))
        assertEquals("/documentenApiFile", dataGrid.childFormFields[0].pointer.toString())
        assertEquals("UploadField", dataGrid.childFormFields[1].javaClass.simpleName)
        assertTrue(dataGrid.childFormFields[1].value.toString().contains(""""filename":"img2.jpg""""))
        assertEquals("/documentenApiFile2", dataGrid.childFormFields[1].pointer.toString())
        assertEquals("UploadField", dataGrid.childFormFields[2].javaClass.simpleName)
        assertTrue(dataGrid.childFormFields[2].value.toString().contains(""""filename":"img3.jpg""""))
        assertEquals("/documentenApiFile", dataGrid.childFormFields[2].pointer.toString())
        assertEquals("UploadField", dataGrid.childFormFields[3].javaClass.simpleName)
        assertTrue(dataGrid.childFormFields[3].value.toString().contains(""""filename":"img4.jpg""""))
        assertEquals("/documentenApiFile2", dataGrid.childFormFields[3].pointer.toString())
    }

}