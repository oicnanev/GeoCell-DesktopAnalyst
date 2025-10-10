package com.geocell.desktopanalyst.model

data class MCCMNC(
    val type: String?,
    val mcc: Int,
    val mnc: Int,
    val operator: String?,
    val country: Country?,
    val brand: String?,
    val status: String?,
    val bands: String?,
    val notes: String?,
)
