package com.geocell.desktopanalyst.model.table

import org.jetbrains.exposed.sql.Table

/**
 * Exposed SQL Table object representing the geocell_enbgnb database table.
 *
 * This table stores eNodeB (4G LTE) and gNodeB (5G NR) base station information,
 * which are the fundamental infrastructure elements for modern cellular networks.
 * These base stations serve as central hubs that manage multiple cells and provide
 * radio access network (RAN) functionality.
 *
 * The table establishes the relationship between base station identifiers and their
 * physical locations, enabling precise geographical positioning of network
 * infrastructure elements for coverage analysis and network planning.
 *
 * @property id the unique auto-incrementing identifier for the base station record
 * @property enbGnb the base station identifier number (eNodeB ID for 4G, gNodeB ID for 5G)
 * @property location the foreign key reference to the geographical location in [LocationTable]
 *
 * @sample
 * ```
 * // SQL equivalent table definition:
 * // CREATE TABLE geocell_enbgnb (
 * //   id BIGSERIAL PRIMARY KEY,
 * //   enb_gnb INTEGER NOT NULL,
 * //   location_id BIGINT REFERENCES geocell_location(id)
 * // );
 * ```
 *
 * @sample
 * ```
 * // Typical base station entries:
 * // id: 12345, enb_gnb: 8840, location_id: 67890
 * // id: 12346, enb_gnb: 8841, location_id: 67891
 * // id: 12347, enb_gnb: 8842, location_id: 67892
 * ```
 *
 * @see LocationTable
 * @see CellTable
 * @see org.jetbrains.exposed.sql.Table
 * @since 1.0.0
 */
object EnbGnbTable : Table("geocell_enbgnb") {
    val id = long("id").autoIncrement()
    val enbGnb = integer("enb_gnb")
    val location = long("location_id").references(LocationTable.id)
    override val primaryKey = PrimaryKey(id)
}