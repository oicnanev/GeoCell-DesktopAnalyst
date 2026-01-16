package com.geocell.desktopanalyst.controller

import com.geocell.desktopanalyst.model.FilterParams
import com.geocell.desktopanalyst.model.domain.Cell
import com.geocell.desktopanalyst.service.CsvProcessor
import com.geocell.desktopanalyst.service.DatabaseService
import com.geocell.desktopanalyst.service.KmzGenerator
import com.geocell.desktopanalyst.service.QueryResultKmzGenerator
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
    private val queryResultKmzGenerator = QueryResultKmzGenerator()

    fun initializeDatabase() {
        dbService = DatabaseService()
    }

    // Expose database service for tabs that need direct access
    val databaseService: DatabaseService
        get() = this.dbService

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
     * Exports query results to KMZ format.
     *
     * This method takes a list of cells from query results and generates a KMZ file
     * using the QueryResultKmzGenerator which:
     * - Names placemarks by CGI
     * - Colors based on operator (MEO=blue, NOS=orange, Vodafone=red, DIGI=cyan)
     * - Includes all database fields in descriptions
     *
     * @param cells list of cells to export
     * @param outputFile the destination file where the KMZ will be written
     * @return true if export was successful, false otherwise
     */
    suspend fun exportQueryResultsToKmz(
        cells: List<Cell>,
        outputFile: File
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            // Convert list to map by CGI
            val cellsMap = cells.associateBy { it.cgi ?: "unknown-${it.id}" }

            // Fetch polygon data for all cells
            val cellPolygons = cells.associate { cell ->
                cell.id to dbService.getCellPolygons(cell.id)
            }

            // Generate the final KMZ file using QueryResultKmzGenerator
            queryResultKmzGenerator.generateKmz(cellsMap, cellPolygons, outputFile)
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

    /**
     * Queries cells within a circular geographical area.
     *
     * @param centerLat latitude of the circle center
     * @param centerLon longitude of the circle center
     * @param radiusKm search radius in kilometers
     * @param filters optional filters to apply
     * @return list of cells within the specified circle
     */
    fun queryCellsInCircle(
        centerLat: Double,
        centerLon: Double,
        radiusKm: Double,
        filters: FilterParams = FilterParams()
    ): List<com.geocell.desktopanalyst.model.domain.Cell> {
        return dbService.getCellsInCircle(
            centerLat = centerLat,
            centerLon = centerLon,
            radiusKm = radiusKm,
            filters = filters
        )
    }

    /**
     * Queries cells within a rectangular geographical area.
     *
     * @param lat1 latitude of first corner
     * @param lon1 longitude of first corner
     * @param lat2 latitude of second corner
     * @param lon2 longitude of second corner
     * @param filters optional filters to apply
     * @return list of cells within the specified rectangle
     */
    fun queryCellsInRectangle(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double,
        filters: FilterParams = FilterParams()
    ): List<com.geocell.desktopanalyst.model.domain.Cell> {
        return dbService.getCellsInRectangle(
            lat1 = lat1,
            lon1 = lon1,
            lat2 = lat2,
            lon2 = lon2,
            filters = filters
        )
    }

    /**
     * Queries cells within an administrative region (district and county).
     *
     * @param districtName name of the district
     * @param countyName name of the county
     * @param filters optional filters to apply
     * @return list of cells within the specified administrative region
     */
    fun queryCellsInAdministrativeRegion(
        districtName: String,
        countyName: String,
        filters: FilterParams = FilterParams()
    ): List<Cell> {
        return dbService.getCellsInAdministrativeRegion(
            districtName = districtName,
            countyName = countyName,
            filters = filters
        )
    }

    /**
     * Queries cells by LAC/TAC (Location Area Code / Tracking Area Code).
     *
     * @param lacTac the LAC/TAC value to search for
     * @param filters optional filter parameters to apply to the query
     * @return a list of cells matching the specified LAC/TAC
     * @throws IllegalArgumentException if lacTac is invalid
     */
    fun queryCellsByLacTac(lacTac: Int, filters: FilterParams = FilterParams()): List<Cell> {
        if (lacTac <= 0) {
            throw IllegalArgumentException("LAC/TAC must be greater than 0")
        }

        return dbService.getCellsByLacTac(lacTac, filters)
    }

    /**
     * Queries cells by eNB/gNB identifier.
     *
     * @param enbGnbId the eNB/gNB identifier to search for
     * @param filters optional filter parameters to apply to the query
     * @return a list of cells associated with the specified eNB/gNB
     * @throws IllegalArgumentException if enbGnbId is invalid
     */
    fun queryCellsByEnbGnb(enbGnbId: Int, filters: FilterParams = FilterParams()): List<Cell> {
        if (enbGnbId <= 0) {
            throw IllegalArgumentException("eNB/gNB ID must be greater than 0")
        }

        return dbService.getCellsByEnbGnb(enbGnbId, filters)
    }

    /**
     * Queries cells by frequency band.
     *
     * @param band the band designation to search for (e.g., "B20", "B3", "n78")
     * @param filters optional filter parameters to apply to the query
     * @return a list of cells using the specified frequency band
     * @throws IllegalArgumentException if band is empty or invalid
     */
    fun queryCellsByBand(band: String, filters: FilterParams = FilterParams()): List<Cell> {
        if (band.isBlank()) {
            throw IllegalArgumentException("Band cannot be empty")
        }

        return dbService.getCellsByBand(band.trim(), filters)
    }
}
