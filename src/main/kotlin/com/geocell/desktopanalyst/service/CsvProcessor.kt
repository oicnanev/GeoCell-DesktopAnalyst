package com.geocell.desktopanalyst.service

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import java.io.File

data class CsvRecord(
    val timestamp: String,
    val cgi: String,
    // Add more CSV columns as needed
)

class CsvProcessor {
    fun parseCsv(file: File): List<CsvRecord> {
        return csvReader().readAllWithHeader(file).map { row ->
            CsvRecord(
                timestamp = row["timestamp"] ?: "",
                cgi = row["cgi"] ?: ""
            )
        }
    }
}