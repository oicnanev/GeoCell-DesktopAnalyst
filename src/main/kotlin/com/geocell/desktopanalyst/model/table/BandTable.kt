package com.geocell.desktopanalyst.model.table

import org.jetbrains.exposed.sql.Table

/**
 * Exposed SQL Table object representing the geocell_band database table.
 *
 * This table stores frequency band information for cellular network technologies,
 * including technical specifications such as bandwidth, frequency ranges, and
 * channel numbers. It supports multiple cellular generations (2G, 3G, 4G, 5G)
 * with their respective band characteristics.
 *
 * The table serves as a reference for cellular frequency bands used by network
 * operators, enabling proper band configuration and frequency planning for
 * cellular network elements.
 *
 * @property id the unique auto-incrementing identifier for the band record
 * @property band the band designation identifier (e.g., "B20", "B3", "B7", "n78")
 * @property bandwidth the total bandwidth available in the band, measured in MHz
 * @property uplinkFrequency the uplink frequency range in MHz (device to tower direction)
 * @property downlinkFrequency the downlink frequency range in MHz (tower to device direction)
 * @property earfcn the E-UTRA Absolute Radio Frequency Channel Number for LTE bands
 *
 * @sample
 * ```
 * // SQL equivalent table definition:
 * // CREATE TABLE geocell_band (
 * //   id BIGSERIAL PRIMARY KEY,
 * //   band VARCHAR(50),
 * //   bandwidth FLOAT,
 * //   uplink_freq FLOAT,
 * //   downlink_freq FLOAT,
 * //   earfcn FLOAT
 * // );
 * ```
 *
 * @see org.jetbrains.exposed.sql.Table
 * @since 1.0.0
 */
object BandTable : Table("geocell_band") {
    val id = long("id").autoIncrement()
    val band = varchar("band", length = 50).nullable()
    val bandwidth = float("bandwidth").nullable()
    val uplinkFrequency = float("uplink_freq").nullable()
    val downlinkFrequency = float("downlink_freq").nullable()
    val earfcn = float("earfcn").nullable()
    override val primaryKey = PrimaryKey(id)
}