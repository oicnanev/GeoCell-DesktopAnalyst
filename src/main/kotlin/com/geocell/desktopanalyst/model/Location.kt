package com.geocell.desktopanalyst.model

import org.locationtech.jts.geom.Point

data class Location(
    val id: Long,
    val coordinates: Point?,
    val address: String?,
    val address1: String?,
    val zip4: Int,
    val zip3: Int,
    val postalDesignation: String?,
    val idCounty: Long?
)
