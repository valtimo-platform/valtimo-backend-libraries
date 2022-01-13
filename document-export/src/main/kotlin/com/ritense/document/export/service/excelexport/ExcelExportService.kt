package com.ritense.document.export.service.excelexport

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.springframework.stereotype.Service

@Service
class ExcelExportService(
    private val dataToExcelParser: DataToExcelParser
) {

    fun parseDataToExcelAndStoreInS3() = runBlocking {
        launch {
            //TODO: write function
            val rows = emptyList<List<Any>>()
            val fileName = "s3path"
            dataToExcelParser.parseDataToExcel(fileName, rows)
        }
    }
}