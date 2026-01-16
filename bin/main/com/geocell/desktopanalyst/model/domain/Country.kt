package com.geocell.desktopanalyst.model.domain

import org.locationtech.jts.geom.Polygon
import java.awt.Image

/**
 * Data class representing a country with geographical, political, and visual attributes.
 *
 * This domain model encapsulates national-level information including geographical boundaries,
 * international codes, and visual representations. It serves as a foundational entity for
 * regional organization of cellular network data and multinational network planning.
 *
 * Countries provide the top-level geographical context for cellular infrastructure data,
 * enabling regional analysis, international roaming considerations, and multi-national
 * network visualization.
 *
 * @property name the full official name of the country (e.g., "Portugal", "Spain")
 * @property code the international country code (e.g., "PT", "ES", "FR") typically following ISO 3166-1 standard
 * @property polygon the geographical boundary of the country as a JTS polygon geometry
 * @property flag the visual representation of the country's flag as an image
 *
 * @sample
 * ```
 * val portugal = Country(
 *     name = "Portugal",
 *     code = "PT",
 *     polygon = iberianPeninsulaGeometry,
 *     flag = ImageIO.read(File("portugal_flag.png"))
 * )
 * ```
 *
 * @sample
 * ```
 * // Usage in regional network analysis
 * val countries = databaseService.getCountriesWithNetworkData()
 * countries.forEach { country ->
 *     val cellsInCountry = databaseService.getCellsByCountry(country.code)
 *     analyzeNetworkDensity(country.polygon, cellsInCountry)
 * }
 * ```
 *
 * @see Polygon
 * @since 1.0.0
 */
data class Country(
    val name: String?,
    val code: String?,
    val polygon: Polygon?,
    val flag: Image?
)