package com.geocell.desktopanalyst.model.table

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.Table
import org.locationtech.jts.geom.Polygon
import org.locationtech.jts.io.WKTReader
import org.locationtech.jts.io.WKTWriter

/**
 * Exposed SQL Table object representing the geocell_cellpolygon database table.
 *
 * This table stores polygon geometries representing cellular network coverage areas
 * and service territories. It supports multiple polygon representations for different
 * use cases, including detailed coverage maps and simplified versions for performance.
 *
 * The table utilizes PostGIS spatial capabilities and custom column types to handle
 * complex geographical data, enabling spatial queries and operations on cellular
 * coverage areas.
 *
 * @property id the unique auto-incrementing identifier for the polygon record
 * @property polygon the detailed polygon geometry representing the full coverage area
 * @property polygonShort a simplified or generalized polygon for performance optimization
 * @property cellId the foreign key reference to the associated cell in [CellTable]
 *
 * @sample
 * ```
 * // SQL equivalent table definition:
 * // CREATE TABLE geocell_cellpolygon (
 * //   id BIGSERIAL PRIMARY KEY,
 * //   polygon GEOMETRY(POLYGON, 4326),
 * //   polygon_short GEOMETRY(POLYGON, 4326),
 * //   cell_id BIGINT REFERENCES geocell_cell(id)
 * // );
 * ```
 *
 * @see CellTable
 * @see org.jetbrains.exposed.sql.Table
 * @since 1.0.0
 */
object CellPolygonTable : Table("geocell_cellpolygon") {
    val id = long("id").autoIncrement()
    val polygon = polygon("polygon", 4326)
    val polygonShort = polygon("polygon_short", 4326)
    val cellId = long("cell_id").references(CellTable.id)
    override val primaryKey = PrimaryKey(id)
}

/**
 * Custom ColumnType for handling Polygon geometries in Exposed ORM.
 *
 * This class provides bidirectional conversion between JTS Polygon objects and
 * PostGIS geometry representations in the database. It supports both WKT (Well-Known Text)
 * format and native PostGIS geometry types for optimal performance and compatibility.
 *
 * The column type uses SRID 4326 (WGS84) by default, which is the standard for
 * geographical coordinates using latitude and longitude.
 *
 * @param srid the Spatial Reference System Identifier (default: 4326 for WGS84)
 *
 * @throws IllegalArgumentException when the database value is not a valid polygon geometry
 * @see org.jetbrains.exposed.sql.ColumnType
 * @since 1.0.0
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

/**
 * Extension function to create Polygon columns in Exposed Table definitions.
 *
 * This function provides a convenient way to define polygon geometry columns
 * in table schemas, abstracting the custom column type implementation and
 * providing a clean API for spatial column definition.
 *
 * @param name the name of the database column
 * @param srid the Spatial Reference System Identifier (default: 4326 for WGS84)
 * @return a Column<Polygon?> instance for use in table definitions
 *
 * @sample
 * ```
 * object MySpatialTable : Table("my_table") {
 *     val id = long("id").autoIncrement()
 *     val area = polygon("area", 4326)
 *     // ... other columns
 * }
 * ```
 *
 * @see Table
 * @see PolygonColumnType
 * @since 1.0.0
 */
fun Table.polygon(name: String, srid: Int = 4326): Column<Polygon?> =
    registerColumn(name, PolygonColumnType(srid))

/**
 * Converts a PostGIS Polygon object to a JTS Polygon object.
 *
 * This utility function handles the conversion between PostGIS native geometry
 * types and JTS (Java Topology Suite) geometry objects, enabling interoperability
 * between database spatial operations and application-level geometry processing.
 *
 * The conversion preserves the polygon's shell (exterior ring) and any holes
 * (interior rings) present in the original geometry.
 *
 * @param postgisPolygon the PostGIS Polygon object to convert
 * @return a JTS Polygon object with equivalent geometry
 *
 * @since 1.0.0
 */
private fun convertPostGisPolygonToJts(postgisPolygon: org.postgis.Polygon): Polygon {
    // Implementation of the specific conversion - Simplified version, may need tweaking
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