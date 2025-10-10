package com.geocell.desktopanalyst.model.domain

/**
 * Data class representing frequency band information for cellular network technology.
 *
 * This domain model encapsulates the technical specifications of a cellular frequency band,
 * including bandwidth, frequency ranges, and identification codes. It is used to model
 * the radio frequency characteristics of 2G, 3G, 4G, 5G, and NR-IoT technologies.
 *
 * @property band the band designation identifier (e.g., "B20", "B3", "B7")
 * @property bandwidth the total bandwidth available in the band, measured in MHz
 * @property uplinkFreq the uplink frequency range in MHz (device to tower)
 * @property downlinkFreq the downlink frequency range in MHz (tower to device)
 * @property earfcn the E-UTRA Absolute Radio Frequency Channel Number for LTE bands
 *
 * @sample
 * ```
 * val band20 = Band(
 *     band = "B20",
 *     bandwidth = 10.0,
 *     uplinkFreq = 832.0,
 *     downlinkFreq = 791.0,
 *     earfcn = 6150.0
 * )
 *
 * val band3 = Band(
 *     band = "B3",
 *     bandwidth = 15.0,
 *     uplinkFreq = 1710.0,
 *     downlinkFreq = 1805.0,
 *     earfcn = 1200.0
 * )
 * ```
 *
 * @see Cell
 * @since 1.0.0
 */
data class Band(
    val band: String?,
    val bandwidth: Double?,
    val uplinkFreq: Double?,
    val downlinkFreq: Double?,
    val earfcn: Double?
)