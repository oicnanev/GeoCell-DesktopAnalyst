package com.geocell.desktopanalyst.model

import java.time.LocalDate

data class TechnologyFilter(
    val g2: Boolean = false,
    val g3: Boolean = false,
    val g4: Boolean = false,
    val g5: Boolean = false,
    val nrIoT: Boolean = false
) {
    fun toTechnologyIds(): List<Int> {
        val technologies = mutableListOf<Int>()
        if (g2) technologies.add(2)
        if (g3) technologies.add(3)
        if (g4) technologies.add(4)
        if (g5) technologies.add(5)
        if (nrIoT) technologies.add(99) // Assuming 99 for NR-IoT
        return technologies
    }
}

data class OperatorFilter(
    val meo: Boolean = false,
    val nos: Boolean = false,
    val vodafone: Boolean = false,
    val digi: Boolean = false
) {
    fun toMccMncList(): List<String> {
        // Map operators to their MCC-MNC prefixes
        val operators = mutableListOf<String>()
        if (meo) operators.add("268-03") // MEO Portugal
        if (nos) operators.add("268-06") // NOS Portugal
        if (vodafone) operators.add("268-01") // Vodafone Portugal
        if (digi) operators.add("226-05") // DIGI Romania (exemplo, ajuste conforme necessário)
        return operators
    }
}

data class DateFilter(
    val from: LocalDate? = null,
    val to: LocalDate? = null
) {
    val isEnabled: Boolean
        get() = from != null && to != null
}

data class NeighborFilters(
    val technology: TechnologyFilter = TechnologyFilter(),
    val operator: OperatorFilter = OperatorFilter(),
    val dates: DateFilter = DateFilter(),
    val sameNetwork: Boolean = false
)