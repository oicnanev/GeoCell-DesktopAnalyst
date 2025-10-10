package com.geocell.desktopanalyst.model

import org.locationtech.jts.geom.Polygon

data class CellPolygon(
    val id: Long,
    val polygon: Polygon?,
    val polygonShort: Polygon?,
    val cellId: Long?
)
