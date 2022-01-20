package com.ritense.document.export.service

import com.ritense.document.export.service.excelexport.DataToExcelParser
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder

private val ROWS = listOf(
    listOf<Any>("Head First Java", "Kathy Serria", 79.0),
    listOf<Any>("Effective Java", "Joshua Bloch", 36.0),
    listOf<Any>("Clean Code", "Robert martin", 42.0),
    listOf<Any>("Thinking in Java", "Bruce Eckel", 35.0)
)


@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class DataToExcelParserTest {
    private val dataToExcelParser = DataToExcelParser()

    @Order(1)
    @Test
    fun `should create excel with booknames`() {
        dataToExcelParser.parseDataToExcel (
            fileName = "src\\test\\resources\\test-files\\Books.xlsx",
            rows = ROWS
        )
    }

//    @Order(2)
//    @Test
//    fun `should append rows to existing excel`() {
//
//        csvExports.writeToExcelPerRow("D:\\proefprojecten\\Flexibelexporteren\\csvexports\\src\\test\\resources\\Books.xlsx", rows2)
//        csvExports.appendToExistingExcelPerRow(
//            "D:\\proefprojecten\\Flexibelexporteren\\csvexports\\src\\test\\resources\\Books.xlsx",
//            "D:\\proefprojecten\\Flexibelexporteren\\csvexports\\src\\test\\resources\\MoreBooks.xlsx",
//            rows2
//        )
//    }



}