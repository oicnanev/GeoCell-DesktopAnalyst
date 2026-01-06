package com.geocell.desktopanalyst.model.domain

import org.locationtech.jts.geom.Point

/**
 * Data class representing a precise geographical location with address information.
 *
 * This domain model encapsulates both spatial coordinates and human-readable address data,
 * serving as the fundamental geographical reference point for all cellular network elements.
 * Locations provide the precise positioning context for cells, base stations, and other
 * network infrastructure components.
 *
 * The class combines GIS capabilities with traditional address information, enabling both
 * spatial analysis and practical field operations. It serves as the bridge between precise
 * coordinate-based systems and human-understandable location descriptions.
 *
 * @property id the unique database identifier for the location record
 * @property coordinates the precise geographical coordinates as a JTS Point geometry
 * @property address the primary address line (e.g., street name and number)
 * @property address1 the secondary address line (e.g., building, floor, apartment)
 * @property zip4 the 4-digit extension of the postal code for precise localization
 * @property zip3 the 3-digit base of the postal code for regional grouping
 * @property postalDesignation the city/town name or postal designation
 * @property idCounty the foreign key reference to the associated [County] entity
 *
 * @sample
 * ```
 * val cellSiteLocation = Location(
 *     id = 78910,
 *     coordinates = Point(38.736946, -9.142685), // Lisbon coordinates
 *     address = "Avenida da Liberdade 245",
 *     address1 = "Telecom Tower, 15th Floor",
 *     zip4 = 1250,
 *     zip3 = 125,
 *     postalDesignation = "Lisbon",
 *     idCounty = 1106
 * )
 * ```
 *
 * @sample
 * ```
 * // Usage in distance calculations and coverage analysis
 * val locations = databaseService.getLocationsByCounty(1106)
 * locations.forEach { location ->
 *     val coverageRadius = calculateCoverageRadius(location.coordinates)
 *     plotCoverageArea(location, coverageRadius)
 * }
 * ```
 *
 * @see County
 * @see Point
 * @see Cell
 * @since 1.0.0
 */
data class Location(
    val id: Long,
    val coordinates: Point?,
    val address: String?,
    val address1: String?,
    val zip4: Int?,
    val zip3: Int?,
    val postalDesignation: String?,
    val idCounty: Long?
)