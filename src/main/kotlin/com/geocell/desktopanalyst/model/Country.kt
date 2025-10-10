package com.geocell.desktopanalyst.model

import org.locationtech.jts.geom.Polygon
import java.awt.Image

data class Country(
    val  name: String?,
    val code: String?,
    val polygon: Polygon?,
    val flag: Image?
)
