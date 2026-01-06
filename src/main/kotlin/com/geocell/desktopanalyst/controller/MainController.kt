package com.geocell.desktopanalyst.controller

import com.geocell.desktopanalyst.service.CsvProcessor
import com.geocell.desktopanalyst.service.DatabaseService
import com.geocell.desktopanalyst.service.KmzGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Main controller class that orchestrates the CSV to KMZ conversion pipeline.
 */
class MainController {

    data class CsvData(
        val color: String?,
        val target: String?,
        val notes: String?
    )

    private lateinit var dbService: DatabaseService
    private val csvProcessor = CsvProcessor()
    private val kmzGenerator = KmzGenerator()

    fun initializeDatabase() {
        dbService = DatabaseService()
    }

    suspend fun processCsvToKmz(csvFile: File, outputFile: File): Boolean = withContext(Dispatchers.IO) {
        try {
            // Parse CSV and extract metadata
            val records = csvProcessor.parseCsv(csvFile)
            val csvDataByCgi = records.associate {
                it.cgi to CsvData(it.color, it.target, it.notes)
            }
            val cgis = records.map { it.cgi }.distinct()

            // Fetch complete cell data from database
            val cells = dbService.getCellsByCgiList(cgis)

            // Fetch polygon data for all retrieved cells
            val cellPolygons = cells.values.associate { cell ->
                cell.id to dbService.getCellPolygons(cell.id)
            }

            // Create timestamp mapping for temporal organization
            val timestampsByCgi = records.associate { it.cgi to it.timestamp }

            // Generate the final KMZ file
            kmzGenerator.generateKmz(cells, cellPolygons, timestampsByCgi, csvDataByCgi, outputFile)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Queries neighbor cells within a specified radius from a given CGI with optional filters.
     */
    fun queryNeighbors(
        cgi: String,
        radiusKm: Double,
        technologies: List<Int> = emptyList(),
        operators: List<String> = emptyList(),
        sameNetwork: Boolean = false,
        startDate: String? = null,
        endDate: String? = null
    ): List<com.geocell.desktopanalyst.model.domain.Cell> {
        return dbService.getNeighborCells(
            cgi = cgi,
            radiusKm = radiusKm,
            technologies = technologies,
            operators = operators,
            sameNetwork = sameNetwork,
            startDate = startDate,
            endDate = endDate
        )
    }
}