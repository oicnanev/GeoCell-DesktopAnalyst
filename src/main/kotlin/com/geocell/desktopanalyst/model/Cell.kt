package com.geocell.desktopanalyst.model

import java.time.LocalDate

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
    val mccMnc: MCCMNC?
)

