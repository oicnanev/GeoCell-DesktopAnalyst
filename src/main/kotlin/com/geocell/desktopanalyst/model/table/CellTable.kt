package com.geocell.desktopanalyst.model.table

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date

/**
 * Exposed SQL Table object representing the geocell_cell database table.
 *
 * This table serves as the central repository for cellular network element data,
 * storing comprehensive information about individual cells across multiple technology
 * generations (2G, 3G, 4G, 5G, NR-IoT). It establishes relationships with all major
 * domain entities through foreign key references.
 *
 * The table supports complex cellular network topologies with proper normalization
 * and referential integrity, enabling sophisticated queries for network analysis,
 * planning, and visualization.
 *
 * @property id the unique auto-incrementing identifier for the cell record
 * @property lacTac the Location Area Code (2G/3G) or Tracking Area Code (4G/5G) identifier
 * @property enbGnb the foreign key reference to the base station in [EnbGnbTable]
 * @property ci the Cell Identity for 2G/3G networks (optional)
 * @property eciNci the E-UTRAN Cell Identity for 4G or NR Cell Identity for 5G networks
 * @property cgi the Cell Global Identity - unique worldwide identifier for the cell
 * @property paragonCgi alternative CGI format used in Paragon systems
 * @property mccMnc the foreign key reference to the mobile operator in [MccMncTable]
 * @property technology the technology generation code (2=2G, 3=3G, 4=4G, 5=5G, 10=NR-IoT)
 * @property band the foreign key reference to frequency band information in [BandTable]
 * @property direction the antenna direction in degrees (0-359) where 0° is North
 * @property name the human-readable name or description of the cell
 * @property location the foreign key reference to geographical data in [LocationTable]
 * @property created the date when this cell record was initially created
 * @property owner the identifier of the user or system that created the record
 * @property modified the date when this cell record was last updated
 * @property modifier the identifier of the user or system that last modified the record
 *
 * @sample
 * ```
 * // SQL equivalent table definition:
 * // CREATE TABLE geocell_cell (
 * //   id BIGSERIAL PRIMARY KEY,
 * //   lac_tac VARCHAR(50) NOT NULL,
 * //   enb_gnb_id BIGINT REFERENCES geocell_enbgnb(id),
 * //   ci VARCHAR(20),
 * //   eci_nci VARCHAR(20),
 * //   cgi VARCHAR(30),
 * //   paragon_cgi VARCHAR(30),
 * //   mcc_mnc_id INTEGER REFERENCES geocell_mccmnc(mnc),
 * //   technology INTEGER DEFAULT 0,
 * //   band_id BIGINT REFERENCES geocell_band(id),
 * //   direction INTEGER DEFAULT 0,
 * //   name VARCHAR(200),
 * //   location_id BIGINT REFERENCES geocell_location(id),
 * //   created DATE NOT NULL,
 * //   owner_id VARCHAR(100),
 * //   modified DATE NOT NULL,
 * //   modifier_id VARCHAR(100)
 * // );
 * ```
 *
 * @see EnbGnbTable
 * @see MccMncTable
 * @see BandTable
 * @see LocationTable
 * @see org.jetbrains.exposed.sql.Table
 * @since 1.0.0
 */
object CellTable : Table("geocell_cell") {
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