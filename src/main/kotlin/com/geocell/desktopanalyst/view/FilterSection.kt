package com.geocell.desktopanalyst.view

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.CheckBox
import javafx.scene.control.DatePicker
import javafx.scene.control.Label
import javafx.scene.control.Separator
import javafx.scene.layout.GridPane
import javafx.scene.layout.VBox
import javafx.scene.text.Font

class FilterSection: GridPane() {
    private val technologyCheckboxes = mutableListOf<CheckBox>()
    private val operatorCheckboxes = mutableListOf<CheckBox>()
    private val datePickers = mutableListOf<DatePicker>()
    private lateinit var exportButton: Button

    init {
        setupLayout()
        setupStyle()
    }

    private fun setupLayout() {
        padding = Insets(15.0)
        hgap = 20.0
        vgap = 10.0

        // Label filters
        val filtersLabel = Label("Filters").apply {
            font = Font(14.0)
        }
        add(filtersLabel, 0, 0)

        // Horizontal separator
        val separator = Separator().apply {
            prefWidth = 385.0
        }
        add(separator, 0, 1, 6, 1) // Corrigido: span de 6 colunas

        // Technology section
        setupTechnologySection()

        // Vertical separator 1
        val vertSeparator1 = Separator().apply {
            orientation = javafx.geometry.Orientation.VERTICAL
            prefHeight = 104.0
        }
        add(vertSeparator1, 1, 2, 1, 2)

        // Operator section
        setupOperatorSection()

        // Vertical separator 2 (CORRIGIDO: usar variável diferente)
        val vertSeparator2 = Separator().apply {
            orientation = javafx.geometry.Orientation.VERTICAL
            prefHeight = 104.0
        }
        add(vertSeparator2, 3, 2, 1, 2) // Corrigido: coluna 3

        // Between dates section
        setupDatesSection()

        // Buttons
        setupButtonsSection()
    }

    private fun setupTechnologySection() {
        val techLabel = Label("Technology:").apply { // Corrigido: adicionar ":"
            font = Font(14.0)
        }
        add(techLabel, 0, 2)

        val techBox = VBox(5.0).apply {
            children.addAll(
                CheckBox("2G").also { technologyCheckboxes.add(it) },
                CheckBox("3G").also { technologyCheckboxes.add(it) },
                CheckBox("4G").also { technologyCheckboxes.add(it) },
                CheckBox("5G").also { technologyCheckboxes.add(it) },
                CheckBox("NR-IoT").also { technologyCheckboxes.add(it) }
            )
        }
        add(techBox, 0, 3)
    }

    private fun setupOperatorSection() {
        val operatorLabel = Label("Operator:").apply { // Corrigido: adicionar ":"
            font = Font(14.0)
        }
        add(operatorLabel, 2, 2)

        val operatorBox = VBox(5.0).apply {
            children.addAll(
                CheckBox("MEO").also { operatorCheckboxes.add(it) },
                CheckBox("NOS").also { operatorCheckboxes.add(it) },
                CheckBox("Vodafone").also { operatorCheckboxes.add(it) },
                CheckBox("DIGI").also { operatorCheckboxes.add(it) }
            )
        }
        add(operatorBox, 2, 3)
    }

    private fun setupDatesSection() {
        val datesLabel = Label("Between dates:").apply {
            font = Font(14.0)
        }
        add(datesLabel, 4, 2)

        val datesBox = GridPane().apply {
            hgap = 10.0
            vgap = 10.0

            add(Label("From:"), 0, 0)
            add(DatePicker().also { datePickers.add(it) }, 1, 0)
            add(Label("Until:"), 0, 1)
            add(DatePicker().also { datePickers.add(it) }, 1, 1)
        }
        add(datesBox, 4, 3)
    }

    private fun setupButtonsSection() {
        val buttonsBox = VBox(10.0).apply {
            alignment = Pos.CENTER_RIGHT
            children.addAll(
                Button("Export to KMZ").apply {
                    exportButton = this
                }
            )
        }
        add(buttonsBox, 5, 2, 1, 2)
    }

    private fun setupStyle() {
        // additional styles if necessary
    }

    // Public Access Methods
    fun getTechnologyCheckboxes(): List<CheckBox> = technologyCheckboxes
    fun getOperatorCheckboxes(): List<CheckBox> = operatorCheckboxes
    fun getDatePickers(): List<DatePicker> = datePickers
    fun getExportButton(): Button = exportButton
}