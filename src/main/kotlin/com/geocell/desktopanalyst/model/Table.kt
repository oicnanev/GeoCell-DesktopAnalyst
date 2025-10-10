package com.geocell.desktopanalyst.model

import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date
import org.locationtech.jts.geom.Polygon
import org.locationtech.jts.io.WKTReader
import org.locationtech.jts.io.WKTWriter

object CountyTable : Table("geocell_county") {
    val id = long("id").autoIncrement()
    val idCounty = varchar("id_county", length = 20)
    val county = varchar("county", length = 100)
    val district = varchar("district_id", length = 20) references DistrictTable.id
    override val primaryKey = PrimaryKey(idCounty, district)
}

object DistrictTable : Table("geocell_district") {
    val id = varchar("id", length = 20)
    val district = varchar("district", length = 100)
    val country = varchar("country_id", length = 100) references CountryTable.name
    override val primaryKey = PrimaryKey(id)
}

object CountryTable : Table("geocell_country") {
    val name = varchar("name", length = 100)
    val code = varchar("code", length = 10).nullable()
    override val primaryKey = PrimaryKey(name)
}

object LocationTable : Table("geocell_location") {
    val id = long("id").autoIncrement()
    val coordinates = point("coordinates", 4326)
    val address = varchar("address", length = 100).nullable()
    val address1 = varchar("address1", length = 100).nullable()
    val zip4 = integer("zip4").default(0)
    val zip3 = integer("zip3").default(0)
    val postalDesignation = varchar("postal_designation", length = 100).nullable()
    val idCounty = long("id_county_id").references(CountyTable.id).nullable()
    override val primaryKey = PrimaryKey(id)
}

object CellPolygonTable : Table("geocell_cellpolygon") {
    val id = long("id").autoIncrement()
    val polygon = polygon("polygon", 4326)
    val polygonShort = polygon("polygon_short", 4326)
    val cellId = long("cell_id").references(CellTable.id)
    override val primaryKey = PrimaryKey(id)
}

object MccMncTable : Table("geocell_mccmnc") {
    val id = long("id").autoIncrement()
    val type = varchar("type", length =  100).nullable()
    val mcc = integer("mcc")
    val mnc = integer("mnc")
    val brand = varchar("brand", length = 100).nullable()
    val operator = varchar("operator", length = 200).nullable()
    val status = varchar("status", length = 100).nullable()
    val bands = varchar("bands", length = 200).nullable()
    val notes = varchar("notes", length = 300).nullable()
    val country = varchar("country_id", length = 100).references(CountryTable.name).nullable()
    override val primaryKey = PrimaryKey(mcc, mnc)
}

object BandTable : Table("geocell_band") {
    val id = long("id").autoIncrement()
    val band = varchar("band", length = 50).nullable()
    val bandwidth = float("bandwidth").nullable()
    val uplinkFrequency = float("uplink_freq").nullable()
    val downlinkFrequency = float("downlink_freq").nullable()
    val earfcn = float("earfcn").nullable()
    override val primaryKey = PrimaryKey(id)
}

object EnbGnbTable : Table("geocell_enbgnb") {
    val id = long("id").autoIncrement()
    val enbGnb = integer("enb_gnb")
    val location = long("location_id").references(LocationTable.id)
    override val primaryKey = PrimaryKey(id)
}

object CellTable : Table("geocell_cell") {
    val TECH_CHOICES = mapOf(
        2 to "2G",
        3 to "3G",
        4 to "4G",
        5 to "5G",
        10 to "NR-IoT"
    )
    val id = long("id").autoIncrement()
    val lacTac = varchar("lac_tac", length = 50)
    val enbGnb = long("enb_gnb_id").references(EnbGnbTable.id)
    val ci = varchar("ci", length = 20).nullable()
    val eciNci = varchar("eci_nci", length = 20).nullable()
    val cgi = varchar("cgi", length = 30).nullable()
    val paragonCgi = varchar("paragon_cgi", length = 30).nullable()
    val mccMnc = integer("mcc_mnc_id").references(MccMncTable.mnc).nullable()
    val technology = integer("technology").default(0)
    val band = long("band_id").references(BandTable.id).nullable()
    val direction = integer("direction").default(0)
    val name = varchar("name", length = 200).nullable()
    val location = long("location_id").references(LocationTable.id).nullable()
    val created = date("created")
    val owner = varchar("owner_id", length = 100).nullable()
    val modified = date("modified")
    val modifier = varchar("modifier_id", length = 100).nullable()
    override val primaryKey = PrimaryKey(id)
}


// ColumnType específico para Polygon
/*class PolygonColumnType(private var srid: Int = 4326) : ColumnType() {
    override fun sqlType(): String = "GEOMETRY(POLYGON, $srid)"

    override fun valueFromDB(value: Any): Polygon = when (value) {
        is String -> {
            val geometry = WKTReader().read(value)
            if (geometry is Polygon) {
                geometry.srid = this.srid
                geometry
            } else {
                throw IllegalArgumentException("Expected POLYGON but got: ${geometry.geometryType}")
            }
        }
        is org.postgis.PGgeometry -> {
            val geometry = value.geometry
            if (geometry is org.postgis.Polygon) {
                // Converter de PostGIS Polygon para JTS Polygon
                convertPostGisPolygonToJts(geometry)
            } else {
                throw IllegalArgumentException("Expected POLYGON but got: ${geometry.type}")
            }
        }
        else -> throw IllegalArgumentException("Unexpected value: $value")
    }

    override fun notNullValueToDB(value: Any): String {
        val polygon = value as Polygon
        return "ST_GeomFromText('${WKTWriter().write(polygon)}', $srid)"
    }

    override fun nonNullValueToString(value: Any): String =
        WKTWriter().write(value as Polygon)
}
*/
class PolygonColumnType(private var srid: Int = 4326) : ColumnType() {
    override fun sqlType(): String = "GEOMETRY(POLYGON, $srid)"

    override fun valueFromDB(value: Any): Polygon = when (value) {
        is String -> {
            val geometry = WKTReader().read(value)
            if (geometry is Polygon) {
                geometry.srid = this.srid
                geometry
            } else {
                throw IllegalArgumentException("Expected POLYGON but got: ${geometry.geometryType}")
            }
        }
        is org.postgis.PGgeometry -> {
            val geometry = value.geometry
            if (geometry is org.postgis.Polygon) {
                convertPostGisPolygonToJts(geometry)
            } else {
                throw IllegalArgumentException("Expected POLYGON but got: ${geometry.type}")
            }
        }
        else -> throw IllegalArgumentException("Unexpected value: $value")
    }

    override fun notNullValueToDB(value: Any): String {
        val polygon = value as Polygon
        return "ST_GeomFromText('${WKTWriter().write(polygon)}', $srid)"
    }

    override fun nonNullValueToString(value: Any): String {
        val polygon = value as Polygon
        return "ST_GeomFromText('${WKTWriter().write(polygon)}', $srid)"
    }
}

// Função auxiliar para criar colunas Polygon
fun Table.polygon(name: String, srid: Int = 4326): Column<Polygon?> =
    registerColumn(name, PolygonColumnType(srid))

// Conversor de PostGIS Polygon para JTS Polygon (se necessário)
private fun convertPostGisPolygonToJts(postgisPolygon: org.postgis.Polygon): Polygon {
    // Implementação da conversão específica
    // Esta é uma versão simplificada - pode precisar de ajustes
    val geometryFactory = org.locationtech.jts.geom.GeometryFactory()
    val shell = geometryFactory.createLinearRing(
        postgisPolygon.getRing(0).points.map {
            org.locationtech.jts.geom.Coordinate(it.x, it.y)
        }.toTypedArray()
    )

    val holes = if (postgisPolygon.numRings() > 1) {
        (1 until postgisPolygon.numRings()).map { ringIndex ->
            geometryFactory.createLinearRing(
                postgisPolygon.getRing(ringIndex).points.map {
                    org.locationtech.jts.geom.Coordinate(it.x, it.y)
                }.toTypedArray()
            )
        }.toTypedArray()
    } else {
        null
    }

    return geometryFactory.createPolygon(shell, holes)
}

// ColumnType para Point
/*
class PointColumnType(private var srid: Int = 4326) : ColumnType() {
    override fun sqlType(): String = "GEOMETRY(POINT, $srid)"

    override fun valueFromDB(value: Any): org.locationtech.jts.geom.Point = when (value) {
        is String -> {
            val geometry = WKTReader().read(value)
            if (geometry is org.locationtech.jts.geom.Point) {
                geometry.srid = this.srid
                geometry
            } else {
                throw IllegalArgumentException("Expected POINT but got: ${geometry.geometryType}")
            }
        }
        else -> throw IllegalArgumentException("Unexpected value: $value")
    }

    override fun notNullValueToDB(value: Any): String {
        val point = value as org.locationtech.jts.geom.Point
        return "ST_GeomFromText('${WKTWriter().write(point)}', $srid)"
    }
}
*/
class PointColumnType(private var srid: Int = 4326) : ColumnType() {
    override fun sqlType(): String = "GEOMETRY(POINT, $srid)"

    override fun valueFromDB(value: Any): org.locationtech.jts.geom.Point = when (value) {
        is String -> {
            // Existing WKT string handling
            val geometry = WKTReader().read(value)
            if (geometry is org.locationtech.jts.geom.Point) {
                geometry.srid = this.srid
                geometry
            } else {
                throw IllegalArgumentException("Expected POINT but got: ${geometry.geometryType}")
            }
        }
        is org.postgis.PGgeometry -> {
            // Handle PostGIS geometry object
            val postgisPoint = value.geometry
            if (postgisPoint is org.postgis.Point) {
                // Convert PostGIS Point to JTS Point
                val coordinate = org.locationtech.jts.geom.Coordinate(postgisPoint.x, postgisPoint.y)
                val geometryFactory = org.locationtech.jts.geom.GeometryFactory()
                val jtsPoint = geometryFactory.createPoint(coordinate)
                jtsPoint.srid = this.srid
                jtsPoint
            } else {
                throw IllegalArgumentException("Expected POINT but got: ${postgisPoint.type}")
            }
        }
        else -> {
            println("Unexpected value type: ${value.javaClass.name}")
            println("Value: $value")
            throw IllegalArgumentException("Unexpected value: $value")
        }
    }

    override fun notNullValueToDB(value: Any): String {
        val point = value as org.locationtech.jts.geom.Point
        return "ST_GeomFromText('${WKTWriter().write(point)}', $srid)"
    }

    override fun nonNullValueToString(value: Any): String {
        val point = value as org.locationtech.jts.geom.Point
        return "ST_GeomFromText('${WKTWriter().write(point)}', $srid)"
    }
}

// Função auxiliar para Point
fun Table.point(name: String, srid: Int = 4326): Column<org.locationtech.jts.geom.Point?> =
    registerColumn(name, PointColumnType(srid))
