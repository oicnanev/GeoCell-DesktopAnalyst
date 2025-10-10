package com.geocell.desktopanalyst.model.domain

import org.locationtech.jts.geom.Polygon

/**
 * Data class representing a county or municipal-level administrative division.
 *
 * This domain model encapsulates geographical and administrative information at the county level,
 * serving as an intermediate geographical entity between districts and specific locations.
 * Counties provide detailed regional context for cellular network planning and analysis.
 *
 * In the geographical hierarchy, counties represent municipal-level divisions that aggregate
 * multiple locations and provide more granular organization than districts while maintaining
 * broader coverage than individual cell sites.
 *
 * @property idCounty the unique identifier for the county within the administrative system
 * @property county the official name of the county or municipality
 * @property polygon the geographical boundary of the county as a JTS polygon geometry
 * @property district the parent [District] entity that contains this county
 *
 * @sample
 * ```
 * val lisbonCounty = County(
 *     idCounty = "1106",
 *     county = "Lisbon",
 *     polygon = lisbonMunicipalityGeometry,
 *     district = lisbonDistrict
 * )
 * ```
 *
 * @sample
 * ```
 * // Usage in regional network density analysis
 * val countiesInDistrict = databaseService.getCountiesByDistrict("11")
 * countiesInDistrict.forEach { county ->
 *     val cellCount = databaseService.getCellCountByCounty(county.idCounty)
 *     calculateNetworkDensity(county.polygon, cellCount)
 * }
 * ```
 *
 * @see District
 * @see Location
 * @see Polygon
 * @since 1.0.0
 */
data class County(
    val idCounty: String?,
    val county: String?,
    val polygon: Polygon?,
    val district: District?
)