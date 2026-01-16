package com.geocell.desktopanalyst.view

import com.geocell.desktopanalyst.model.FilterParams
import javafx.application.Platform
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

    // Controller and MainView references
    private var controller: com.geocell.desktopanalyst.controller.MainController? = null
    private var mainView: MainView? = null
    private var databaseService: com.geocell.desktopanalyst.service.DatabaseService? = null

    // Store last query results for export
    private var lastQueryResults: List<com.geocell.desktopanalyst.model.domain.Cell> = emptyList()

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
        val dbService = databaseService
        if (dbService == null) {
            println("WARNING: DatabaseService not set, cannot fetch counties")
            countyComboBox.items = FXCollections.observableArrayList("Database not initialized")
            return
        }

        // Fetch counties from database in background thread
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val counties = dbService.getCountiesByDistrict(district)

                // Update UI on JavaFX thread
                withContext(Dispatchers.JavaFx) {
                    if (counties.isNotEmpty()) {
                        countyComboBox.items = FXCollections.observableArrayList(counties)
                        countyComboBox.value = null
                    } else {
                        countyComboBox.items = FXCollections.observableArrayList("No counties found")
                    }
                }
            } catch (e: Exception) {
                println("ERROR fetching counties: ${e.message}")
                e.printStackTrace()
                withContext(Dispatchers.JavaFx) {
                    countyComboBox.items = FXCollections.observableArrayList("Error loading counties")
                }
            }
        }
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

        // Check if controller is available
        if (controller == null) {
            resultsTextArea.text = "Error: Controller not initialized. Please restart the application."
            return
        }

        try {
            // Show processing message
            resultsTextArea.text = "Querying administrative region:\n" +
                    "District: $district\n" +
                    "County: $county\n" +
                    "Processing..."

            // Extract filters from main UI
            val filters = extractFiltersFromMainView()

            // Query the database through controller
            val cells = controller!!.queryCellsInAdministrativeRegion(
                districtName = district,
                countyName = county,
                filters = filters
            )

            // Store results for export
            lastQueryResults = cells

            // Format and display results
            resultsTextArea.text = formatResults(cells, district, county, filters)

            println("Querying administrative region - District: $district, County: $county")
            println("Filters applied: $filters")
            println("Found ${cells.size} cell(s)")

        } catch (e: IllegalArgumentException) {
            resultsTextArea.text = "Error: ${e.message}"
        } catch (e: Exception) {
            resultsTextArea.text = "Error querying database: ${e.message}"
            e.printStackTrace()
        }
    }

    private fun extractFiltersFromMainView(): FilterParams {
        val mainView = this.mainView
        if (mainView == null) {
            println("WARNING: MainView not set, using empty filters")
            return FilterParams()
        }

        // Extract technology filters
        val technologyCheckboxes = mainView.getTechnologyCheckboxes()
        val selectedTechnologies = technologyCheckboxes
            .filter { it.isSelected }
            .mapNotNull { checkbox ->
                when (checkbox.text) {
                    "2G" -> 2
                    "3G" -> 3
                    "4G" -> 4
                    "5G" -> 5
                    "NR-IoT" -> 10
                    else -> null
                }
            }

        // Extract operator filters
        val operatorCheckboxes = mainView.getOperatorCheckboxes()
        val selectedOperators = operatorCheckboxes
            .filter { it.isSelected }
            .map { it.text }

        // Extract date filters
        val datePickers = mainView.getDatePickers()
        val fromDate = datePickers.getOrNull(0)?.value?.toString()
        val toDate = datePickers.getOrNull(1)?.value?.toString()

        println("Extracted filters:")
        println("  Technologies: $selectedTechnologies")
        println("  Operators: $selectedOperators")
        println("  Date range: $fromDate to $toDate")

        return FilterParams(
            technologies = selectedTechnologies,
            operators = selectedOperators,
            startDate = fromDate,
            endDate = toDate
        )
    }

    private fun formatResults(
        cells: List<com.geocell.desktopanalyst.model.domain.Cell>,
        district: String,
        county: String,
        filters: FilterParams
    ): String {
        val header = "Administrative Region Query Results\n" +
                "District: $district\n" +
                "County: $county\n" +
                formatFiltersInfo(filters) +
                "Found ${cells.size} cell(s)\n" +
                "=".repeat(60) + "\n"

        if (cells.isEmpty()) {
            return header + "No cells found within the specified administrative region and filters."
        }

        val results = cells.mapIndexed { index, cell ->
            "${index + 1}. CGI: ${cell.cgi ?: "N/A"}\n" +
                    "   Technology: ${getTechnologyName(cell.technology)}\n" +
                    "   Operator: ${cell.mccMnc?.operator ?: "N/A"}\n" +
                    "   Name: ${cell.name ?: "N/A"}\n" +
                    "   LAC/TAC: ${cell.lacTac}\n" +
                    "   eCI/nCI: ${cell.eciNci ?: "N/A"}\n" +
                    "   Band: ${cell.band?.band ?: "N/A"}\n" +
                    "   Location: ${formatLocation(cell.location)}\n"
        }.joinToString("\n")

        return header + results
    }

    private fun formatFiltersInfo(filters: FilterParams): String {
        val info = StringBuilder()

        if (filters.technologies.isNotEmpty()) {
            val techNames = filters.technologies.map { getTechnologyName(it) }
            info.append("Technologies: ${techNames.joinToString(", ")}\n")
        }

        if (filters.operators.isNotEmpty()) {
            info.append("Operators: ${filters.operators.joinToString(", ")}\n")
        }

        if (filters.sameNetwork) {
            info.append("Same network only: Yes\n")
        }

        if (filters.startDate != null || filters.endDate != null) {
            info.append("Date range: ${filters.startDate ?: "Any"} to ${filters.endDate ?: "Any"}\n")
        }

        return info.toString()
    }

    private fun getTechnologyName(technologyCode: Int): String {
        return when (technologyCode) {
            2 -> "2G"
            3 -> "3G"
            4 -> "4G"
            5 -> "5G"
            10 -> "NR-IoT"
            else -> "Unknown ($technologyCode)"
        }
    }

    private fun formatLocation(location: com.geocell.desktopanalyst.model.domain.Location?): String {
        if (location == null) return "No location data"

        val coords = location.coordinates
        val address = location.address
        val postal = location.postalDesignation

        return if (coords != null) {
            val coordStr = "(${coords.y}, ${coords.x})"
            val addressStr = if (address != null) "$address, " else ""
            val postalStr = if (postal != null) postal else ""
            "$addressStr$postalStr $coordStr"
        } else {
            "No coordinates"
        }
    }

    // Public methods for integration with controller and main view
    fun setController(controller: com.geocell.desktopanalyst.controller.MainController) {
        this.controller = controller
    }

    fun setMainView(mainView: MainView) {
        this.mainView = mainView
    }

    fun setDatabaseService(databaseService: com.geocell.desktopanalyst.service.DatabaseService) {
        this.databaseService = databaseService
    }

    fun loadDistricts() {
        val dbService = databaseService
        if (dbService == null) {
            println("WARNING: DatabaseService not set, cannot load districts")
            districtComboBox.items = FXCollections.observableArrayList("Database not initialized")
            return
        }

        // Fetch districts from database in background thread
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val districts = dbService.getAllDistricts()

                // Update UI on JavaFX thread
                withContext(Dispatchers.JavaFx) {
                    if (districts.isNotEmpty()) {
                        districtComboBox.items = FXCollections.observableArrayList(districts)
                        println("Loaded ${districts.size} districts into dropdown")
                    } else {
                        districtComboBox.items = FXCollections.observableArrayList("No districts found")
                    }
                }
            } catch (e: Exception) {
                println("ERROR fetching districts: ${e.message}")
                e.printStackTrace()
                withContext(Dispatchers.JavaFx) {
                    districtComboBox.items = FXCollections.observableArrayList("Error loading districts")
                }
            }
        }
    }

    fun setResults(content: String) {
        resultsTextArea.text = content
    }

    fun getDistrictComboBox(): ComboBox<String> = districtComboBox
    fun getCountyComboBox(): ComboBox<String> = countyComboBox
    fun getQueryButton(): Button = queryButton
    fun getResultsTextArea(): TextArea = resultsTextArea
    fun getLastQueryResults(): List<com.geocell.desktopanalyst.model.domain.Cell> = lastQueryResults
}