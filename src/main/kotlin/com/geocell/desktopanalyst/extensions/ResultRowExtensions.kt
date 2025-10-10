package com.geocell.desktopanalyst.extensions

import com.geocell.desktopanalyst.model.domain.Band
import com.geocell.desktopanalyst.model.domain.Cell
import com.geocell.desktopanalyst.model.domain.CellPolygon
import com.geocell.desktopanalyst.model.domain.Location
import com.geocell.desktopanalyst.model.table.BandTable
import com.geocell.desktopanalyst.model.table.CellPolygonTable
import com.geocell.desktopanalyst.model.table.CellTable
import com.geocell.desktopanalyst.model.table.LocationTable
import org.jetbrains.exposed.sql.ResultRow

/**
 * Extension functions for mapping Exposed ORM ResultRow objects to domain models.
 *
 * These extensions provide type-safe conversion between database query results and
 * domain entities, handling optional fields and complex relationships with proper
 * null safety.
 *
 * @see ResultRow
 * @see Cell
 * @see CellPolygon
 * @since 1.0.0
 */

/**
 * Converts a ResultRow from a complex cell query into a fully populated Cell domain object.
 *
 * This extension handles the mapping of a multi-table join result (CellTable, LocationTable, BandTable)
 * into a complete Cell entity with all related data. It safely handles nullable fields and
 * provides default values where appropriate.
 *
 * The mapping includes:
 * - Core cell properties (CGI, technology, direction, etc.)
 * - Optional band information with frequency data
 * - Optional location data with coordinates and address information
 * - Temporal metadata (created and modified timestamps)
 *
 * @receiver the ResultRow containing joined data from cell-related tables
 * @return a fully populated [Cell] object with all available related data
 *
 * @throws Exception if required fields are missing or type conversion fails
 *
 * @sample
 * ```
 * val cell = (CellTable leftJoin LocationTable leftJoin BandTable)
 *     .select { CellTable.cgi eq "268-06-8840-8453" }
 *     .single()
 *     .toCell()
 * ```
 *
 * @see CellTable
 * @see LocationTable
 * @see BandTable
 */
fun ResultRow.toCell(): Cell =
    Cell(
        id = this[CellTable.id],
        cgi = this[CellTable.cgi],
        paragonCgi = this[CellTable.paragonCgi] ?: "",
        technology = this[CellTable.technology],
        name = this[CellTable.name] ?: "",
        direction = this[CellTable.direction],
        lacTac = this[CellTable.lacTac],
        eciNci = this[CellTable.eciNci] ?: "",
        enbGnbId = null,
        mccMnc = null,
        band = this.getOrNull(BandTable.id)?.let { _ ->
            Band(
                band = this.getOrNull(BandTable.band),
                bandwidth = this.getOrNull(BandTable.bandwidth) as Double?,
                uplinkFreq = this.getOrNull(BandTable.uplinkFrequency) as Double?,
                downlinkFreq = this.getOrNull(BandTable.downlinkFrequency) as Double?,
                earfcn = this.getOrNull(BandTable.earfcn) as Double?
            )
        },
        location = this.getOrNull(LocationTable.id)?.let { locationId ->
            Location(
                id = locationId,
                coordinates = this.getOrNull(LocationTable.coordinates),
                address = this.getOrNull(LocationTable.address),
                address1 = this.getOrNull(LocationTable.address1),
                zip4 = this.getOrNull(LocationTable.zip4) ?: 0,
                zip3 = this.getOrNull(LocationTable.zip3) ?: 0,
                postalDesignation = this.getOrNull(LocationTable.postalDesignation),
                idCounty = this.getOrNull(LocationTable.idCounty),
            )
        },
        created = this[CellTable.created],
        modified = this[CellTable.modified]
    )

/**
 * Converts a ResultRow from CellPolygonTable into a CellPolygon domain object.
 *
 * This extension provides a straightforward mapping for polygon data, converting
 * spatial geometry data from the database into domain model format for use in
 * visualization and spatial operations.
 *
 * @receiver the ResultRow containing cell polygon data
 * @return a [CellPolygon] object with spatial geometry and cell association
 *
 * @sample
 * ```
 * val polygons = CellPolygonTable
 *     .select { CellPolygonTable.cellId eq 12345L }
 *     .map { it.toCellPolygon() }
 * ```
 *
 * @see CellPolygonTable
 * @see CellPolygon
 */
fun ResultRow.toCellPolygon(): CellPolygon =
    CellPolygon(
        id = this[CellPolygonTable.id],
        polygon = this[CellPolygonTable.polygon],
        polygonShort = this[CellPolygonTable.polygonShort],
        cellId = this[CellPolygonTable.cellId]
    )