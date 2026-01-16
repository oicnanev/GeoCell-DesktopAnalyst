package com.geocell.desktopanalyst.model.table

import org.jetbrains.exposed.sql.Table

/**
 * Exposed SQL Table object representing the geocell_mccmnc database table.
 *
 * This table stores Mobile Country Code (MCC) and Mobile Network Code (MNC) information,
 * which form the international identification system for mobile network operators worldwide.
 * These codes are essential for roaming, network selection, and operator identification
 * in cellular networks across different countries and regions.
 *
 * The table uses a composite primary key combining MCC and MNC to ensure unique
 * identification of mobile network operators globally, while maintaining comprehensive
 * business and technical information about each operator.
 *
 * @property id the unique auto-incrementing identifier for the operator record
 * @property type the network technology or service type (e.g., "GSM", "UMTS", "LTE", "NR")
 * @property mcc the Mobile Country Code (3 digits) identifying the country
 * @property mnc the Mobile Network Code (2-3 digits) identifying the network operator
 * @property brand the commercial brand name used by the operator
 * @property operator the full legal name of the mobile network operator
 * @property status the operational status (e.g., "Operational", "Inactive", "Planned")
 * @property bands the frequency bands supported by this operator
 * @property notes additional information or comments about the operator
 * @property country the foreign key reference to the associated country in [CountryTable]
 *
 * @sample
 * ```
 * // SQL equivalent table definition:
 * // CREATE TABLE geocell_mccmnc (
 * //   id BIGSERIAL,
 * //   type VARCHAR(100),
 * //   mcc INTEGER NOT NULL,
 * //   mnc INTEGER NOT NULL,
 * //   brand VARCHAR(100),
 * //   operator VARCHAR(200),
 * //   status VARCHAR(100),
 * //   bands VARCHAR(200),
 * //   notes VARCHAR(300),
 * //   country_id VARCHAR(100) REFERENCES geocell_country(name),
 * //   PRIMARY KEY (mcc, mnc)
 * // );
 * ```
 *
 * @sample
 * ```
 * // Typical operator entries for Portugal:
 * // mcc: 268, mnc: 1, operator: "Vodafone Portugal", brand: "Vodafone"
 * // mcc: 268, mnc: 3, operator: "NOS Comunicações", brand: "NOS"
 * // mcc: 268, mnc: 6, operator: "MEO", brand: "MEO"
 * ```
 *
 * @see CountryTable
 * @see CellTable
 * @see org.jetbrains.exposed.sql.Table
 * @since 1.0.0
 */
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