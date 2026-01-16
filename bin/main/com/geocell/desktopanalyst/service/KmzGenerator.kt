package com.geocell.desktopanalyst.service

import com.geocell.desktopanalyst.controller.MainController.CsvData
import com.geocell.desktopanalyst.model.domain.Cell
import com.geocell.desktopanalyst.model.domain.CellPolygon
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Service class for generating KMZ files from cellular network data.
 *
 * KMZ is a compressed KML (Keyhole Markup Language) format used by Google Earth
 * for displaying geographic data. This generator creates organized KMZ files with:
 * - Daily folders containing Points and Polygons subfolders
 * - Custom colors from CSV data
 * - Rotated directional icons
 * - Transparent polygons for better visibility
 * - Detailed cell information in placemark descriptions
 *
 * @see Cell
 * @see CellPolygon
 * @see CsvData
 * @since 1.0.0
 */
class KmzGenerator {

    /**
     * Generates a KMZ file from cellular network data with CSV-based customization.
     *
     * Creates a compressed ZIP archive containing a KML document with organized
     * folder structure, styled placemarks, and polygon data. The output is optimized
     * for visualization in Google Earth with proper altitude settings and transparency.
     *
     * @param cells map of Cell Global Identity to Cell objects containing cellular data
     * @param cellPolygons map of cell IDs to their associated polygon geometries
     * @param timestampsByCgi map of CGI to timestamp strings for temporal organization
     * @param csvDataByCgi map of CGI to CSV metadata (color, target, notes)
     * @param outputFile the destination file where the KMZ will be written
     *
     * @sample
     * ```
     * val generator = KmzGenerator()
     * generator.generateKmz(cells, polygons, timestamps, csvData, File("output.kmz"))
     * ```
     */
    fun generateKmz(
        cells: Map<String, Cell>,
        cellPolygons: Map<Long, List<CellPolygon>>,
        timestampsByCgi: Map<String, String>,
        csvDataByCgi: Map<String, CsvData>,
        outputFile: File
    ) {
        ZipOutputStream(outputFile.outputStream()).use { zos ->
            zos.putNextEntry(ZipEntry("doc.kml"))

            val kmlContent = """
<?xml version="1.0" encoding="UTF-8"?>
<kml xmlns="http://www.opengis.net/kml/2.2">
<Document>
    <name>Cells Export</name>
    <description>Generated from CSV data</description>
    ${generateFolders(cells, cellPolygons, timestampsByCgi, csvDataByCgi)}
</Document>
</kml>
            """.trimIndent()

            zos.write(kmlContent.toByteArray(Charsets.UTF_8))
            zos.closeEntry()
        }
    }

    /**
     * Generates the folder structure for organizing data by date with Points and Polygons subfolders.
     *
     * Groups cells by date extracted from timestamps and creates a hierarchical structure:
     * - Date Folder
     *   - Points Subfolder (containing cell location placemarks)
     *   - Polygons Subfolder (containing cell coverage area polygons)
     *
     * @param cells map of cellular data to organize
     * @param cellPolygons polygon data associated with cells
     * @param timestampsByCgi temporal data for grouping
     * @param csvDataByCgi CSV metadata for styling
     * @return KML folder structure as XML string
     */
    private fun generateFolders(
        cells: Map<String, Cell>,
        cellPolygons: Map<Long, List<CellPolygon>>,
        timestampsByCgi: Map<String, String>,
        csvDataByCgi: Map<String, CsvData>
    ): String {
        // 1. Group cells by date extracted from timestamps
        val cellsByDate = cells.values
            .filter { cell ->
                val timestamp = timestampsByCgi[cell.cgi]
                timestamp != null && timestamp.isNotBlank()
            }
            .groupBy { cell ->
                // Extract date from timestamp (assuming the "YYYY/MM/DD HH:MM:SS" format)
                val timestamp = timestampsByCgi[cell.cgi]!!
                timestamp.substringBefore(" ")
            }
            .mapValues { (_, cellsInDay) ->
                // Sort cells by time within the day
                cellsInDay.sortedBy { cell ->
                    val timestamp = timestampsByCgi[cell.cgi]!!
                    timestamp.substringAfter(" ")
                }
            }
            .toSortedMap() // Sort folders by date

        // 2. Generate folders for each day with subfolders of Points and Polygons
        return cellsByDate.entries.joinToString("") { (date, cellsInDay) ->
            """
            <Folder>
                <name>${escapeXml(date)}</name>
                <description>${date} cells</description>
                
                <!-- Subfolder for Points -->
                <Folder>
                    <name>Points</name>
                    <description>Cell points of ${date}</description>
                    ${generatePlacemarksForDay(cellsInDay, timestampsByCgi, csvDataByCgi)}
                </Folder>
                
                <!-- Subfolder for Polygons -->
                <Folder>
                    <name>Polygons</name>
                    <description>Cell Polygons of ${date}</description>
                    ${generatePolygonsForDay(cellsInDay, cellPolygons, timestampsByCgi, csvDataByCgi)}
                </Folder>  
            </Folder>
            """
        }
    }

    /**
     * Generates point placemarks for cells within a specific day.
     *
     * Creates KML Placemark elements with styled icons showing cell locations.
     * Icons are rotated based on cell direction and colored according to CSV data
     * or technology type fallback.
     *
     * @param cellsInDay list of cells for the specific day
     * @param timestampsByCgi temporal data for naming placemarks
     * @param csvDataByCgi CSV metadata for styling and descriptions
     * @return KML Placemark elements as XML string
     */
    private fun generatePlacemarksForDay(
        cellsInDay: List<Cell>,
        timestampsByCgi: Map<String, String>,
        csvDataByCgi: Map<String, CsvData>
    ): String {
        return cellsInDay.joinToString("") { cell ->
            cell.location?.coordinates?.let { coordinates ->
                val fullTimestamp = timestampsByCgi[cell.cgi] ?: "No timestamp"
                val timeOnly = fullTimestamp.substringAfter(" ")
                val heading = (cell.direction - 180) % 360

                // Searches data from the CSV. If no data, uses default values.
                val csvData = csvDataByCgi[cell.cgi]
                val iconColor = csvData?.color ?: getDefaultColorForTechnology(cell.technology)
                val target = csvData?.target ?: "N/A"
                val notes = csvData?.notes ?: "N/A"

                """
                <Placemark>
                    <name>${escapeXml(timeOnly)}</name>
                    <description>${escapeXml(createCellDescriptionWithTimestamp(cell, fullTimestamp, target, notes))}</description>
                    <Style>
                        <IconStyle>
                            <color>${escapeXml(iconColor)}</color>
                            <scale>1.2</scale>
                            <heading>$heading</heading>
                            <Icon>
                                <!--<href>http://maps.google.com/mapfiles/kml/pal5/icon6.png</href>-->
                                <href>http://maps.google.com/mapfiles/kml/pal4/icon57.png</href>
                            </Icon>
                        </IconStyle>
                    </Style>
                    <Point>
                        <altitudeMode>relativeToGround</altitudeMode>
                        <coordinates>${coordinates.x},${coordinates.y},50.0</coordinates>
                    </Point>
                </Placemark>
                """
            } ?: ""
        }
    }

    /**
     * Generates polygon placemarks for cell coverage areas within a specific day.
     *
     * Creates KML Polygon elements with transparent styling extruded above ground level.
     * Polygon colors are derived from CSV data with applied transparency for better map visibility.
     *
     * @param cellsInDay list of cells for the specific day
     * @param cellPolygons polygon geometries associated with the cells
     * @param timestampsByCgi temporal data for naming polygons
     * @param csvDataByCgi CSV metadata for styling and descriptions
     * @return KML Polygon Placemark elements as XML string
     */
    private fun generatePolygonsForDay(
        cellsInDay: List<Cell>,
        cellPolygons: Map<Long, List<CellPolygon>>,
        timestampsByCgi: Map<String, String>,
        csvDataByCgi: Map<String, CsvData>
    ): String {
        return cellsInDay.joinToString("") { cell ->
            cellPolygons[cell.id]?.joinToString("") { polygon ->
                polygon.polygon?.let { geom ->
                    val fullTimestamp = timestampsByCgi[cell.cgi] ?: "No timestamp"
                    val timeOnly = fullTimestamp.substringAfter(" ")
                    val csvData = csvDataByCgi[cell.cgi]
                    val polygonColor = makeColorTransparent(csvData?.color ?: "00ff00")
                    val target = csvData?.target ?: "N/A"
                    val notes = csvData?.notes ?: "N/A"

                    """
                    <Placemark>
                        <name>${escapeXml("${timeOnly} - Polygon")}</name>
                        <description>${escapeXml(createCellDescriptionWithTimestamp(cell, fullTimestamp, target, notes))}</description>
                        <Style>
                            <LineStyle>
                                <color>${escapeXml(polygonColor)}</color>
                                <width>2</width>
                            </LineStyle>
                            <PolyStyle>
                                <color>${escapeXml(polygonColor)}</color>
                                <fill>1</fill>
                                <outline>1</outline>
                            </PolyStyle>
                        </Style>
                        <Polygon>
                            <altitudeMode>relativeToGround</altitudeMode>
                            <extrude>1</extrude>
                            <outerBoundaryIs>
                                <LinearRing>
                                    <coordinates>
                                        ${geom.coordinates.joinToString(" ") { coord ->
                                            "${coord.x},${coord.y},50.0"
                                        }}
                                    </coordinates>
                                </LinearRing>
                            </outerBoundaryIs>
                        </Polygon>
                    </Placemark>
                    """
                } ?: ""
            } ?: ""
        }
    }

    /**
     * Applies transparency to a color hex value for polygon styling.
     *
     * Converts opaque colors to semi-transparent versions suitable for polygon fills
     * that don't obstruct underlying map features.
     *
     * @param colorHex the original color in hexadecimal format (6 or 8 characters)
     * @param transparency the transparency value (default: "4f" = ~30% opacity)
     * @return the color with applied transparency in aabbggrr format
     */
    private fun makeColorTransparent(colorHex: String, transparency: String = "4f"): String {
        val fullColor = if (colorHex.length == 6) "ff$colorHex" else colorHex  // Add alpha if missing
        // Replace the first 2 characters (alpha) with the transparency value
        return transparency + fullColor.substring(2)
    }

    /**
     * Creates a formatted description for cell placemarks with all relevant information.
     *
     * Generates a multi-line description containing target, notes, timestamp, CGI,
     * technology, name, band, and direction information for display in Google Earth
     * placemark popups.
     *
     * @param cell the cell data to describe
     * @param timestamp the recording timestamp
     * @param target the target or purpose from CSV
     * @param notes additional notes from CSV
     * @return formatted multi-line description string
     */
    private fun createCellDescriptionWithTimestamp(
        cell: Cell,
        timestamp: String,
        target: String,
        notes: String
    ): String =
        """
            Target: $target
            Notes: $notes
            Timestamp: $timestamp
            CGI: ${cell.cgi}
            Technology: ${getTechnologyName(cell.technology)}
            Name: ${cell.name ?: "N/A"}
            Band: ${cell.band?.band ?: "N/A"}
            Direction: ${cell.direction}°
        """.trimIndent()

    /**
     * Converts technology numeric codes to human-readable names.
     *
     * @param tech the technology code (2, 3, 4, 5, 10)
     * @return the technology name as string
     */
    private fun getTechnologyName(tech: Int): String {
        return when(tech) {
            2 -> "2G"
            3 -> "3G"
            4 -> "4G"
            5 -> "5G"
            10 -> "NR-IoT"
            else -> "Unknown"
        }
    }

    /**
     * Escapes XML special characters to prevent parsing errors.
     *
     * Handles the five predefined XML entities: &, <, >, ", '
     *
     * @param text the text to escape
     * @return the escaped text safe for XML embedding
     */
    private fun escapeXml(text: String): String {
        return text.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }

    /**
     * Provides default colors for cellular technologies when CSV color is not specified.
     *
     * @param tech the technology code
     * @return the default color in aabbggrr format
     */
    private fun getDefaultColorForTechnology(tech: Int): String =
        when(tech) {
            2 -> "ff0000ff" // Blue for 2G
            3 -> "ff00ff00" // Green for 3G
            4 -> "ffff0000" // Red for 4G
            5 -> "ff000000" // Black for 5G
            else -> "ffffffff" // White for unknown
        }
}