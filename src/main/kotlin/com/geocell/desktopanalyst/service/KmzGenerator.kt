package com.geocell.desktopanalyst.service

import com.geocell.desktopanalyst.model.Cell
import com.geocell.desktopanalyst.model.CellPolygon
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class KmzGenerator {
    fun generateKmz(
        cells: Map<String, Cell>,
        cellPolygons: Map<Long, List<CellPolygon>>,
        timestampsByCgi: Map<String, String>,
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
    
    ${generateStyles()}
    ${generatePlacemarks(cells, timestampsByCgi)}
    ${generatePolygons(cellPolygons)}
    
</Document>
</kml>
            """.trimIndent()

            zos.write(kmlContent.toByteArray(Charsets.UTF_8))
            zos.closeEntry()
        }
    }

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

    private fun generateStyles(): String {
        return """
        <Style id="polygonStyle">
            <LineStyle>
                <color>7f00ffff</color>
                <width>2</width>
            </LineStyle>
            <PolyStyle>
                <color>7f00ff00</color>
                <fill>1</fill>
                <outline>1</outline>
            </PolyStyle>
        </Style>
        ${(2..5).joinToString("") { tech ->
            """
            <Style id="cellStyle$tech">
                <IconStyle>
                    <color>${
                        when(tech) {
                            2 -> "ff0000ff" // Blue for 2G
                            3 -> "ff00ff00" // Green for 3G  
                            4 -> "ffff0000" // Red for 4G
                            5 -> "ff000000" // Black for 5G
                            else -> "ffffffff"
                        }
                    }</color>
                    <scale>1.2</scale>
                    <Icon>
                        <href>http://maps.google.com/mapfiles/kml/pal4/icon57.png</href>
                    </Icon>
                </IconStyle>
            </Style>
            """
        }}
    """.trimIndent()
    }

    private fun generatePlacemarks(cells: Map<String, Cell>, timestampsByCgi: Map<String, String>): String {
        return cells.values.joinToString("") { cell ->
            cell.location?.coordinates?.let { coordinates ->
                val timestamp = timestampsByCgi[cell.cgi] ?: "No timestamp"
                """
                <Placemark>
                    <name>${escapeXml(timestamp)}</name>
                    <description>${escapeXml(createCellDescriptionWithTimestamp(cell, timestamp))}</description>
                    <styleUrl>#cellStyle${cell.technology}</styleUrl>
                    <Point>
                        <coordinates>${coordinates.x},${coordinates.y},0</coordinates>
                    </Point>
                </Placemark>
                """
            } ?: ""
        }
    }

    private fun createCellDescriptionWithTimestamp(cell: Cell, timestamp: String): String {
        return """
            Timestamp: $timestamp
            CGI: ${cell.cgi}
            Technology: ${getTechnologyName(cell.technology)}
            Name: ${cell.name ?: "N/A"}
            Operator: ${cell.mccMnc?.operator ?: "N/A"}
            Band: ${cell.band?.band ?: "N/A"}
            Direction: ${cell.direction}°
        """.trimIndent()
    }

    private fun generatePolygons(cellPolygons: Map<Long, List<CellPolygon>>): String {
        return cellPolygons.entries.joinToString("") { (cellId, polygons) ->
            polygons.joinToString("") { polygon ->
                polygon.polygon?.let { geom ->
                    """
                    <Placemark>
                        <name>${cellId}</name>
                        <styleUrl>#polygonStyle</styleUrl>
                        <Polygon>
                            <outerBoundaryIs>
                                <LinearRing>
                                    <coordinates>
                                        ${geom.coordinates.joinToString(" ") { coord ->
                                            "${coord.x},${coord.y},0"
                                        }}
                                    </coordinates>
                                </LinearRing>
                            </outerBoundaryIs>
                        </Polygon>
                    </Placemark>
                    """
                } ?: ""
            }
        }
    }

    private fun escapeXml(text: String): String {
        return text.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }
}