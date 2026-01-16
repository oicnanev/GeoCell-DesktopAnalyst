package com.geocell.desktopanalyst.view

import com.geocell.desktopanalyst.model.FilterParams
import javafx.geometry.Insets
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.layout.BorderPane
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.text.Text

/**
 * Tab for querying cells by network attributes (LAC/TAC, eNB/gNB, Band).
 *
 * This tab allows users to:
 * - Search cells by LAC/TAC (Location Area Code / Tracking Area Code)
 * - Search cells by eNB/gNB identifier
 * - Search cells by frequency band
 * - Display results in a text area
 *
 * @see MainController
 * @since 1.0.0
 */
class NetworkTab : BorderPane() {

    // UI Components
    private val titleText = Text("Cells by network attributes")
    
    // Input fields
    private val lacTacTextField = TextField().apply {
        promptText = "LAC / TAC"
        prefWidth = 114.0
    }
    private val enbGnbTextField = TextField().apply {
        promptText = "eNB / gNB"
        prefWidth = 114.0
    }
    private val bandTextField = TextField().apply {
        promptText = "Band"
        prefWidth = 116.0
    }
    
    // Buttons
    private val queryLacTacButton = Button("Query by LAC/TAC").apply {
        prefHeight = 26.0
        prefWidth = 120.0
    }
    private val queryEnbGnbButton = Button("Query by eNB/gNB").apply {
        prefHeight = 26.0
        prefWidth = 120.0
    }
    private val queryBandButton = Button("Query by Band").apply {
        prefHeight = 26.0
        prefWidth = 100.0
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

        // Center panel with input fields and results
        val centerPanel = HBox(20.0).apply {
            // Left side - input fields
            val inputPanel = VBox(15.0).apply {
                children.addAll(
                    GridPane().apply {
                        hgap = 10.0
                        vgap = 10.0
                        add(Label("LAC/TAC:"), 0, 0)
                        add(lacTacTextField, 1, 0)
                        add(queryLacTacButton, 1, 1)
                        
                        add(Label("eNB/gNB:"), 0, 2)
                        add(enbGnbTextField, 1, 2)
                        add(queryEnbGnbButton, 1, 3)
                        
                        add(Label("Band:"), 0, 4)
                        add(bandTextField, 1, 4)
                        add(queryBandButton, 1, 5)
                    }
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
        queryLacTacButton.setOnAction {
            queryByLacTac()
        }
        
        queryEnbGnbButton.setOnAction {
            queryByEnbGnb()
        }
        
        queryBandButton.setOnAction {
            queryByBand()
        }
        
        // Allow Enter key to trigger queries
        lacTacTextField.setOnAction { queryByLacTac() }
        enbGnbTextField.setOnAction { queryByEnbGnb() }
        bandTextField.setOnAction { queryByBand() }
    }

    fun queryByLacTac() {
        val lacTac = lacTacTextField.text.trim()

        if (lacTac.isEmpty()) {
            resultsTextArea.text = "Error: Please enter a LAC/TAC"
            return
        }

        // Validate it's a number
        val lacTacValue = try {
            lacTac.toInt()
        } catch (e: NumberFormatException) {
            resultsTextArea.text = "Error: LAC/TAC must be a valid number"
            return
        }

        if (lacTacValue <= 0) {
            resultsTextArea.text = "Error: LAC/TAC must be greater than 0"
            return
        }

        // Check if controller is available
        if (controller == null) {
            resultsTextArea.text = "Error: Controller not initialized. Please restart the application."
            return
        }

        try {
            // Show processing message
            resultsTextArea.text = "Querying by LAC/TAC:\n" +
                    "LAC/TAC: $lacTacValue\n" +
                    "Processing..."

            // Extract filters from main UI
            val filters = extractFiltersFromMainView()

            // Query the database through controller
            val cells = controller!!.queryCellsByLacTac(
                lacTac = lacTacValue,
                filters = filters
            )

            // Store results for export
            lastQueryResults = cells

            // Format and display results
            resultsTextArea.text = formatResults(cells, "LAC/TAC", lacTacValue.toString(), filters)

            println("Querying by LAC/TAC: $lacTacValue")
            println("Filters applied: $filters")
            println("Found ${cells.size} cell(s)")

        } catch (e: IllegalArgumentException) {
            resultsTextArea.text = "Error: ${e.message}"
        } catch (e: Exception) {
            resultsTextArea.text = "Error querying database: ${e.message}"
            e.printStackTrace()
        }
    }

    fun queryByEnbGnb() {
        val enbGnb = enbGnbTextField.text.trim()

        if (enbGnb.isEmpty()) {
            resultsTextArea.text = "Error: Please enter an eNB/gNB identifier"
            return
        }

        // Validate it's a number
        val enbGnbId = try {
            enbGnb.toInt()
        } catch (e: NumberFormatException) {
            resultsTextArea.text = "Error: eNB/gNB must be a valid number"
            return
        }

        if (enbGnbId <= 0) {
            resultsTextArea.text = "Error: eNB/gNB must be greater than 0"
            return
        }

        // Check if controller is available
        if (controller == null) {
            resultsTextArea.text = "Error: Controller not initialized. Please restart the application."
            return
        }

        try {
            // Show processing message
            resultsTextArea.text = "Querying by eNB/gNB:\n" +
                    "eNB/gNB ID: $enbGnbId\n" +
                    "Processing..."

            // Extract filters from main UI
            val filters = extractFiltersFromMainView()

            // Query the database through controller
            val cells = controller!!.queryCellsByEnbGnb(
                enbGnbId = enbGnbId,
                filters = filters
            )

            // Store results for export
            lastQueryResults = cells

            // Format and display results
            resultsTextArea.text = formatResults(cells, "eNB/gNB", enbGnbId.toString(), filters)

            println("Querying by eNB/gNB: $enbGnbId")
            println("Filters applied: $filters")
            println("Found ${cells.size} cell(s)")

        } catch (e: IllegalArgumentException) {
            resultsTextArea.text = "Error: ${e.message}"
        } catch (e: Exception) {
            resultsTextArea.text = "Error querying database: ${e.message}"
            e.printStackTrace()
        }
    }

    fun queryByBand() {
        val band = bandTextField.text.trim()

        if (band.isEmpty()) {
            resultsTextArea.text = "Error: Please enter a band"
            return
        }

        // Check if controller is available
        if (controller == null) {
            resultsTextArea.text = "Error: Controller not initialized. Please restart the application."
            return
        }

        try {
            // Show processing message
            resultsTextArea.text = "Querying by Band:\n" +
                    "Band: $band\n" +
                    "Processing..."

            // Extract filters from main UI
            val filters = extractFiltersFromMainView()

            // Query the database through controller
            val cells = controller!!.queryCellsByBand(
                band = band,
                filters = filters
            )

            // Store results for export
            lastQueryResults = cells

            // Format and display results
            resultsTextArea.text = formatResults(cells, "Band", band, filters)

            println("Querying by Band: $band")
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
        queryType: String,
        queryValue: String,
        filters: FilterParams
    ): String {
        val header = "Network Query Results\n" +
                "Query Type: $queryType\n" +
                "Query Value: $queryValue\n" +
                formatFiltersInfo(filters) +
                "Found ${cells.size} cell(s)\n" +
                "=".repeat(60) + "\n"

        if (cells.isEmpty()) {
            return header + "No cells found matching the specified criteria and filters."
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

    // Public methods for integration with controller
    fun setResults(content: String) {
        resultsTextArea.text = content
    }

    fun getLacTacTextField(): TextField = lacTacTextField
    fun getEnbGnbTextField(): TextField = enbGnbTextField
    fun getBandTextField(): TextField = bandTextField
    fun getQueryLacTacButton(): Button = queryLacTacButton
    fun getQueryEnbGnbButton(): Button = queryEnbGnbButton
    fun getQueryBandButton(): Button = queryBandButton
    fun getResultsTextArea(): TextArea = resultsTextArea
    fun getLastQueryResults(): List<com.geocell.desktopanalyst.model.domain.Cell> = lastQueryResults
}