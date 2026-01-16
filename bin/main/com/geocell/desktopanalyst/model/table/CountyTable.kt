package com.geocell.desktopanalyst.model.table

import org.jetbrains.exposed.sql.Table

/**
 * Exposed SQL Table object representing the geocell_county database table.
 *
 * This table stores county or municipal-level administrative division information,
 * serving as an intermediate geographical entity between districts and specific
 * locations. Counties provide detailed regional context for cellular network
 * planning and analysis at the municipal level.
 *
 * The table uses a composite primary key combining the county identifier with
 * the district reference, ensuring unique county entries within each district
 * while maintaining the hierarchical relationship with parent districts.
 *
 * @property id the unique auto-incrementing identifier for the county record
 * @property idCounty the unique identifier for the county within the administrative system
 * @property county the official name of the county or municipality
 * @property district the foreign key reference to the parent district in [DistrictTable]
 *
 * @sample
 * ```
 * // SQL equivalent table definition:
 * // CREATE TABLE geocell_county (
 * //   id BIGSERIAL,
 * //   id_county VARCHAR(20) NOT NULL,
 * //   county VARCHAR(100) NOT NULL,
 * //   district_id VARCHAR(20) REFERENCES geocell_district(id),
 * //   PRIMARY KEY (id_county, district_id)
 * // );
 * ```
 *
 * @sample
 * ```
 * // Typical county entries for Lisbon district:
 * // id_county: "1106", county: "Lisbon", district_id: "11"
 * // id_county: "1115", county: "Oeiras", district_id: "11"
 * // id_county: "1116", county: "Amadora", district_id: "11"
 * ```
 *
 * @see DistrictTable
 * @see LocationTable
 * @see org.jetbrains.exposed.sql.Table
 * @since 1.0.0
 */
object CountyTable : Table("geocell_county") {
    val id = long("id").autoIncrement()
    val idCounty = varchar("id_county", length = 20)
    val county = varchar("county", length = 100)
    val district = varchar("district_id", length = 20) references DistrictTable.id
    override val primaryKey = PrimaryKey(idCounty, district)
}