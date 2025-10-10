package com.geocell.desktopanalyst.model

import de.micromata.opengis.kml.v_2_2_0.xal.AddressDetails
import org.locationtech.jts.geom.Polygon

data class District(
    val id: String?,
    val district: String?,
    val polygon: Polygon?,
    val country: Country?
)
