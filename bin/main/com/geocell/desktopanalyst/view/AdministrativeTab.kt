package com.geocell.desktopanalyst.view

import javafx.collections.FXCollections
import javafx.geometry.Insets
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.layout.BorderPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.text.Text

/**
 * Tab for querying cells within administrative regions (district and county).
 *
 * This tab allows users to:
 * - Select a district from a dropdown list
 * - Select a county from a dropdown list (filtered by selected district)
 * - Query the database for cells in the selected administrative region
 * - Display results in a text area
 *
 * @see MainController
 * @since 1.0.0
 */
class AdministrativeTab : BorderPane() {

    // UI Components
    private val titleText = Text("Cells in administrative region")
    
    // Dropdowns
    private val districtComboBox = ComboBox<String>().apply {
        promptText = "District"
        prefWidth = 120.0
        // TODO: Populate with actual districts from database
        items = FXCollections.observableArrayList(
            "Lisboa",
            "Porto", 
            "Braga",
            "Setúbal",
            "Aveiro",
            "Coimbra",
            "Faro",
            "Leiria",
            "Santarém",
            "Viseu"
        )
    }
    
    private val countyComboBox = ComboBox<String>().apply {
        promptText = "County"
        prefWidth = 133.0
        // Will be populated based on selected district
        isDisable = true
    }
    
    // Button
    private val queryButton = Button("Query Region").apply {
        prefHeight = 26.0
        prefWidth = 120.0
    }
    
    // Results
    private val resultsTextArea = TextArea().apply {
        prefHeight = 164.0
        prefWidth = 338.0
        isEditable = false
        style = "-fx-font-family: 'Monospaced';"
    }

    init {
        setupLayout()
        setupStyles()
        setupEventHandlers()
    }

    private fun setupLayout() {
        padding = Insets(10.0)

        // Top panel with title
        val topPanel = VBox(10.0).apply {
            children.add(titleText)
        }

        // Center panel with dropdowns and results
        val centerPanel = HBox(20.0).apply {
            // Left side - dropdowns
            val inputPanel = VBox(15.0).apply {
                children.addAll(
                    Label("District:"),
                    districtComboBox,
                    Label("County:"),
                    countyComboBox,
                    queryButton
                )
                padding = Insets(10.0)
            }

            // Right side - results area
            val resultsPanel = VBox(5.0).apply {
                children.addAll(
                    Label("Results:"),
                    resultsTextArea
                )
                VBox.setVgrow(resultsTextArea, Priority.ALWAYS)
            }

            children.addAll(inputPanel, resultsPanel)
            HBox.setHgrow(resultsPanel, Priority.ALWAYS)
        }

        // Set layout
        top = topPanel
        center = centerPanel
    }

    private fun setupStyles() {
        titleText.wrappingWidth = 363.0
        resultsTextArea.style = "-fx-background-color: #f5f5f5; -fx-border-color: #cccccc;"
    }

    private fun setupEventHandlers() {
        // District selection handler
        districtComboBox.setOnAction {
            val selectedDistrict = districtComboBox.value
            if (selectedDistrict != null) {
                updateCountyDropdown(selectedDistrict)
                countyComboBox.isDisable = false
            } else {
                countyComboBox.items.clear()
                countyComboBox.isDisable = true
            }
        }
        
        queryButton.setOnAction {
            queryAdministrativeRegion()
        }
    }

    private fun updateCountyDropdown(district: String) {
        // TODO: Fetch counties for selected district from database
        // This is a mock implementation
        val counties = when (district) {
            "Lisboa" -> listOf(
                "Lisboa",
                "Sintra",
                "Cascais",
                "Oeiras",
                "Loures",
                "Amadora",
                "Vila Franca de Xira"
            )
            "Porto" -> listOf(
                "Porto",
                "Matosinhos",
                "Vila Nova de Gaia",
                "Gondomar",
                "Maia",
                "Valongo"
            )
            "Braga" -> listOf(
                "Braga",
                "Guimarães",
                "Barcelos",
                "Famalicão",
                "Vizela"
            )
            else -> listOf("Select a district first")
        }
        
        countyComboBox.items = FXCollections.observableArrayList(counties)
        countyComboBox.value = null
    }

    fun queryAdministrativeRegion() {
        val district = districtComboBox.value
        val county = countyComboBox.value

        if (district == null) {
            resultsTextArea.text = "Error: Please select a district"
            return
        }

        if (county == null) {
            resultsTextArea.text = "Error: Please select a county"
            return
        }

        // TODO: Implement actual database query for administrative region
        resultsTextArea.text = "Querying administrative region:\n" +
                              "District: $district\n" +
                              "County: $county\n\n" +
                              "This feature is under development."
        
        println("Querying administrative region - District: $district, County: $county")
    }

    // Public methods for integration with controller
    fun setResults(content: String) {
        resultsTextArea.text = content
    }

    fun getDistrictComboBox(): ComboBox<String> = districtComboBox
    fun getCountyComboBox(): ComboBox<String> = countyComboBox
    fun getQueryButton(): Button = queryButton
    fun getResultsTextArea(): TextArea = resultsTextArea
}