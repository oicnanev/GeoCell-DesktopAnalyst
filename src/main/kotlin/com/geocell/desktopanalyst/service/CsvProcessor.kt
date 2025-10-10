package com.geocell.desktopanalyst.service

import com.geocell.desktopanalyst.util.ColorConverter
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import java.io.File

/**
 * Data class representing a parsed CSV record for cell data.
 *
 * This record contains all the information extracted from a single row in the input CSV file,
 * including temporal, geographical, and metadata about cellular network elements.
 *
 * @property timestamp the date and time when the cell data was recorded
 * @property cgi the Cell Global Identity identifier for the cellular element
 * @property color the visual representation color in KML hexadecimal format (aabbggrr)
 * @property target the target or purpose of this cell record
 * @property notes additional notes or comments about the cell
 *
 * @see CsvProcessor
 * @since 1.0.0
 */
data class CsvRecord(
    val timestamp: String,
    val cgi: String,
    val color: String?,
    val target: String?,
    val notes: String?
)

/**
 * Service class for parsing CSV files containing cellular network data.
 *
 * This processor handles the extraction and transformation of CSV data into structured
 * [CsvRecord] objects, including color conversion to KML format and default value handling.
 *
 * The expected CSV format should include the following columns:
 * - `timestamp`: Date and time of recording (e.g., "2025/04/26 00:02:34")
 * - `cgi`: Cell Global Identity identifier
 * - `color`: Color name or value (converted to KML hex format)
 * - `target`: Target or purpose of the cell
 * - `notes`: Additional notes or comments
 *
 * @constructor Creates a new CSV processor instance
 * @see CsvRecord
 * @see ColorConverter
 * @since 1.0.0
 */
class CsvProcessor {

    /**
     * Parses a CSV file and converts it into a list of structured cell records.
     *
     * This function reads the entire CSV file, maps column values to [CsvRecord] properties,
     * and applies appropriate transformations including:
     * - Color name conversion to KML hexadecimal format
     * - Default value assignment for missing optional fields
     * - String trimming and normalization
     *
     * @param file the CSV file to parse
     * @return a list of [CsvRecord] objects representing each row in the CSV file
     * @throws Exception if the file cannot be read or parsed
     *
     * @sample
     * ```
     * val processor = CsvProcessor()
     * val records = processor.parseCsv(File("cells.csv"))
     * // records: List<CsvRecord>
     * ```
     *
     * CSV Example:
     * ```
     * timestamp,cgi,color,target,notes
     * 2025/04/26 00:02:34,268-06-8840-8453,red,coverage,Initial deployment
     * 2025/04/26 00:03:34,268-03-8521869,green,capacity,High traffic area
     * ```
     */
    fun parseCsv(file: File): List<CsvRecord> =
        csvReader().readAllWithHeader(file).map { row ->
            CsvRecord(
                timestamp   = row["timestamp"] ?: "",
                cgi         = row["cgi"] ?: "",
                color       = ColorConverter.convertColorToKmlHex(row["color"] ?: "red"),
                target      = row["target"] ?: "unknown",
                notes       = row["notes"] ?: ""
            )
        }
}