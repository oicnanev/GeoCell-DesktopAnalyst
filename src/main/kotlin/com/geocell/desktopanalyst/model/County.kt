package com.geocell.desktopanalyst.model

import org.locationtech.jts.geom.Polygon

data class County(
    val idCounty: String?,
    val county: String?,
    val polygon: Polygon?,
    val district: District?
)
