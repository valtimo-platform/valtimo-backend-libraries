package com.ritense.document.export.service.excelexport

import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.stereotype.Component
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@Component
class DataToExcelParser {

    fun parseDataToExcel(
        fileName: String,
        rows: List<List<Any>>
    ) {
        val fileExists = Files.exists(Path.of(fileName))
        var workbook = createWorkbook(fileName, fileExists)

        workbook = convertDataToRows(
            workbook = workbook,
            rows = rows
        )
        writeDataToExcel(
            fileName = fileName,
            workbook = workbook,
            fileExists = fileExists
        )
    }

    private fun writeDataToExcel(
        fileName: String,
        workbook: SXSSFWorkbook,
        fileExists: Boolean
    ) {
        if (fileExists) {
            FileOutputStream(tempExcelFileName).use { out -> workbook.write(out) }
            // files can not be overridden with Workbook
            Files.delete(Paths.get(fileName))
            Files.move(Paths.get(tempExcelFileName), Paths.get(fileName))
        } else {
            FileOutputStream(fileName).use { out -> workbook.write(out) }
        }
        workbook.dispose()
    }

    private fun convertDataToRows(
        workbook: SXSSFWorkbook,
        rows: List<List<Any>>
    ): SXSSFWorkbook {
        val sheet = workbook.getSheetAt(0)
        val startRow = sheet.lastRowNum.plus(1)
        workbook.use {
            rows.forEachIndexed { index, columns ->
                val row = sheet.createRow(startRow.plus(index))
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
        }
        return workbook
    }

    private fun createWorkbook(
        fileName: String,
        fileExists: Boolean
    ): SXSSFWorkbook {
        val workbook: SXSSFWorkbook
        if (fileExists) {
            val xssf = XSSFWorkbook(fileName)
            workbook = SXSSFWorkbook(xssf, 1)
            workbook.xssfWorkbook.getSheetAt(0)
        } else {
            workbook = SXSSFWorkbook(1)
            workbook.createSheet(sheetname)
        }
        return workbook
    }


    companion object {
        private const val tempExcelFileName = "tempExcelFileName"
        private const val sheetname = "sheet_1"
    }
}
