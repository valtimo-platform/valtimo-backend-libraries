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


package com.ritense.document.export.service

import com.ritense.document.export.service.excelexport.DataToExcelParser
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import java.nio.file.Files.deleteIfExists
import java.nio.file.Files.exists
import java.nio.file.Path

private val ROWS = listOf(
    listOf<Any>("Head First Java", "Kathy Serria", 79.0),
    listOf<Any>("Effective Java", "Joshua Bloch", 36.0),
    listOf<Any>("Clean Code", "Robert martin", 42.0),
    listOf<Any>("Thinking in Java", "Bruce Eckel", 35.0)
)

private val MORE_ROWS = listOf(
    listOf<Any>("Hsdfdfsdfsdead First Java", "Kathy Serria", 79.0),
    listOf<Any>("Effecdfdfaftive Java", "Joshua Bloch", 36.0),
    listOf<Any>("Clean sdfdfaCode", "Robert martin", 42.0),
    listOf<Any>("Thinking iafdafadfdfn Java", "Bruce Eckel", 35.0)
)

private const val PATH = "src/test/resources/test-files/Books.xlsx"

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class DataToExcelParserTest {
    private val dataToExcelParser = DataToExcelParser()

    @AfterEach
    fun `breakdown`() {
        deleteIfExists(Path.of(PATH))
    }

    @Order(1)
    @Test
    fun `should create excel`() {
        dataToExcelParser.parseDataToExcel(
            fileName = PATH,
            rows = ROWS
        )
        assertThat(exists(Path.of(PATH))).isTrue
    }

    @Order(2)
    @Test
    fun `should write rows into excel`() {
        dataToExcelParser.parseDataToExcel(
            fileName = PATH,
            rows = ROWS
        )
        assertThat(getDataFromExcel()).isEqualTo(ROWS)
    }

    @Order(3)
    @Test
    fun `should append rows to existing excel`() {
        dataToExcelParser.parseDataToExcel(
            fileName = PATH,
            rows = ROWS
        )
        dataToExcelParser.parseDataToExcel(
            fileName = PATH,
            rows = MORE_ROWS
        )
        assertThat(getDataFromExcel()).isEqualTo(ROWS + MORE_ROWS)
    }

    private fun getDataFromExcel(): List<Any> {
        val xssf = XSSFWorkbook(PATH)
        val sheet = xssf.getSheetAt(0)
        val dataList = mutableListOf<MutableList<Any>>()
        sheet.forEach { row ->
            val list = mutableListOf<Any>()
            row.forEach { cell ->
                when (cell.cellType) {
                    CellType.STRING -> list.add(cell.richStringCellValue.string)
                    CellType.NUMERIC -> list.add(cell.numericCellValue)
                    else -> list.add(" ")
                }
            }
            dataList.add(list)
        }
        return dataList
    }
}