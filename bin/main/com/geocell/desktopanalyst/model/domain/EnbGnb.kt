package com.geocell.desktopanalyst.model.domain

/**
 * Data class representing an eNodeB (4G) or gNodeB (5G) base station in cellular networks.
 *
 * This domain model encapsulates the core infrastructure elements for 4G LTE and 5G NR networks,
 * serving as the central hub that manages multiple cells and provides radio access network (RAN)
 * functionality. eNodeBs/gNodeBs are the fundamental building blocks of modern cellular networks.
 *
 * In network architecture, these base stations control radio resources, manage handovers,
 * and provide the interface between user equipment and the core network. Each base station
 * typically hosts multiple cells with different sectors and frequency bands.
 *
 * @property enbGnb the base station identifier number (eNodeB ID for 4G, gNodeB ID for 5G)
 * @property location the geographical location and site information of the base station
 * @property id the unique database identifier for this base station record
 *
 * @sample
 * ```
 * val lisbonEnb = EnbGnb(
 *     enbGnb = 8840,
 *     location = Location(/* site location details */),
 *     id = 12345
 * )
 * ```
 *
 * @sample
 * ```
 * // Usage in network topology analysis
 * val baseStations = databaseService.getEnbGnbsByRegion("Lisbon")
 * baseStations.forEach { enbGnb ->
 *     val cells = databaseService.getCellsByEnbGnb(enbGnb.id)
 *     analyzeSectorCoverage(enbGnb.location, cells)
 * }
 * ```
 *
 * @see Cell
 * @see Location
 * @since 1.0.0
 */
data class EnbGnb(
    val enbGnb: Int?,
    val location: Location,
    val id: Long
)