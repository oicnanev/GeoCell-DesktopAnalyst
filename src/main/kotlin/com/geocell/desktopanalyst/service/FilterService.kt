package com.geocell.desktopanalyst.service

import com.geocell.desktopanalyst.model.DateFilter
import com.geocell.desktopanalyst.model.NeighborFilters
import com.geocell.desktopanalyst.model.OperatorFilter
import com.geocell.desktopanalyst.model.TechnologyFilter

import javafx.scene.control.CheckBox
import javafx.scene.control.DatePicker
import java.time.LocalDate

class FilterService {

    fun extractFiltersFromUI(
        technologyCheckboxes: List<CheckBox>,
        operatorCheckboxes: List<CheckBox>,
        datePickers: List<DatePicker>
    ): NeighborFilters {
        return NeighborFilters(
            technology = extractTechnologyFilter(technologyCheckboxes),
            operator = extractOperatorFilter(operatorCheckboxes),
            dates = extractDateFilter(datePickers)
        )
    }

    private fun extractTechnologyFilter(checkboxes: List<CheckBox>): TechnologyFilter {
        return TechnologyFilter(
            g2 = checkboxes.getOrNull(0)?.isSelected ?: false,
            g3 = checkboxes.getOrNull(1)?.isSelected ?: false,
            g4 = checkboxes.getOrNull(2)?.isSelected ?: false,
            g5 = checkboxes.getOrNull(3)?.isSelected ?: false,
            nrIoT = checkboxes.getOrNull(4)?.isSelected ?: false
        )
    }

    private fun extractOperatorFilter(checkboxes: List<CheckBox>): OperatorFilter {
        return OperatorFilter(
            meo = checkboxes.getOrNull(0)?.isSelected ?: false,
            nos = checkboxes.getOrNull(1)?.isSelected ?: false,
            vodafone = checkboxes.getOrNull(2)?.isSelected ?: false,
            digi = checkboxes.getOrNull(3)?.isSelected ?: false
        )
    }

    private fun extractDateFilter(datePickers: List<DatePicker>): DateFilter {
        return DateFilter(
            from = datePickers.getOrNull(0)?.value,
            to = datePickers.getOrNull(1)?.value
        )
    }
}