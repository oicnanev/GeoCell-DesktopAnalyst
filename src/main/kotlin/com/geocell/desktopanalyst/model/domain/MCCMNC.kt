package com.geocell.desktopanalyst.model.domain

/**
 * Data class representing Mobile Country Code (MCC) and Mobile Network Code (MNC) information.
 *
 * This domain model encapsulates the international identification system for mobile network operators,
 * providing standardized codes that uniquely identify operators worldwide. MCC/MNC codes are essential
 * for roaming, network selection, and operator identification in cellular networks.
 *
 * The class includes both the technical codes and associated business information such as operator names,
 * brands, and operational status, providing a complete picture of mobile network operators.
 *
 * @property type the type of network technology or service (e.g., "GSM", "UMTS", "LTE", "NR")
 * @property mcc the Mobile Country Code (3 digits) identifying the country
 * @property mnc the Mobile Network Code (2-3 digits) identifying the network operator within the country
 * @property operator the full name of the mobile network operator
 * @property country the associated [Country] entity for this MCC
 * @property brand the commercial brand name used by the operator
 * @property status the operational status of the network (e.g., "Operational", "Inactive", "Planned")
 * @property bands the frequency bands supported by this operator
 * @property notes additional information or comments about the operator
 *
 * @sample
 * ```
 * val vodafonePortugal = MCCMNC(
 *     type = "LTE",
 *     mcc = 268,
 *     mnc = 6,
 *     operator = "Vodafone Portugal",
 *     country = portugal,
 *     brand = "Vodafone",
 *     status = "Operational",
 *     bands = "B3, B7, B20, B28",
 *     notes = "Leading operator in urban areas"
 * )
 * ```
 *
 * @sample
 * ```
 * val meo = MCCMNC(
 *     type = "LTE/NR",
 *     mcc = 268,
 *     mnc = 3,
 *     operator = "MEO - Serviços de Comunicações e Multimédia",
 *     country = portugal,
 *     brand = "MEO",
 *     status = "Operational",
 *     bands = "B1, B3, B7, B20, B28, n78",
 *     notes = "Altice Portugal group"
 * )
 * ```
 *
 * @see Country
 * @see Cell
 * @since 1.0.0
 */
data class MCCMNC(
    val type: String?,
    val mcc: Int?,
    val mnc: Int?,
    val operator: String?,
    val country: Country?,
    val brand: String?,
    val status: String?,
    val bands: String?,
    val notes: String?,
)