package com.geocell.desktopanalyst.model.domain

import java.time.LocalDate

/**
 * Data class representing a cellular network element with complete technical and geographical data.
 *
 * This domain model encapsulates all information about a cellular network element including
 * identification, technology specifications, location data, frequency bands, and temporal metadata.
 * It serves as the central entity for cellular infrastructure data throughout the application.
 *
 * The class supports multiple cellular technologies (2G, 3G, 4G, 5G, NR-IoT) and contains
 * both technical specifications and visualization metadata for KMZ generation.
 *
 * @property id the unique database identifier for the cell record
 * @property lacTac the Location Area Code (2G/3G) or Tracking Area Code (4G/5G) identifier
 * @property ci the Cell Identity for 2G/3G networks (optional)
 * @property eciNci the E-UTRAN Cell Identity for 4G or NR Cell Identity for 5G networks
 * @property cgi the Cell Global Identity - unique worldwide identifier for the cell
 * @property paragonCgi alternative CGI format used in Paragon systems
 * @property technology the technology generation code (2=2G, 3=3G, 4=4G, 5=5G, 10=NR-IoT)
 * @property direction the antenna direction in degrees (0-359) where 0° is North
 * @property name the human-readable name or description of the cell
 * @property created the date when this cell record was initially created in the system
 * @property modified the date when this cell record was last updated
 * @property band the frequency band information and technical specifications
 * @property enbGnbId the eNodeB identifier for 4G or gNodeB identifier for 5G
 * @property location the geographical coordinates and address information
 * @property mccMnc the Mobile Country Code and Mobile Network Code operator information
 * @property color the visualization color in KML hexadecimal format (aabbggrr) from CSV
 * @property target the purpose or target classification from CSV metadata
 * @property notes additional descriptive information or comments from CSV
 *
 * @sample
 * ```
 * val cell = Cell(
 *     id = 12345,
 *     lacTac = "1250",
 *     eciNci = "678901234",
 *     cgi = "268-06-8840-8453",
 *     paragonCgi = "2680688408453",
 *     technology = 4,
 *     direction = 90,
 *     name = "Lisbon Downtown Sector A",
 *     created = LocalDate.of(2023, 1, 15),
 *     modified = LocalDate.of(2024, 3, 20),
 *     band = Band("B20", 10.0, 832.0, 791.0, 6150.0),
 *     enbGnbId = 8840,
 *     location = Location(/* ... */),
 *     mccMnc = MCCMNC(268, 6, "Vodafone Portugal"),
 *     color = "ff0000ff",
 *     target = "Urban Coverage",
 *     notes = "High capacity sector for downtown area"
 * )
 * ```
 *
 * @see Band
 * @see Location
 * @see MCCMNC
 * @since 1.0.0
 */
data class Cell(
    val id: Long,
    val lacTac: String,
    val ci: String? = null,
    val eciNci: String?,
    val cgi: String?,
    val paragonCgi: String?,
    val technology: Int,
    val direction: Int,
    val name: String?,
    val created: LocalDate,
    val modified: LocalDate,
    val band: Band?,
    val enbGnbId: Long?,
    val location: Location?,
    val mccMnc: MCCMNC?,
    val color: String? = null,
    val target: String? = null,
    val notes: String? = null,
    var distanceFromReference: Double? = null
)