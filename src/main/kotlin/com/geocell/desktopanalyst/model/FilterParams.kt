package com.geocell.desktopanalyst.model

/**
 * Data class representing filter parameters for cell queries.
 *
 * This class encapsulates all possible filter criteria that can be applied
 * when querying cellular network data. It supports filtering by technology,
 * operator, network type, and date ranges.
 *
 * @property technologies list of technology codes to filter (2=2G, 3=3G, 4=4G, 5=5G, 10=NR-IoT)
 * @property operators list of operator names to filter (MEO, NOS, Vodafone, etc.)
 * @property sameNetwork filter to only show cells from the same network operator as reference
 * @property startDate filter cells created after this date (format: YYYY-MM-DD)
 * @property endDate filter cells created before this date (format: YYYY-MM-DD)
 *
 * @sample
 * ```
 * val filters = FilterParams(
 *     technologies = listOf(4, 5), // 4G and 5G only
 *     operators = listOf("MEO", "NOS"),
 *     sameNetwork = false,
 *     startDate = "2023-01-01",
 *     endDate = "2023-12-31"
 * )
 * ```
 *
 * @since 1.0.0
 */
data class FilterParams(
    val technologies: List<Int> = emptyList(),
    val operators: List<String> = emptyList(),
    val sameNetwork: Boolean = false,
    val startDate: String? = null,
    val endDate: String? = null
)