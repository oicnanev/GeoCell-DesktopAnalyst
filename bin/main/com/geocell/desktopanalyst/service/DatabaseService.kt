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
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
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
        (CellTable leftJoin LocationTable leftJoin CountyTable
            leftJoin MccMncTable leftJoin BandTable)
            .select { CellTable.cgi eq cgi }
            .singleOrNull()
            ?.toCell()
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