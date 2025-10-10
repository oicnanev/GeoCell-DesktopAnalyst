package com.geocell.desktopanalyst.extensions

import com.geocell.desktopanalyst.model.*
import org.jetbrains.exposed.sql.ResultRow

// Extension function para converter ResultRow para Cell
fun ResultRow.toCell(): Cell {
    return Cell(
        id = this[CellTable.id],
        cgi = this[CellTable.cgi],
        paragonCgi = this[CellTable.paragonCgi]?:"",
        technology = this[CellTable.technology],
        name = this[CellTable.name]?:"",
        direction = this[CellTable.direction],
        lacTac = this[CellTable.lacTac],
        eciNci = this[CellTable.eciNci]?:"",
        enbGnbId = null,//this[CellTable.enbGnb]?: 0L,
        mccMnc = null, // Inicializa como null
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
}

// Extension function para converter ResultRow para CellPolygon
fun ResultRow.toCellPolygon(): CellPolygon {
    return CellPolygon(
        id = this[CellPolygonTable.id],
        polygon = this[CellPolygonTable.polygon],
        polygonShort = this[CellPolygonTable.polygonShort],
        cellId = this[CellPolygonTable.cellId]
    )
}