package com.geocell.desktopanalyst.model.table

import org.jetbrains.exposed.sql.Table

/**
 * Exposed SQL Table object representing the geocell_district database table.
 *
 * This table stores district or regional-level administrative division information,
 * serving as an intermediate geographical entity between countries and counties.
 * Districts provide regional context for cellular network planning, resource
 * allocation, and regional analysis within a country.
 *
 * The table establishes the hierarchical relationship between countries and their
 * constituent districts, enabling organized regional management of cellular
 * network infrastructure and operations.
 *
 * @property id the unique identifier for the district within the administrative system
 * @property district the official name of the district or region
 * @property country the foreign key reference to the parent country in [CountryTable]
 *
 * @sample
 * ```
 * // SQL equivalent table definition:
 * // CREATE TABLE geocell_district (
 * //   id VARCHAR(20) PRIMARY KEY,
 * //   district VARCHAR(100) NOT NULL,
 * //   country_id VARCHAR(100) REFERENCES geocell_country(name)
 * // );
 * ```
 *
 * @sample
 * ```
 * // Typical district entries for Portugal:
 * // id: "11", district: "Lisbon", country_id: "Portugal"
 * // id: "12", district: "Portalegre", country_id: "Portugal"
 * // id: "13", district: "Santarém", country_id: "Portugal"
 * // id: "14", district: "Setúbal", country_id: "Portugal"
 * ```
 *
 * @see CountryTable
 * @see CountyTable
 * @see org.jetbrains.exposed.sql.Table
 * @since 1.0.0
 */
object DistrictTable : Table("geocell_district") {
    val id = varchar("id", length = 20)
    val district = varchar("district", length = 100)
    val country = varchar("country_id", length = 100) references CountryTable.name
    override val primaryKey = PrimaryKey(id)
}