package com.geocell.desktopanalyst.extensions

import com.geocell.desktopanalyst.model.domain.Band
import com.geocell.desktopanalyst.model.domain.Cell
import com.geocell.desktopanalyst.model.domain.CellPolygon
import com.geocell.desktopanalyst.model.domain.Location
import com.geocell.desktopanalyst.model.domain.MCCMNC
import com.geocell.desktopanalyst.model.table.BandTable
import com.geocell.desktopanalyst.model.table.CellPolygonTable
import com.geocell.desktopanalyst.model.table.CellTable
import com.geocell.desktopanalyst.model.table.LocationTable
import com.geocell.desktopanalyst.model.table.MccMncTable
import org.jetbrains.exposed.sql.ResultRow
import org.locationtech.jts.geom.Point

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
 * This extension handles the mapping of a multi-table join result (CellTable, LocationTable, BandTable, MccMncTable)
 * into a complete Cell entity with all related data. It safely handles nullable fields and
 * provides default values where appropriate.
 *
 * The mapping includes:
 * - Core cell properties (CGI, technology, direction, etc.)
 * - Optional MCC/MNC information with operator and country data
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
    enbGnbId = this.getOrNull(CellTable.enbGnb),
    mccMnc = this.getOrNull(MccMncTable.id)?.let { mccMncId ->
        MCCMNC(
            type = this.getOrNull(MccMncTable.type),
            mcc = this.getOrNull(MccMncTable.mcc),
            mnc = this.getOrNull(MccMncTable.mnc),
            operator = this.getOrNull(MccMncTable.operator),
            country = null,
            brand = this.getOrNull(MccMncTable.brand),
            status = this.getOrNull(MccMncTable.status),
            bands = this.getOrNull(MccMncTable.bands),
            notes = this.getOrNull(MccMncTable.notes)
        )
    },
    band = this.getOrNull(BandTable.id)?.let { bandId ->
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
            coordinates = this.getOrNull(LocationTable.coordinates) as Point?,
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

fun ResultRow.toCellPolygon(): CellPolygon =
    CellPolygon(
        id = this[CellPolygonTable.id],
        polygon = this[CellPolygonTable.polygon],
        polygonShort = this[CellPolygonTable.polygonShort],
        cellId = this[CellPolygonTable.cellId]
    )