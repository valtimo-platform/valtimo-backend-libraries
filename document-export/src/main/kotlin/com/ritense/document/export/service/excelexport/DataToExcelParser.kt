package com.ritense.document.export.service.excelexport

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

        workbook.use {
            val startRow = workbook.xssfWorkbook.getSheetAt(0).lastRowNum.plus(1)
            println(startRow)
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
            Files.delete(Paths.get(fileName))
            Files.move(Paths.get(tempExcelFileName), Paths.get(fileName))
        } else {
            FileOutputStream(fileName).use { out -> workbook.write(out) }
        }
        workbook.dispose()
    }

    companion object {
        private const val tempExcelFileName = "tempExcelFileName"
        private const val sheetname = "sheet_1"
    }
}
