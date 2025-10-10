package com.geocell.desktopanalyst.model.table

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.Table
import org.locationtech.jts.io.WKTReader
import org.locationtech.jts.io.WKTWriter

/**
 * Exposed SQL Table object representing the geocell_location database table.
 *
 * This table stores precise geographical locations with comprehensive address information,
 * serving as the fundamental geographical reference point for all cellular network elements.
 * It combines spatial coordinates with human-readable address data to support both
 * technical spatial analysis and practical field operations.
 *
 * The table establishes the geographical context for cellular infrastructure, enabling
 * precise positioning, distance calculations, and spatial relationships between
 * network elements and their service areas.
 *
 * @property id the unique auto-incrementing identifier for the location record
 * @property coordinates the precise geographical coordinates as a JTS Point geometry
 * @property address the primary address line (e.g., street name and number)
 * @property address1 the secondary address line (e.g., building, floor, apartment)
 * @property zip4 the 4-digit extension of the postal code for precise localization
 * @property zip3 the 3-digit base of the postal code for regional grouping
 * @property postalDesignation the city/town name or postal designation
 * @property idCounty the foreign key reference to the associated county in [CountyTable]
 *
 * @sample
 * ```
 * // SQL equivalent table definition:
 * // CREATE TABLE geocell_location (
 * //   id BIGSERIAL PRIMARY KEY,
 * //   coordinates GEOMETRY(POINT, 4326),
 * //   address VARCHAR(100),
 * //   address1 VARCHAR(100),
 * //   zip4 INTEGER DEFAULT 0,
 * //   zip3 INTEGER DEFAULT 0,
 * //   postal_designation VARCHAR(100),
 * //   id_county_id BIGINT REFERENCES geocell_county(id)
 * // );
 * ```
 *
 * @see CountyTable
 * @see CellTable
 * @see EnbGnbTable
 * @see org.jetbrains.exposed.sql.Table
 * @since 1.0.0
 */
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

/**
 * Custom ColumnType for handling Point geometries in Exposed ORM.
 *
 * This class provides bidirectional conversion between JTS Point objects and
 * PostGIS geometry representations in the database. It supports both WKT (Well-Known Text)
 * format and native PostGIS geometry types, with comprehensive error handling
 * and debugging information for spatial data processing.
 *
 * The column type uses SRID 4326 (WGS84) by default, which is the standard for
 * geographical coordinates using latitude and longitude in decimal degrees.
 *
 * @param srid the Spatial Reference System Identifier (default: 4326 for WGS84)
 *
 * @throws IllegalArgumentException when the database value is not a valid point geometry
 * @see org.jetbrains.exposed.sql.ColumnType
 * @since 1.0.0
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

/**
 * Extension function to create Point geometry columns in Exposed Table definitions.
 *
 * This function provides a convenient way to define point geometry columns
 * in table schemas, abstracting the custom column type implementation and
 * providing a clean API for spatial column definition with proper SRID handling.
 *
 * @param name the name of the database column
 * @param srid the Spatial Reference System Identifier (default: 4326 for WGS84)
 * @return a Column<Point?> instance for use in table definitions
 *
 * @sample
 * ```
 * object MySpatialTable : Table("my_table") {
 *     val id = long("id").autoIncrement()
 *     val position = point("position", 4326)
 *     // ... other columns
 * }
 * ```
 *
 * @see Table
 * @see PointColumnType
 * @since 1.0.0
 */
fun Table.point(name: String, srid: Int = 4326): Column<org.locationtech.jts.geom.Point?> =
    registerColumn(name, PointColumnType(srid))