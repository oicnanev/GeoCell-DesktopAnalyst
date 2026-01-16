package com.geocell.desktopanalyst.service

import com.geocell.desktopanalyst.model.domain.Cell
import com.geocell.desktopanalyst.model.domain.CellPolygon
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Service class for generating KMZ files from query results.
 *
 * This generator creates KMZ files from database query results with:
 * - CGI-based naming for placemarks
 * - Operator-based colors (MEO=blue, NOS=orange, Vodafone=red, DIGI=cyan)
 * - Complete database field information in descriptions
 * - All cells in a single folder (no date-based organization)
 *
 * @see Cell
 * @see CellPolygon
 * @since 1.0.0
 */
class QueryResultKmzGenerator {

    /**
     * Generates a KMZ file from query results.
     *
     * @param cells map of Cell Global Identity to Cell objects
     * @param cellPolygons map of cell IDs to their associated polygon geometries
     * @param outputFile the destination file where the KMZ will be written
     */
    fun generateKmz(
        cells: Map<String, Cell>,
        cellPolygons: Map<Long, List<CellPolygon>>,
        outputFile: File
    ) {
        ZipOutputStream(outputFile.outputStream()).use { zos ->
            zos.putNextEntry(ZipEntry("doc.kml"))

            val kmlContent = """
<?xml version="1.0" encoding="UTF-8"?>
<kml xmlns="http://www.opengis.net/kml/2.2">
<Document>
    <name>Query Results Export</name>
    <description>Generated from database query</description>

    <!-- Folder for Points -->
    <Folder>
        <name>Points</name>
        <description>Cell locations</description>
        ${generatePlacemarks(cells)}
    </Folder>

    <!-- Folder for Polygons -->
    <Folder>
        <name>Polygons</name>
        <description>Cell coverage areas</description>
        ${generatePolygons(cells, cellPolygons)}
    </Folder>
</Document>
</kml>
            """.trimIndent()

            zos.write(kmlContent.toByteArray(Charsets.UTF_8))
            zos.closeEntry()
        }
    }

    /**
     * Generates point placemarks for cells.
     */
    private fun generatePlacemarks(cells: Map<String, Cell>): String {
        return cells.values.joinToString("") { cell ->
            cell.location?.coordinates?.let { coordinates ->
                val heading = (cell.direction - 180) % 360
                val iconColor = getColorForOperator(cell.mccMnc?.brand)

                """
                <Placemark>
                    <name>${escapeXml(cell.cgi ?: "Unknown")}</name>
                    <description>${escapeXml(createCellDescription(cell))}</description>
                    <Style>
                        <IconStyle>
                            <color>${escapeXml(iconColor)}</color>
                            <scale>1.2</scale>
                            <heading>$heading</heading>
                            <Icon>
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
     * Generates polygon placemarks for cell coverage areas.
     */
    private fun generatePolygons(
        cells: Map<String, Cell>,
        cellPolygons: Map<Long, List<CellPolygon>>
    ): String {
        return cells.values.joinToString("") { cell ->
            cellPolygons[cell.id]?.joinToString("") { polygon ->
                polygon.polygon?.let { geom ->
                    val polygonColor = makeColorTransparent(getColorForOperator(cell.mccMnc?.brand))

                    """
                    <Placemark>
                        <name>${escapeXml("${cell.cgi ?: "Unknown"} - Polygon")}</name>
                        <description>${escapeXml(createCellDescription(cell))}</description>
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
     * Creates a comprehensive description with all database fields.
     */
    private fun createCellDescription(cell: Cell): String {
        val parts = mutableListOf<String>()

        // Basic identification
        parts.add("CGI: ${cell.cgi ?: "N/A"}")
        parts.add("Paragon CGI: ${cell.paragonCgi ?: "N/A"}")
        parts.add("Technology: ${getTechnologyName(cell.technology)}")

        // Operator information
        cell.mccMnc?.let { mccMnc ->
            parts.add("Operator: ${mccMnc.operator ?: "N/A"}")
            parts.add("Brand: ${mccMnc.brand ?: "N/A"}")
            parts.add("MCC-MNC: ${mccMnc.mcc}-${mccMnc.mnc}")
        }

        // Cell details
        parts.add("Name: ${cell.name ?: "N/A"}")
        parts.add("LAC/TAC: ${cell.lacTac ?: "N/A"}")
        cell.ci?.let { parts.add("CI: $it") }
        parts.add("eCI/nCI: ${cell.eciNci ?: "N/A"}")
        parts.add("eNB/gNB ID: ${cell.enbGnbId ?: "N/A"}")

        // Frequency information
        cell.band?.let { band ->
            parts.add("Band: ${band.band ?: "N/A"}")
            parts.add("EARFCN: ${band.earfcn ?: "N/A"}")
            parts.add("Bandwidth: ${band.bandwidth ?: "N/A"} MHz")
            parts.add("Uplink Freq: ${band.uplinkFreq ?: "N/A"} MHz")
            parts.add("Downlink Freq: ${band.downlinkFreq ?: "N/A"} MHz")
        }

        // Physical characteristics
        parts.add("Direction: ${cell.direction}°")

        // Location information
        cell.location?.let { location ->
            location.coordinates?.let { coords ->
                parts.add("Coordinates: ${coords.y}, ${coords.x}")
            }
            location.address?.let { parts.add("Address: $it") }
            location.address1?.let { parts.add("Address 1: $it") }
            location.postalDesignation?.let { parts.add("Postal: $it") }
            if (location.zip4 != null && location.zip3 != null) {
                parts.add("ZIP: ${location.zip3}-${location.zip4}")
            }
        }

        // Dates
        parts.add("Created: ${cell.created}")
        parts.add("Modified: ${cell.modified}")

        // Distance (if available from neighbor queries)
        cell.distanceFromReference?.let {
            parts.add("Distance: ${"%.2f".format(it)} km")
        }

        return parts.joinToString("\n")
    }

    /**
     * Returns color based on operator brand.
     * MEO = blue, NOS = orange, Vodafone = red, DIGI = cyan
     * Format: aabbggrr (KML format)
     */
    private fun getColorForOperator(brand: String?): String {
        return when (brand?.uppercase()) {
            "MEO" -> "ffff0000"      // Blue
            "NOS" -> "ff0080ff"      // Orange
            "VODAFONE" -> "ff0000ff" // Red
            "DIGI" -> "ffffff00"     // Cyan
            else -> "ff00ff00"       // Green (default)
        }
    }

    /**
     * Applies transparency to a color hex value for polygon styling.
     */
    private fun makeColorTransparent(colorHex: String, transparency: String = "4f"): String {
        val fullColor = if (colorHex.length == 6) "ff$colorHex" else colorHex
        return transparency + fullColor.substring(2)
    }

    /**
     * Converts technology numeric codes to human-readable names.
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
     */
    private fun escapeXml(text: String): String {
        return text.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }
}
