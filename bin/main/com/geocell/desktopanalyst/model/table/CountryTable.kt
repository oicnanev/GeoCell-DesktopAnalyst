package com.geocell.desktopanalyst.model.table

import org.jetbrains.exposed.sql.Table

/**
 * Exposed SQL Table object representing the geocell_country database table.
 *
 * This table stores country information used for geographical organization and
 * international context in cellular network data. It provides the top-level
 * geographical hierarchy for organizing network elements by country and supports
 * international standards for country identification.
 *
 * The table uses the country name as the primary key, ensuring unique country
 * entries while maintaining human-readable identifiers for easy reference and
 * integration with international country code systems.
 *
 * @property name the full official name of the country, serving as the primary key
 * @property code the international country code following ISO 3166-1 standard (e.g., "PT", "ES", "FR")
 *
 * @sample
 * ```
 * // SQL equivalent table definition:
 * // CREATE TABLE geocell_country (
 * //   name VARCHAR(100) PRIMARY KEY,
 * //   code VARCHAR(10)
 * // );
 * ```
 *
 * @sample
 * ```
 * // Typical country entries:
 * // name: "Portugal", code: "PT"
 * // name: "Spain", code: "ES"
 * // name: "France", code: "FR"
 * ```
 *
 * @see DistrictTable
 * @see org.jetbrains.exposed.sql.Table
 * @since 1.0.0
 */
object CountryTable : Table("geocell_country") {
    val name = varchar("name", length = 100)
    val code = varchar("code", length = 10).nullable()
    override val primaryKey = PrimaryKey(name)
}