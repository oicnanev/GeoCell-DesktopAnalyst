package com.geocell.desktopanalyst.model.domain

import org.locationtech.jts.geom.Polygon

/**
 * Data class representing a district or regional-level administrative division within a country.
 *
 * This domain model encapsulates geographical and administrative information at the district level,
 * serving as an intermediate geographical entity between countries and counties. Districts provide
 * regional context for cellular network planning, resource allocation, and regional analysis.
 *
 * In the geographical hierarchy, districts represent important regional divisions that aggregate
 * multiple counties and provide organizational structure for network operations and regional
 * management. They are typically the first level of sub-national administration below the country level.
 *
 * @property id the unique identifier for the district within the administrative system
 * @property district the official name of the district or region
 * @property polygon the geographical boundary of the district as a JTS polygon geometry
 * @property country the parent [Country] entity that contains this district
 *
 * @sample
 * ```
 * val lisbonDistrict = District(
 *     id = "11",
 *     district = "Lisbon",
 *     polygon = lisbonDistrictGeometry,
 *     country = portugal
 * )
 * ```
 *
 * @sample
 * ```
 * // Usage in regional network planning
 * val districtsInCountry = databaseService.getDistrictsByCountry("PT")
 * districtsInCountry.forEach { district ->
 *     val networkResources = calculateRegionalResources(district.polygon)
 *     allocateNetworkBudget(district.id, networkResources)
 * }
 * ```
 *
 * @see Country
 * @see County
 * @see Polygon
 * @since 1.0.0
 */
data class District(
    val id: String?,
    val district: String?,
    val polygon: Polygon?,
    val country: Country?
)