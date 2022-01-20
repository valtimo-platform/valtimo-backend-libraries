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


package com.ritense.document.export.service.excelexport

import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.stereotype.Component
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

@Component
class DataToExcelParser {

    fun parseDataToExcel(
        fileName: String,
        rows: List<List<Any>>
    ) {
        val fileExists = Files.exists(Path.of(fileName))
        val workbook = createWorkbook(fileName, fileExists)

        convertDataToRows(
            workbook = workbook,
            fileName = fileName,
            fileExists = fileExists,
            rows = rows
        )
    }

    private fun createWorkbook(
        fileName: String,
        fileExists: Boolean
    ): SXSSFWorkbook {
        val workbook: SXSSFWorkbook
        if (fileExists) {
            val xssf = XSSFWorkbook(fileName)
            workbook = SXSSFWorkbook(xssf, 1)
        } else {
            workbook = SXSSFWorkbook(1)
            workbook.createSheet(sheetname)
        }
        return workbook
    }

    private fun convertDataToRows(
        workbook: SXSSFWorkbook,
        fileName: String,
        fileExists: Boolean,
        rows: List<List<Any>>
    ) {
        val startRow = workbook.xssfWorkbook.getSheetAt(0).lastRowNum.plus(1)
        workbook.use {
            rows.forEachIndexed { index, columns ->
                val row = workbook.getSheetAt(0).createRow(startRow.plus(index))
                columns.indices.forEach { cellNumber ->
                    if (columns[cellNumber] is String) {
                        val result: String = columns[cellNumber] as String
                        row.createCell(cellNumber).setCellValue(result)
                    }
                    if (columns[cellNumber] is Number) {
                        val result: Double = columns[cellNumber] as Double
                        row.createCell(cellNumber).setCellValue(result)
                    }
                }
            }
            writeDataToExcel(fileName, workbook, fileExists)
        }
    }

    private fun writeDataToExcel(
        fileName: String,
        workbook: SXSSFWorkbook,
        fileExists: Boolean
    ) {
        if (fileExists) {
            FileOutputStream(tempExcelFileName).use { out -> workbook.write(out) }
            // files can not be overridden with Workbook
            println("deleted data and copy data now")
            Files.move(Paths.get(tempExcelFileName), Paths.get(fileName), StandardCopyOption.REPLACE_EXISTING)
        } else {
            println("writing data now")
            FileOutputStream(fileName).use { out -> workbook.write(out) }
        }
        workbook.dispose()
    }

    companion object {
        private const val tempExcelFileName = "src/test/resources/test-files/tempExcelFileName.xlsx"
        private const val sheetname = "sheet_1"
    }
}
