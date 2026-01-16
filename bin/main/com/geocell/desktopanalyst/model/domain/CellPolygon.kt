package com.geocell.desktopanalyst.model.domain

import org.locationtech.jts.geom.Polygon

/**
 * Data class representing geographical polygon data for cellular network coverage areas.
 *
 * This domain model encapsulates spatial geometry data that defines the coverage area
 * of cellular network elements. It supports multiple polygon representations for
 * different use cases, such as detailed coverage maps and simplified visualization.
 *
 * Polygons are used to visualize the estimated service area of cells in mapping
 * applications like Google Earth, providing a geographical context for network
 * planning and optimization.
 *
 * @property id the unique database identifier for the polygon record
 * @property polygon the detailed polygon geometry representing the full coverage area
 * @property polygonShort a simplified or generalized polygon for performance optimization
 * @property cellId the foreign key reference to the associated [Cell] entity
 *
 * @sample
 * ```
 * val coveragePolygon = CellPolygon(
 *     id = 78910,
 *     polygon = detailedCoverageGeometry,
 *     polygonShort = simplifiedCoverageGeometry,
 *     cellId = 12345
 * )
 * ```
 *
 * @sample
 * ```
 * // Usage in KMZ generation for Google Earth visualization
 * val polygons = databaseService.getCellPolygons(cell.id)
 * polygons.forEach { cellPolygon ->
 *     kmlGenerator.addPolygon(cellPolygon.polygon ?: cellPolygon.polygonShort)
 * }
 * ```
 *
 * @see Cell
 * @see Polygon
 * @since 1.0.0
 */
data class CellPolygon(
    val id: Long,
    val polygon: Polygon?,
    val polygonShort: Polygon?,
    val cellId: Long?
)