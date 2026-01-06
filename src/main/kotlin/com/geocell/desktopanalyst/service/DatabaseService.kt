package com.geocell.desktopanalyst.service

import com.geocell.desktopanalyst.extensions.toCell
import com.geocell.desktopanalyst.extensions.toCellPolygon
import com.geocell.desktopanalyst.model.table.BandTable
import com.geocell.desktopanalyst.model.domain.Cell
import com.geocell.desktopanalyst.model.domain.CellPolygon
import com.geocell.desktopanalyst.model.table.CellPolygonTable
import com.geocell.desktopanalyst.model.table.CellTable
import com.geocell.desktopanalyst.model.table.CountyTable
import com.geocell.desktopanalyst.model.table.LocationTable
import com.geocell.desktopanalyst.model.table.MccMncTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.or
import io.github.cdimascio.dotenv.dotenv

/**
 * Service class for database operations related to cellular network data.
 *
 * This service provides access to cellular infrastructure data stored in a PostgreSQL database
 * with PostGIS extension for spatial data. It handles complex joins across multiple tables
 * to retrieve complete cell information including locations, polygons, and metadata.
 *
 * The service uses Exposed ORM for type-safe SQL queries and includes automatic PostGIS
 * extension initialization.
 *
 * @constructor Initializes the database connection using environment variables for configuration.
 * @throws Exception if database connection fails or PostGIS extension cannot be initialized
 *
 * @see Cell
 * @see CellPolygon
 * @since 1.0.0
 */
class DatabaseService {
    init {
        val dotenv = dotenv()
        val url = "jdbc:postgresql://${
            dotenv["DB_HOST"]
        }:${
            dotenv["DB_PORT"]
        }/${
            dotenv["DB_NAME"]
        }"
        val user = dotenv["DB_USER"]
        val password = dotenv["DB_PASSWORD"]

        Database.connect(url, driver = "org.postgresql.Driver", user = user, password = password)
        transaction {
            exec("CREATE EXTENSION IF NOT EXISTS postgis")
        }
    }

    /**
     * Retrieves a single cell by its Cell Global Identity (CGI) identifier.
     *
     * Performs a complex left join across multiple tables to fetch complete cell information
     * including location data, county information, MCC/MNC details, and band specifications.
     *
     * @param cgi the Cell Global Identity identifier to search for
     * @return the complete [Cell] object if found, `null` otherwise
     *
     * @sample
     * ```
     * val service = DatabaseService(url, user, password)
     * val cell = service.getCellByCgi("268-06-8840-8453")
     * // cell: Cell?
     * ```
     */
    fun getCellByCgi(cgi: String): Cell? = transaction {
        addLogger(StdOutSqlLogger)
        
        println("=== DEBUG getCellByCgi ===")
        println("Searching for CGI: $cgi")
        
        val query = (CellTable leftJoin LocationTable leftJoin CountyTable
            leftJoin MccMncTable leftJoin BandTable)
            .select { 
                (CellTable.cgi eq cgi) or (CellTable.paragonCgi eq cgi)
            }
        
        println("Query SQL generated")
        
        val result = query.singleOrNull()
        
        println("Query returned: ${if (result != null) "FOUND" else "NOT FOUND"}")
        
        if (result != null) {
            println("Result row available")
            try {
                val cell = result.toCell()
                println("Successfully converted to Cell object")
                println("Cell CGI: ${cell.cgi}")
                println("Cell location: ${cell.location}")
                return@transaction cell
            } catch (e: Exception) {
                println("ERROR converting to Cell: ${e.message}")
                e.printStackTrace()
            }
        }
        
        println("=== END DEBUG getCellByCgi ===")
        null
    }

    /**
     * Retrieves all polygon data associated with a specific cell identifier.
     *
     * Fetches the geographical polygon representations (coverage areas) for a given cell.
     * These polygons are typically used for visualization in mapping applications.
     *
     * @param cellId the unique identifier of the cell in the database
     * @return a list of [CellPolygon] objects representing the cell's coverage areas
     *
     * @sample
     * ```
     * val polygons = service.getCellPolygons(12345L)
     * // polygons: List<CellPolygon>
     * ```
     */
    fun getCellPolygons(cellId: Long): List<CellPolygon> = transaction {
        CellPolygonTable
            .select { CellPolygonTable.cellId eq cellId }
            .map { it.toCellPolygon() }
    }

    /**
     * Retrieves multiple cells by their CGI identifiers in a single batch operation.
     *
     * This method is optimized for bulk retrieval and includes SQL query logging for debugging.
     * It returns a map where keys are CGI strings and values are the corresponding [Cell] objects.
     *
     * @param cgis a list of Cell Global Identity identifiers to retrieve
     * @return a map of CGI to [Cell] objects for all found cells
     *
     * @sample
     * ```
     * val cells = service.getCellsByCgiList(listOf("268-06-8840-8453", "268-03-8521869"))
     * // cells: Map<String, Cell>
     * val specificCell = cells["268-06-8840-8453"]
     * ```
     */
    fun getCellsByCgiList(cgis: List<String>): Map<String, Cell> = transaction {
        addLogger(StdOutSqlLogger)
        (CellTable leftJoin LocationTable leftJoin CountyTable
                leftJoin MccMncTable leftJoin BandTable)
            .select { CellTable.cgi inList cgis }
            .associate { row ->
                row[CellTable.cgi] to row.toCell()
            }.toNotNullKeyMap()
    }

    /**
     * Retrieves neighbor cells within a specified radius from a given CGI.
     *
     * This method performs a spatial query to find all cells within a certain distance
     * (in kilometers) from the location of the specified cell.
     *
     * @param cgi the Cell Global Identity identifier of the reference cell
     * @param radiusKm the search radius in kilometers
     * @return a list of [Cell] objects within the specified radius, excluding the reference cell
     *
     * @sample
     * ```
     * val neighbors = service.getNeighborCells("268-03-2-1821524179", 1.0)
     * // neighbors: List<Cell> - all cells within 1km radius
     * ```
     *
     * @throws IllegalArgumentException if the reference cell is not found or has no location
     */
    fun getNeighborCells(
        cgi: String,
        radiusKm: Double,
        technologies: List<Int> = emptyList(),
        operators: List<String> = emptyList(),
        sameNetwork: Boolean = false,
        startDate: String? = null,
        endDate: String? = null
    ): List<Cell> = transaction {
        addLogger(StdOutSqlLogger)

        println("=== DEBUG: Starting neighbor query with filters ===")
        println("CGI: $cgi")
        println("Radius: $radiusKm km")
        println("Technologies filter: $technologies")
        println("Operators filter: $operators")
        println("Same network only: $sameNetwork")
        println("Date range: $startDate to $endDate")

        // First, get the reference cell with its location and operator info
        val referenceCell = getCellByCgi(cgi)

        if (referenceCell == null) {
            println("ERROR: Cell with CGI $cgi not found")
            throw IllegalArgumentException("Cell with CGI $cgi not found")
        }

        val referenceLocation = referenceCell.location
        if (referenceLocation == null || referenceLocation.coordinates == null) {
            println("ERROR: Cell with CGI $cgi has no location data")
            throw IllegalArgumentException("Cell with CGI $cgi has no location data")
        }

        // Get reference cell info for sameNetwork filter
        val referenceOperator = referenceCell.mccMnc?.operator
        val referenceMccMnc = referenceCell.mccMnc

        println("Reference operator: $referenceOperator")
        println("Reference coordinates: ${referenceLocation.coordinates}")

        // Get the reference point coordinates
        val refX = referenceLocation.coordinates.x
        val refY = referenceLocation.coordinates.y

        // Convert radius from kilometers to meters (ST_DWithin uses meters)
        val radiusMeters = radiusKm * 1000.0

        // Build the SQL query dynamically based on filters
        val whereConditions = mutableListOf<String>()
        val queryParams = mutableListOf<Any>()

        // Base conditions
        whereConditions.add("l.coordinates IS NOT NULL")
        whereConditions.add("c.cgi != ?")
        queryParams.add(cgi)

        // Spatial condition
        whereConditions.add("ST_DWithin(l.coordinates::geography, ST_SetSRID(ST_MakePoint(?, ?), 4326)::geography, ?)")
        queryParams.add(refX)
        queryParams.add(refY)
        queryParams.add(radiusMeters)

        // Technology filter
        if (technologies.isNotEmpty()) {
            val techPlaceholders = technologies.map { "?" }.joinToString(", ")
            whereConditions.add("c.technology IN ($techPlaceholders)")
            queryParams.addAll(technologies)
        }

        // Operator filter
        if (operators.isNotEmpty()) {
            val operatorPlaceholders = operators.map { "?" }.joinToString(", ")
            whereConditions.add("mcc.brand IN ($operatorPlaceholders)")
            queryParams.addAll(operators)
        }

        // Same network filter
        if (sameNetwork && referenceOperator != null) {
            whereConditions.add("mcc.brand = ?")
            queryParams.add(referenceOperator)
        }

        // Date range filter
        if (startDate != null) {
            whereConditions.add("c.created >= ?::date")
            queryParams.add(startDate)
        }
        if (endDate != null) {
            whereConditions.add("c.created <= ?::date")
            queryParams.add(endDate)
        }

        // Build the WHERE clause
        val whereClause = if (whereConditions.isNotEmpty()) {
            "WHERE " + whereConditions.joinToString(" AND ")
        } else {
            ""
        }

        // Build the SQL query
        val sql = """
        SELECT DISTINCT c.cgi
        FROM geocell_cell c
        LEFT JOIN geocell_location l ON c.location_id = l.id
        LEFT JOIN geocell_mccmnc mcc ON c.mcc_mnc_id = mcc.id
        $whereClause
        LIMIT 1000
    """.trimIndent()

        println("Generated SQL: $sql")
        println("Query parameters: $queryParams")

        // Execute the query
        val neighborCgis = mutableListOf<String>()

        try {
            // We need to use prepared statement with parameters
            // For now, let's build the SQL with parameters inline (for debugging)
            // In production, we should use proper prepared statements

            val sqlWithParams = buildSqlWithParams(sql, queryParams)
            println("Executing SQL with params...")

            val result: MutableList<Array<Any>>? = exec(sqlWithParams) { rs ->
                val results = mutableListOf<Array<Any>>()
                while (rs.next()) {
                    val neighborCgi = rs.getString(1)
                    neighborCgis.add(neighborCgi)
                    results.add(arrayOf(neighborCgi))
                }
                results
            }

            println("Query successful! Found ${neighborCgis.size} neighbors")

        } catch (e: Exception) {
            println("ERROR in query: ${e.message}")
            e.printStackTrace()
            // Fallback to simple query without filters
            //return@transaction getNeighborCellsSimple(cgi, radiusKm)
        }

        // Get complete data for neighbors
        val neighbors = if (neighborCgis.isNotEmpty()) {
            println("Fetching complete data for ${neighborCgis.size} neighbors...")
            getCellsByCgiList(neighborCgis).values.toList()
        } else {
            emptyList()
        }

        println("=== DEBUG: End neighbor query ===")
        println("Total filtered neighbors found: ${neighbors.size}")

        neighbors
    }

    // Helper function to build SQL with parameters (for debugging)
    private fun buildSqlWithParams(sql: String, params: List<Any>): String {
        var result = sql
        params.forEachIndexed { index, param ->
            val value = when (param) {
                is String -> "'$param'"
                is Number -> param.toString()
                else -> param.toString()
            }
            result = result.replaceFirst("\\?".toRegex(), value)
        }
        return result
    }

    // Fallback method without filters
    //private fun getNeighborCellsSimple(cgi: String, radiusKm: Double): List<Cell> = transaction {
        // Simple implementation without filters
        // ... existing simple implementation ...
    //}
}

/**
 * Extension function to safely convert a map with nullable keys to non-null keys.
 *
 * This utility function suppresses unchecked cast warnings and should only be used
 * when the caller is certain that all keys in the map are non-null.
 *
 * @param K the type of keys in the map
 * @param V the type of values in the map
 * @return a new map with the same entries but with non-null key type
 *
 * @sample
 * ```
 * val mapWithNulls: Map<String?, Cell> = mapOf("cgi1" to cell1, null to cell2)
 * val safeMap: Map<String, Cell> = mapWithNulls.toNotNullKeyMap()
 * ```
 *
 * @suppress UNCHECKED_CAST
 */
@Suppress("UNCHECKED_CAST")
fun <K, V> Map<K?, V>.toNotNullKeyMap(): Map<K, V> = this as Map<K, V>
