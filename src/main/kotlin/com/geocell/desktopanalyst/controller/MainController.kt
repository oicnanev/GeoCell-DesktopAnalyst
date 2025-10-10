package com.geocell.desktopanalyst.controller

import com.geocell.desktopanalyst.service.CsvProcessor
import com.geocell.desktopanalyst.service.DatabaseService
import com.geocell.desktopanalyst.service.KmzGenerator
import javafx.application.Application
import javafx.stage.Stage
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.stage.FileChooser
import kotlinx.coroutines.*
import java.io.File

class MainController {
    private lateinit var dbService: DatabaseService
    private val csvProcessor = CsvProcessor()
    private val kmzGenerator = KmzGenerator()

    fun initializeDatabase(host: String, port: Int, database: String, user: String, password: String) {
        val url = "jdbc:postgresql://$host:$port/$database"
        dbService = DatabaseService(url, user, password)
    }

    suspend fun processCsvToKmz(csvFile: File, outputFile: File): Boolean = withContext(Dispatchers.IO) {
        try {
            // Parse CSV
            val records = csvProcessor.parseCsv(csvFile)
            val cgis = records.map { it.cgi }.distinct()

            // Fetch cell data
            val cells = dbService.getCellsByCgiList(cgis)

            // Fetch polygons for all cells
            val cellPolygons = cells.values.associate { cell ->
                cell.id to dbService.getCellPolygons(cell.id)
            }

            // Map timestamps by CGI
            val timestampsByCgi = records.associate { it.cgi to it.timestamp }

            // Generate KMZ
            kmzGenerator.generateKmz(cells, cellPolygons, timestampsByCgi, outputFile)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}